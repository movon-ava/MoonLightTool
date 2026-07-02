package toolbox.service;

import toolbox.model.LauncherType;
import toolbox.model.NodeType;
import toolbox.model.ToolboxNode;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LauncherService {
    private static final long CUSTOM_LAUNCH_WAIT_MILLIS = 1500L;
    private static final int MAX_LOG_CHARS = 4000;
    private File toolsRootDirectory;

    public LauncherService() {
        this(AppPaths.defaultToolsRoot());
    }

    public LauncherService(File toolsRootDirectory) {
        this.toolsRootDirectory = toolsRootDirectory == null ? AppPaths.defaultToolsRoot() : toolsRootDirectory;
    }

    public void setToolsRootDirectory(File toolsRootDirectory) {
        this.toolsRootDirectory = toolsRootDirectory == null ? AppPaths.defaultToolsRoot() : toolsRootDirectory;
    }

    public enum LaunchMode {
        DEFAULT("Default"),
        CMD("CMD"),
        POWERSHELL("PowerShell"),
        EXPLORER("Explorer");

        private final String displayName;

        LaunchMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public void open(ToolboxNode node) throws IOException, URISyntaxException {
        open(node, LaunchMode.DEFAULT);
    }

    public void open(ToolboxNode node, LaunchMode mode) throws IOException, URISyntaxException {
        if (node == null || node.getType() == null) {
            return;
        }

        if (node.usesCustomLauncher()) {
            openWithCustomLauncher(node, mode);
            return;
        }

        if (node.getType() == NodeType.WEB) {
            openWeb(node.getTarget(), mode);
            return;
        }

        if (node.getType() == NodeType.TOOL || node.getType() == NodeType.DOCUMENT) {
            openFileTarget(node.getTarget(), mode, node.getType() == NodeType.DOCUMENT);
        }
    }

    private void openWithCustomLauncher(ToolboxNode node, LaunchMode mode) throws IOException {
        if (node.getLauncherType() != LauncherType.CUSTOM_COMMAND) {
            throw new IOException("Unsupported launcher type");
        }

        String launcherPath = trim(node.getLauncherPath());
        if (launcherPath.isEmpty()) {
            throw new IOException("Launcher path is empty");
        }

        File launcherFile = AppPaths.resolvePath(launcherPath, toolsRootDirectory);
        if (!launcherFile.exists()) {
            throw new IOException("Launcher does not exist: " + launcherFile.getAbsolutePath());
        }

        if (mode == LaunchMode.EXPLORER) {
            openFileTarget(resolveLocateTarget(node), LaunchMode.EXPLORER, false);
            return;
        }

        List<String> command = new ArrayList<String>();
        command.add(launcherFile.getAbsolutePath());
        command.addAll(parseArguments(applyPlaceholders(trim(node.getLaunchArguments()), node)));
        File workingDirectory = resolveWorkingDirectory(node);

        if (mode == LaunchMode.CMD) {
            ProcessBuilder builder = new ProcessBuilder("cmd", "/k", buildWindowsCommand(command));
            if (workingDirectory != null) {
                builder.directory(workingDirectory);
            }
            builder.start();
            return;
        }

        if (mode == LaunchMode.POWERSHELL) {
            ProcessBuilder builder = new ProcessBuilder("powershell", "-NoExit", "-NoProfile", "-Command",
                    "& '" + escapePowerShell(launcherFile.getAbsolutePath()) + "'" + buildPowerShellCommandArguments(command.subList(1, command.size())));
            if (workingDirectory != null) {
                builder.directory(workingDirectory);
            }
            builder.start();
            return;
        }

        ProcessBuilder builder = new ProcessBuilder(command);
        if (workingDirectory != null) {
            builder.directory(workingDirectory);
        }
        File logFile = createLaunchLogFile(node);
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));

        Process process = builder.start();
        try {
            boolean finished = process.waitFor(CUSTOM_LAUNCH_WAIT_MILLIS, TimeUnit.MILLISECONDS);
            if (finished && process.exitValue() != 0) {
                throw new IOException(buildLaunchFailureMessage(command, workingDirectory, process.exitValue(), logFile));
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for custom launcher", ex);
        }
    }

    private void openWeb(String url, LaunchMode mode) throws IOException, URISyntaxException {
        if (url == null || url.trim().isEmpty()) {
            throw new IOException("URL is empty");
        }

        String target = url.trim();
        if (mode == LaunchMode.CMD) {
            new ProcessBuilder("cmd", "/c", "start", "", target).start();
            return;
        }
        if (mode == LaunchMode.POWERSHELL) {
            new ProcessBuilder("powershell", "-NoProfile", "-Command",
                    "Start-Process '" + escapePowerShell(target) + "'").start();
            return;
        }
        if (mode == LaunchMode.EXPLORER) {
            throw new IOException("Explorer mode is not supported for URLs");
        }

        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Desktop API is not supported");
        }
        Desktop.getDesktop().browse(new URI(target));
    }

    private void openFileTarget(String target, LaunchMode mode, boolean preferFileOpen) throws IOException {
        if (target == null || target.trim().isEmpty()) {
            throw new IOException("Target path is empty");
        }

        File file = AppPaths.resolvePath(target.trim(), toolsRootDirectory);
        if (!file.exists()) {
            throw new IOException("Target does not exist: " + file.getAbsolutePath());
        }

        if (mode == LaunchMode.DEFAULT && file.isFile() && isScriptFile(file)) {
            openScriptTarget(file);
            return;
        }
        if (mode == LaunchMode.DEFAULT && file.isFile() && isExecutableFile(file)) {
            openExecutableTarget(file);
            return;
        }

        if (mode == LaunchMode.CMD) {
            File parent = file.isDirectory() ? file : file.getParentFile();
            if (parent != null && parent.exists()) {
                new ProcessBuilder("cmd", "/c", "start", "", "cmd", "/k", "cd /d " + quoteForCmd(parent.getAbsolutePath())
                        + " && " + quoteForCmd(file.getAbsolutePath())).start();
            } else {
                new ProcessBuilder("cmd", "/c", "start", "", file.getAbsolutePath()).start();
            }
            return;
        }
        if (mode == LaunchMode.POWERSHELL) {
            File parent = file.isDirectory() ? file : file.getParentFile();
            if (parent != null && parent.exists()) {
                new ProcessBuilder("powershell", "-NoExit", "-NoProfile", "-Command",
                        "Set-Location -LiteralPath '" + escapePowerShell(parent.getAbsolutePath()) + "'; & '"
                                + escapePowerShell(file.getAbsolutePath()) + "'").start();
            } else {
                new ProcessBuilder("powershell", "-NoProfile", "-Command",
                        "Start-Process -LiteralPath '" + escapePowerShell(file.getAbsolutePath()) + "'").start();
            }
            return;
        }
        if (mode == LaunchMode.EXPLORER) {
            File parent = file.isDirectory() ? file : file.getParentFile();
            if (parent == null || !parent.exists()) {
                throw new IOException("Cannot locate parent directory");
            }
            new ProcessBuilder("explorer.exe", parent.getAbsolutePath()).start();
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Desktop API is not supported");
        }

        if (preferFileOpen) {
            Desktop.getDesktop().open(file);
            return;
        }
        Desktop.getDesktop().open(file);
    }

    private void openScriptTarget(File file) throws IOException {
        String name = file.getName().toLowerCase();
        File parent = file.getParentFile();
        if (parent == null || !parent.exists()) {
            throw new IOException("Cannot locate script directory: " + file.getAbsolutePath());
        }

        if (name.endsWith(".ps1")) {
            new ProcessBuilder("powershell", "-NoExit", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command",
                    "Set-Location -LiteralPath '" + escapePowerShell(parent.getAbsolutePath()) + "'; & '"
                            + escapePowerShell(file.getAbsolutePath()) + "'").start();
            return;
        }

        new ProcessBuilder("cmd", "/c", "start", "", "cmd", "/k", "cd /d " + quoteForCmd(parent.getAbsolutePath())
                + " && " + quoteForCmd(file.getAbsolutePath())).start();
    }

    private void openExecutableTarget(File file) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(file.getAbsolutePath());
        File parent = file.getParentFile();
        if (parent != null && parent.exists()) {
            builder.directory(parent);
        }
        builder.start();
    }

    private String resolveLocateTarget(ToolboxNode node) {
        String target = trim(node.getTarget());
        if (!target.isEmpty()) {
            return target;
        }
        return trim(node.getLauncherPath());
    }

    private File resolveWorkingDirectory(ToolboxNode node) throws IOException {
        String configured = trim(node.getWorkingDirectory());
        if (!configured.isEmpty()) {
            File workDir = AppPaths.resolvePath(applyPlaceholders(configured, node), toolsRootDirectory);
            if (!workDir.exists() || !workDir.isDirectory()) {
                throw new IOException("Working directory does not exist: " + workDir.getAbsolutePath());
            }
            return workDir;
        }

        File target = buildExistingFile(trim(node.getTarget()));
        if (target != null) {
            return target.isDirectory() ? target : target.getParentFile();
        }

        File launcher = buildExistingFile(trim(node.getLauncherPath()));
        if (launcher != null) {
            return launcher.isDirectory() ? launcher : launcher.getParentFile();
        }
        return null;
    }

    private File buildExistingFile(String path) {
        if (path.isEmpty()) {
            return null;
        }
        File file = AppPaths.resolvePath(path, toolsRootDirectory);
        return file.exists() ? file : null;
    }

    private String applyPlaceholders(String template, ToolboxNode node) {
        return template
                .replace("{target}", AppPaths.displayPath(node.getTarget(), toolsRootDirectory))
                .replace("{targetPath}", AppPaths.displayPath(node.getTarget(), toolsRootDirectory))
                .replace("{name}", safe(node.getName()))
                .replace("{workingDir}", AppPaths.displayPath(node.getWorkingDirectory(), toolsRootDirectory))
                .replace("{launcher}", AppPaths.displayPath(node.getLauncherPath(), toolsRootDirectory));
    }

    private List<String> parseArguments(String text) throws IOException {
        List<String> result = new ArrayList<String>();
        if (text == null || text.trim().isEmpty()) {
            return result;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (Character.isWhitespace(ch) && !inQuotes) {
                if (current.length() > 0) {
                    result.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }
            current.append(ch);
        }

        if (inQuotes) {
            throw new IOException("Unclosed quote in launch arguments");
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }
        return result;
    }

    private String buildWindowsCommand(List<String> command) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < command.size(); i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(quoteForCmd(command.get(i)));
        }
        return builder.toString();
    }

    private String quoteForCmd(String value) {
        String escaped = value.replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    private String buildPowerShellCommandArguments(List<String> arguments) {
        if (arguments.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String argument : arguments) {
            builder.append(" '").append(escapePowerShell(argument)).append("'");
        }
        return builder.toString();
    }

    private String buildPowerShellArgumentLiteral(List<String> arguments) {
        if (arguments.isEmpty()) {
            return "@()";
        }
        StringBuilder builder = new StringBuilder("@(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("'").append(escapePowerShell(arguments.get(i))).append("'");
        }
        builder.append(")");
        return builder.toString();
    }

    private String buildPowerShellWorkingDirectoryLiteral(File workingDirectory) {
        if (workingDirectory == null) {
            return "";
        }
        return " -WorkingDirectory '" + escapePowerShell(workingDirectory.getAbsolutePath()) + "'";
    }

    private String escapePowerShell(String value) {
        return value.replace("'", "''");
    }

    private boolean isScriptFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".bat") || name.endsWith(".cmd") || name.endsWith(".ps1");
    }

    private boolean isExecutableFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".exe");
    }

    private File createLaunchLogFile(ToolboxNode node) throws IOException {
        File logDirectory = new File("data", "launcher-logs").getAbsoluteFile();
        if (!logDirectory.exists() && !logDirectory.mkdirs()) {
            throw new IOException("Cannot create launcher log directory: " + logDirectory.getAbsolutePath());
        }
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
        String baseName = sanitizeFileName(trim(node.getName()));
        if (baseName.isEmpty()) {
            baseName = "tool";
        }
        return new File(logDirectory, baseName + "-" + timestamp + ".log");
    }

    private String buildLaunchFailureMessage(List<String> command, File workingDirectory, int exitCode, File logFile) {
        StringBuilder builder = new StringBuilder();
        builder.append("Custom launcher failed").append("\n");
        builder.append("Command: ").append(buildDisplayCommand(command)).append("\n");
        builder.append("Working directory: ").append(workingDirectory == null ? "-" : workingDirectory.getAbsolutePath()).append("\n");
        builder.append("Exit code: ").append(exitCode).append("\n");
        builder.append("Log file: ").append(logFile.getAbsolutePath());

        String excerpt = readLogExcerpt(logFile);
        if (!excerpt.isEmpty()) {
            builder.append("\n\nLog excerpt:\n").append(excerpt);
        }
        return builder.toString();
    }

    private String buildDisplayCommand(List<String> command) {
        return buildWindowsCommand(command);
    }

    private String readLogExcerpt(File logFile) {
        if (logFile == null || !logFile.exists() || !logFile.isFile()) {
            return "";
        }
        try {
            String text = new String(Files.readAllBytes(logFile.toPath()), Charset.defaultCharset()).trim();
            if (text.length() <= MAX_LOG_CHARS) {
                return text;
            }
            return text.substring(text.length() - MAX_LOG_CHARS);
        } catch (IOException ex) {
            return "";
        }
    }

    private String sanitizeFileName(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[\\\\/:*?\"<>|\\s]+", "-").replaceAll("^-+|-+$", "");
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
