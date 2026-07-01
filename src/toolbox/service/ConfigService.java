package toolbox.service;

import toolbox.model.ToolboxConfig;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfigService {
    private final File configFile;

    public ConfigService(File configFile) {
        this.configFile = configFile;
    }

    public ToolboxConfig load() {
        if (!configFile.exists()) {
            ToolboxConfig sample = ToolboxConfig.sample();
            try {
                save(sample);
            } catch (IOException ignored) {
            }
            return sample;
        }

        XMLDecoder decoder = null;
        try {
            decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(configFile)));
            Object object = decoder.readObject();
            if (object instanceof ToolboxConfig) {
                return (ToolboxConfig) object;
            }
        } catch (Exception ignored) {
        } finally {
            if (decoder != null) {
                decoder.close();
            }
        }
        return ToolboxConfig.sample();
    }

    public void save(ToolboxConfig config) throws IOException {
        File parent = configFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        backupConfigIfPresent();

        XMLEncoder encoder = null;
        try {
            encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(configFile)));
            encoder.writeObject(config);
            encoder.flush();
        } finally {
            if (encoder != null) {
                encoder.close();
            }
        }
    }

    public File getConfigFile() {
        return configFile;
    }

    public File getBackupDirectory() {
        File parent = configFile.getParentFile();
        if (parent == null) {
            return new File("backup");
        }
        return new File(parent, "backup");
    }

    private void backupConfigIfPresent() throws IOException {
        if (!configFile.exists() || !configFile.isFile()) {
            return;
        }

        File backupDir = getBackupDirectory();
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            throw new IOException("无法创建备份目录: " + backupDir.getAbsolutePath());
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
        File backupFile = new File(backupDir, "toolbox-config-" + timestamp + ".xml.bak");
        copyFile(configFile, backupFile);
        writeLatestPointer(backupDir, backupFile);
    }

    private void copyFile(File source, File target) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(target);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
            output.flush();
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        }
    }

    private void writeLatestPointer(File backupDir, File backupFile) throws IOException {
        File marker = new File(backupDir, "LATEST_BACKUP.txt");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(marker), StandardCharsets.UTF_8));
            writer.write(backupFile.getName());
            writer.newLine();
            writer.write(backupFile.getAbsolutePath());
            writer.newLine();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
