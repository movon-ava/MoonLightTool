package toolbox.model;

import java.io.Serializable;

public class ToolManifestEntry implements Serializable {
    private String id;
    private String name;
    private String version;
    private String path;
    private String url;
    private String sha256;
    private long size;
    private String description;

    public ToolManifestEntry() {
        this.id = "";
        this.name = "";
        this.version = "";
        this.path = "";
        this.url = "";
        this.sha256 = "";
        this.size = 0L;
        this.description = "";
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
