package toolbox.model;

import java.io.Serializable;

public enum ToolKind implements Serializable {
    WEB("\u0057\u0065\u0062"),
    BUILTIN("\u5185\u7f6e"),
    INSTALLABLE("\u9700\u5b89\u88c5\u914d\u7f6e"),
    LINUX("\u004c\u0069\u006e\u0075\u0078"),
    PLUGIN("\u63d2\u4ef6");

    private final String displayName;

    ToolKind(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
