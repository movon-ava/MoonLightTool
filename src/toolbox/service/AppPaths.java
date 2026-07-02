package toolbox.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public final class AppPaths {
    public static final String APP_HOME_TOKEN = "${APP_HOME}";
    public static final String TOOLS_TOKEN = "${TOOLS}";

    private AppPaths() {
    }

    public static File appHome() {
        try {
            URL location = AppPaths.class.getProtectionDomain().getCodeSource().getLocation();
            File file = new File(location.toURI()).getAbsoluteFile();
            File base = file.isFile() ? file.getParentFile() : file;
            return base == null ? new File(".").getCanonicalFile() : base.getCanonicalFile();
        } catch (Exception ex) {
            try {
                return new File(".").getCanonicalFile();
            } catch (IOException ignored) {
                return new File(".").getAbsoluteFile();
            }
        }
    }

    public static File defaultToolsRoot() {
        return new File(appHome(), "tools");
    }

    public static File resolveToolsRoot(String configured) {
        String value = trim(configured);
        if (value.isEmpty()) {
            return defaultToolsRoot();
        }
        String normalized = value.replace('\\', File.separatorChar).replace('/', File.separatorChar);
        if (normalized.startsWith(APP_HOME_TOKEN)) {
            String suffix = normalized.substring(APP_HOME_TOKEN.length());
            return new File(appHome(), stripLeadingSeparator(suffix)).getAbsoluteFile();
        }
        File file = new File(normalized);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(appHome(), normalized).getAbsoluteFile();
    }

    public static File resolvePath(String value, File toolsRoot) {
        String path = trim(value);
        if (path.isEmpty()) {
            return new File("");
        }
        String normalized = path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
        if (normalized.startsWith(APP_HOME_TOKEN)) {
            String suffix = normalized.substring(APP_HOME_TOKEN.length());
            return new File(appHome(), stripLeadingSeparator(suffix)).getAbsoluteFile();
        }
        if (normalized.startsWith(TOOLS_TOKEN)) {
            String suffix = normalized.substring(TOOLS_TOKEN.length());
            return new File(toolsRoot == null ? defaultToolsRoot() : toolsRoot, stripLeadingSeparator(suffix)).getAbsoluteFile();
        }
        File file = new File(normalized);
        if (file.isAbsolute()) {
            return file;
        }
        if (normalized.equals("tools") || normalized.startsWith("tools" + File.separator)) {
            return new File(appHome(), normalized).getAbsoluteFile();
        }
        File root = toolsRoot == null ? appHome() : toolsRoot;
        return new File(root, normalized).getAbsoluteFile();
    }

    public static String relativizeForConfig(String value, File toolsRoot) {
        String path = trim(value);
        if (path.isEmpty() || looksLikeUrl(path) || path.contains("{")) {
            return path;
        }
        try {
            File file = new File(path);
            if (!file.isAbsolute()) {
                return normalizeSeparators(path);
            }
            File canonical = file.getCanonicalFile();
            File tools = (toolsRoot == null ? defaultToolsRoot() : toolsRoot).getCanonicalFile();
            String relativeToTools = relativeChildPath(tools, canonical);
            if (relativeToTools != null) {
                return normalizeSeparators(new File("tools", relativeToTools).getPath());
            }
            File home = appHome().getCanonicalFile();
            String relativeToHome = relativeChildPath(home, canonical);
            if (relativeToHome != null) {
                return normalizeSeparators(relativeToHome);
            }
            return path;
        } catch (IOException ex) {
            return path;
        }
    }

    public static String displayPath(String value, File toolsRoot) {
        String path = trim(value);
        if (path.isEmpty() || looksLikeUrl(path)) {
            return path;
        }
        File file = resolvePath(path, toolsRoot);
        return file.getPath();
    }

    public static boolean looksLikeUrl(String value) {
        String lower = trim(value).toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("file://");
    }

    private static String relativeChildPath(File parent, File child) throws IOException {
        String parentPath = parent.getCanonicalPath();
        String childPath = child.getCanonicalPath();
        if (childPath.equals(parentPath)) {
            return "";
        }
        String prefix = parentPath.endsWith(File.separator) ? parentPath : parentPath + File.separator;
        if (!childPath.startsWith(prefix)) {
            return null;
        }
        return childPath.substring(prefix.length());
    }

    private static String stripLeadingSeparator(String value) {
        String result = value == null ? "" : value;
        while (result.startsWith(File.separator)) {
            result = result.substring(1);
        }
        return result;
    }

    private static String normalizeSeparators(String value) {
        return trim(value).replace('\\', '/');
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
