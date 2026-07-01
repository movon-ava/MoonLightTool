package toolbox.ui;

import toolbox.model.LauncherType;
import toolbox.model.NodeType;
import toolbox.model.ToolKind;
import toolbox.service.LauncherService;

public final class I18n {
    public static final String ZH = "zh";
    public static final String EN = "en";

    private final String language;

    public I18n(String language) {
        this.language = ZH.equalsIgnoreCase(language) ? ZH : EN;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isChinese() {
        return ZH.equals(language);
    }

    public String text(String zh, String en) {
        return isChinese() ? zh : en;
    }

    public String appTitle() {
        return text("MoonLight工具箱", "MoonLight Toolbox");
    }

    public String welcomeText() {
        return text(
                "按分类管理工具、网址和文档，左侧只看分类，中间看内容，右侧看详情。",
                "Organize tools, documents, and links by category.");
    }

    public String typeName(NodeType type) {
        if (type == NodeType.TOOL) {
            return text("工具", "Tool");
        }
        if (type == NodeType.WEB) {
            return text("网址", "Web");
        }
        if (type == NodeType.DOCUMENT) {
            return text("文档", "Document");
        }
        if (type == NodeType.ROOT) {
            return text("工具箱总览", "Toolbox overview");
        }
        return text("分类", "Category");
    }

    public String toolKind(ToolKind kind) {
        if (kind == ToolKind.WEB) {
            return "Web";
        }
        if (kind == ToolKind.BUILTIN) {
            return text("内置", "Built-in");
        }
        if (kind == ToolKind.INSTALLABLE) {
            return text("需安装配置", "Installable");
        }
        if (kind == ToolKind.LINUX) {
            return "Linux";
        }
        if (kind == ToolKind.PLUGIN) {
            return text("插件", "Plugin");
        }
        return "-";
    }

    public String launcherType(LauncherType type) {
        if (type == LauncherType.CUSTOM_COMMAND) {
            return text("自定义命令", "Custom command");
        }
        return text("系统默认", "System default");
    }

    public String launchMode(LauncherService.LaunchMode mode) {
        if (mode == LauncherService.LaunchMode.CMD) {
            return text("CMD打开", "Open with CMD");
        }
        if (mode == LauncherService.LaunchMode.POWERSHELL) {
            return text("PowerShell打开", "Open with PowerShell");
        }
        if (mode == LauncherService.LaunchMode.EXPLORER) {
            return text("资源管理器", "Explorer");
        }
        return text("默认打开", "Open");
    }
}
