package toolbox;

import toolbox.model.ToolboxConfig;
import toolbox.service.ConfigService;
import toolbox.service.LauncherService;
import toolbox.ui.MainFrame;

import javax.swing.SwingUtilities;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.dpiaware", "true");

        final File configFile = new File("data/toolbox-config.xml");
        final ConfigService configService = new ConfigService(configFile);
        final LauncherService launcherService = new LauncherService();
        final ToolboxConfig config = configService.load();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame.prepareLookAndFeel();
                MainFrame frame = new MainFrame(configService, launcherService, config);
                frame.setVisible(true);
            }
        });
    }
}
