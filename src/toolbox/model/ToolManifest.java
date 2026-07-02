package toolbox.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ToolManifest implements Serializable {
    private String version;
    private String updatedAt;
    private String note;
    private List<ToolManifestEntry> tools;

    public ToolManifest() {
        this.version = "1";
        this.updatedAt = "";
        this.note = "";
        this.tools = new ArrayList<ToolManifestEntry>();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<ToolManifestEntry> getTools() {
        if (tools == null) {
            tools = new ArrayList<ToolManifestEntry>();
        }
        return tools;
    }

    public void setTools(List<ToolManifestEntry> tools) {
        this.tools = tools;
    }
}
