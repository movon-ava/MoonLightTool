package toolbox.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ToolboxNode implements Serializable {
    private String id;
    private String name;
    private NodeType type;
    private ToolKind toolKind;
    private String target;
    private LauncherType launcherType;
    private String launcherPath;
    private String launchArguments;
    private String workingDirectory;
    private String setupCommand;
    private String description;
    private String tags;
    private String colorHex;
    private String iconKey;
    private List<ToolboxNode> children;

    public ToolboxNode() {
        this.id = UUID.randomUUID().toString();
        this.name = "";
        this.type = NodeType.FOLDER;
        this.toolKind = ToolKind.BUILTIN;
        this.target = "";
        this.launcherType = LauncherType.SYSTEM_OPEN;
        this.launcherPath = "";
        this.launchArguments = "";
        this.workingDirectory = "";
        this.setupCommand = "";
        this.description = "";
        this.tags = "";
        this.colorHex = "";
        this.iconKey = "";
        this.children = new ArrayList<ToolboxNode>();
    }

    public static ToolboxNode root() {
        ToolboxNode node = new ToolboxNode();
        node.setName("全部分类");
        node.setType(NodeType.ROOT);
        node.setIconKey("moon");
        return node;
    }

    public static ToolboxNode folder(String name) {
        ToolboxNode node = new ToolboxNode();
        node.setName(name);
        node.setType(NodeType.FOLDER);
        return node;
    }

    public static ToolboxNode tool(String name, String target) {
        ToolboxNode node = new ToolboxNode();
        node.setName(name);
        node.setType(NodeType.TOOL);
        node.setToolKind(ToolKind.BUILTIN);
        node.setTarget(target);
        return node;
    }

    public static ToolboxNode web(String name, String target) {
        ToolboxNode node = new ToolboxNode();
        node.setName(name);
        node.setType(NodeType.WEB);
        node.setToolKind(ToolKind.WEB);
        node.setTarget(target);
        return node;
    }

    public static ToolboxNode document(String name, String target) {
        ToolboxNode node = new ToolboxNode();
        node.setName(name);
        node.setType(NodeType.DOCUMENT);
        node.setToolKind(ToolKind.BUILTIN);
        node.setTarget(target);
        return node;
    }

    public boolean isContainer() {
        return type == NodeType.ROOT || type == NodeType.FOLDER;
    }

    public void addChild(ToolboxNode child) {
        if (children == null) {
            children = new ArrayList<ToolboxNode>();
        }
        children.add(child);
    }

    public void removeChild(ToolboxNode child) {
        if (children != null) {
            children.remove(child);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public ToolKind getToolKind() {
        if (toolKind != null) {
            return toolKind;
        }
        return type == NodeType.WEB ? ToolKind.WEB : ToolKind.BUILTIN;
    }

    public void setToolKind(ToolKind toolKind) {
        this.toolKind = toolKind;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public LauncherType getLauncherType() {
        return launcherType == null ? LauncherType.SYSTEM_OPEN : launcherType;
    }

    public void setLauncherType(LauncherType launcherType) {
        this.launcherType = launcherType;
    }

    public String getLauncherPath() {
        return launcherPath;
    }

    public void setLauncherPath(String launcherPath) {
        this.launcherPath = launcherPath;
    }

    public String getLaunchArguments() {
        return launchArguments;
    }

    public void setLaunchArguments(String launchArguments) {
        this.launchArguments = launchArguments;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getSetupCommand() {
        return setupCommand;
    }

    public void setSetupCommand(String setupCommand) {
        this.setupCommand = setupCommand;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
    }

    public List<ToolboxNode> getChildren() {
        if (children == null) {
            children = new ArrayList<ToolboxNode>();
        }
        return children;
    }

    public void setChildren(List<ToolboxNode> children) {
        this.children = children;
    }

    public boolean usesCustomLauncher() {
        return getLauncherType() == LauncherType.CUSTOM_COMMAND;
    }

    @Override
    public String toString() {
        return name == null || name.trim().isEmpty() ? "未命名节点" : name;
    }
}
