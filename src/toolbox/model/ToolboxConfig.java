package toolbox.model;

import java.io.Serializable;

public class ToolboxConfig implements Serializable {
    private AppSettings settings;
    private ToolboxNode rootNode;

    public ToolboxConfig() {
        this.settings = new AppSettings();
        this.rootNode = ToolboxNode.root();
    }

    public static ToolboxConfig sample() {
        ToolboxConfig config = new ToolboxConfig();

        ToolboxNode workFolder = ToolboxNode.folder("效率工具");
        workFolder.setColorHex("#F59E0B");
        workFolder.setIconKey("terminal");
        workFolder.addChild(ToolboxNode.tool("记事本", "C:\\Windows\\system32\\notepad.exe"));
        workFolder.addChild(ToolboxNode.tool("计算器", "C:\\Windows\\System32\\calc.exe"));

        ToolboxNode webFolder = ToolboxNode.folder("常用网址");
        webFolder.setColorHex("#10B981");
        webFolder.setIconKey("globe");
        webFolder.addChild(ToolboxNode.web("GitHub", "https://github.com"));
        webFolder.addChild(ToolboxNode.web("Stack Overflow", "https://stackoverflow.com"));

        ToolboxNode docsFolder = ToolboxNode.folder("文档资料");
        docsFolder.setColorHex("#7C3AED");
        docsFolder.setIconKey("book");
        docsFolder.addChild(ToolboxNode.document("系统目录", "C:\\Windows"));
        docsFolder.addChild(ToolboxNode.folder("安全研究"));

        config.getRootNode().addChild(workFolder);
        config.getRootNode().addChild(webFolder);
        config.getRootNode().addChild(docsFolder);
        return config;
    }

    public AppSettings getSettings() {
        return settings;
    }

    public void setSettings(AppSettings settings) {
        this.settings = settings;
    }

    public ToolboxNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(ToolboxNode rootNode) {
        this.rootNode = rootNode;
    }
}
