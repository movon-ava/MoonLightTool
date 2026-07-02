package toolbox.service;

import toolbox.model.ToolManifest;
import toolbox.model.ToolManifestEntry;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ToolManifestService {
    private final File manifestFile;
    private final File toolsRoot;

    public ToolManifestService(File manifestFile, File toolsRoot) {
        this.manifestFile = manifestFile;
        this.toolsRoot = toolsRoot;
    }

    public ToolManifest loadOrCreate() throws IOException {
        if (!manifestFile.exists()) {
            ToolManifest manifest = new ToolManifest();
            save(manifest);
            return manifest;
        }

        XMLDecoder decoder = null;
        try {
            decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(manifestFile)));
            Object object = decoder.readObject();
            if (object instanceof ToolManifest) {
                return (ToolManifest) object;
            }
        } catch (Exception ex) {
            throw new IOException("Cannot read tools manifest: " + manifestFile.getAbsolutePath(), ex);
        } finally {
            if (decoder != null) {
                decoder.close();
            }
        }
        return new ToolManifest();
    }

    public void save(ToolManifest manifest) throws IOException {
        File parent = manifestFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        XMLEncoder encoder = null;
        try {
            encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(manifestFile)));
            encoder.writeObject(manifest);
            encoder.flush();
        } finally {
            if (encoder != null) {
                encoder.close();
            }
        }
    }

    public List<ToolStatus> checkTools(ToolManifest manifest) {
        List<ToolStatus> statuses = new ArrayList<ToolStatus>();
        for (ToolManifestEntry entry : manifest.getTools()) {
            File target = AppPaths.resolvePath(entry.getPath(), toolsRoot);
            boolean exists = target.exists();
            boolean sizeMatches = entry.getSize() <= 0 || (exists && target.isFile() && target.length() == entry.getSize());
            statuses.add(new ToolStatus(entry, target, exists, sizeMatches));
        }
        return statuses;
    }

    public File getManifestFile() {
        return manifestFile;
    }

    public File getToolsRoot() {
        return toolsRoot;
    }

    public static final class ToolStatus {
        private final ToolManifestEntry entry;
        private final File file;
        private final boolean exists;
        private final boolean sizeMatches;

        public ToolStatus(ToolManifestEntry entry, File file, boolean exists, boolean sizeMatches) {
            this.entry = entry;
            this.file = file;
            this.exists = exists;
            this.sizeMatches = sizeMatches;
        }

        public ToolManifestEntry getEntry() {
            return entry;
        }

        public File getFile() {
            return file;
        }

        public boolean isExists() {
            return exists;
        }

        public boolean isSizeMatches() {
            return sizeMatches;
        }

        public boolean isOk() {
            return exists && sizeMatches;
        }
    }
}
