package toolbox.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppSettings implements Serializable {
    private String appTitle;
    private String accentHex;
    private String welcomeText;
    private String toolsRootPath;
    private String language;
    private List<String> availableTags;
    private List<String> enabledLaunchModes;

    public AppSettings() {
        this.appTitle = "MoonLight Toolbox";
        this.accentHex = "#1F6FEB";
        this.welcomeText = "Organize tools, documents, and links by category.";
        this.toolsRootPath = "G:\\MoonLight\\tools";
        this.language = "zh";
        this.availableTags = new ArrayList<String>(Arrays.asList(
                "common", "internal", "external", "research", "dev", "ops", "other"
        ));
        this.enabledLaunchModes = new ArrayList<String>(Arrays.asList(
                "DEFAULT", "CMD", "POWERSHELL", "EXPLORER"
        ));
    }

    public String getAppTitle() {
        return appTitle;
    }

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }

    public String getAccentHex() {
        return accentHex;
    }

    public void setAccentHex(String accentHex) {
        this.accentHex = accentHex;
    }

    public String getWelcomeText() {
        return welcomeText;
    }

    public void setWelcomeText(String welcomeText) {
        this.welcomeText = welcomeText;
    }

    public String getToolsRootPath() {
        return toolsRootPath;
    }

    public void setToolsRootPath(String toolsRootPath) {
        this.toolsRootPath = toolsRootPath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getAvailableTags() {
        if (availableTags == null) {
            availableTags = new ArrayList<String>();
        }
        return availableTags;
    }

    public void setAvailableTags(List<String> availableTags) {
        this.availableTags = availableTags;
    }

    public List<String> getEnabledLaunchModes() {
        if (enabledLaunchModes == null) {
            enabledLaunchModes = new ArrayList<String>();
        }
        return enabledLaunchModes;
    }

    public void setEnabledLaunchModes(List<String> enabledLaunchModes) {
        this.enabledLaunchModes = enabledLaunchModes;
    }
}
