package toolbox.service;

import toolbox.model.LauncherType;
import toolbox.model.NodeType;
import toolbox.model.ToolboxNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class ToolTemplateService {
    private final File toolsRootDirectory;

    public ToolTemplateService(File toolsRootDirectory) {
        this.toolsRootDirectory = toolsRootDirectory;
    }

    public File getToolsRootDirectory() {
        return toolsRootDirectory;
    }

    public File createTemplate(ToolboxNode node) throws IOException {
        if (node == null) {
            throw new IOException("Node is null");
        }
        if (node.getType() == NodeType.WEB) {
            throw new IOException("Template directories are only for local tools or documents");
        }
        if (toolsRootDirectory == null) {
            throw new IOException("Tools root directory is not configured");
        }
        if (!toolsRootDirectory.exists() && !toolsRootDirectory.mkdirs()) {
            throw new IOException("Cannot create tools root directory: " + toolsRootDirectory.getAbsolutePath());
        }

        String bucket = inferBucket(node);
        File templateDir = new File(new File(toolsRootDirectory, bucket), sanitizeName(node.getName()));
        if (!templateDir.exists() && !templateDir.mkdirs()) {
            throw new IOException("Cannot create template directory: " + templateDir.getAbsolutePath());
        }

        File readme = new File(templateDir, "README.txt");
        writeText(readme, buildReadme(node, templateDir));

        if (node.getType() == NodeType.TOOL) {
            File launcherDir = new File(templateDir, "bin");
            if (!launcherDir.exists()) {
                launcherDir.mkdirs();
            }
            File dataDir = new File(templateDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
        }

        return templateDir;
    }

    private String inferBucket(ToolboxNode node) {
        if (node.getLauncherType() == LauncherType.CUSTOM_COMMAND) {
            String launcher = lower(node.getLauncherPath());
            if (launcher.contains("java")) {
                return "java";
            }
            if (launcher.contains("python")) {
                return "python";
            }
            if (launcher.contains("node")) {
                return "node";
            }
            return "custom";
        }
        if (node.getType() == NodeType.DOCUMENT) {
            return "document";
        }
        return "native";
    }

    private String buildReadme(ToolboxNode node, File templateDir) {
        StringBuilder builder = new StringBuilder();
        builder.append("Tool Template").append("\r\n");
        builder.append("================").append("\r\n\r\n");
        builder.append("Name: ").append(empty(node.getName())).append("\r\n");
        builder.append("Type: ").append(node.getType() == null ? "" : node.getType().name()).append("\r\n");
        builder.append("Directory: ").append(templateDir.getAbsolutePath()).append("\r\n");
        builder.append("Launcher Mode: ").append(node.getLauncherType() == null ? "" : node.getLauncherType().name()).append("\r\n");
        builder.append("Launcher Path: ").append(empty(node.getLauncherPath())).append("\r\n");
        builder.append("Launch Arguments: ").append(empty(node.getLaunchArguments())).append("\r\n");
        builder.append("Working Directory: ").append(empty(node.getWorkingDirectory())).append("\r\n");
        builder.append("Target: ").append(empty(node.getTarget())).append("\r\n\r\n");
        builder.append("Suggested next steps:").append("\r\n");
        builder.append("1. Put the actual tool files in this directory.").append("\r\n");
        builder.append("2. Update target / launcher path if needed.").append("\r\n");
        builder.append("3. Keep runtime-specific files inside the same directory when possible.").append("\r\n");
        return builder.toString();
    }

    private void writeText(File targetFile, String content) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8));
            writer.write(content);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private String sanitizeName(String value) {
        String raw = value == null ? "" : value.trim();
        if (raw.isEmpty()) {
            return "unnamed-tool";
        }
        return raw.replaceAll("[\\\\/:*?\"<>|\\s]+", "-").replaceAll("-{2,}", "-");
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private String empty(String value) {
        return value == null ? "" : value;
    }
}
