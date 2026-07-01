package toolbox.model;

import java.io.Serializable;

public enum LauncherType implements Serializable {
    SYSTEM_OPEN("系统默认"),
    CUSTOM_COMMAND("自定义命令");

    private final String displayName;

    LauncherType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
