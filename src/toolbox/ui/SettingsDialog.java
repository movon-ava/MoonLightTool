package toolbox.ui;

import toolbox.model.AppSettings;
import toolbox.service.LauncherService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

public class SettingsDialog extends JDialog {
    private final JTextField titleField;
    private final JTextField toolsRootField;
    private final JComboBox<String> languageBox;
    private final JPanel colorPreview;
    private final DefaultListModel<String> tagsModel;
    private final JList<String> tagsList;
    private final JTextField newTagField;
    private final JCheckBox defaultOpenBox;
    private final JCheckBox cmdOpenBox;
    private final JCheckBox powershellOpenBox;
    private final JCheckBox explorerOpenBox;
    private String selectedAccentHex;
    private final I18n i18n;
    private boolean confirmed;

    public SettingsDialog(Frame owner, AppSettings settings) {
        this(owner, settings, new I18n(settings == null ? I18n.ZH : settings.getLanguage()));
    }

    public SettingsDialog(Frame owner, AppSettings settings, I18n i18n) {
        super(owner, (i18n == null ? new I18n(I18n.ZH) : i18n).text("工具箱设置", "Settings"), true);
        this.i18n = i18n == null ? new I18n(I18n.ZH) : i18n;
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(AppTheme.BACKGROUND);

        selectedAccentHex = settings.getAccentHex();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppTheme.PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        titleField = new JTextField(settings.getAppTitle(), 24);
        addRow(form, gbc, 0, this.i18n.text("工具箱名称", "Toolbox name"), titleField);

        toolsRootField = new JTextField(settings.getToolsRootPath(), 24);
        toolsRootField.setFont(AppTheme.BODY_FONT);
        addRow(form, gbc, 1, this.i18n.text("工具目录", "Tools root"), toolsRootField);

        languageBox = new JComboBox<String>(new String[]{this.i18n.text("中文", "Chinese"), this.i18n.text("英文", "English")});
        languageBox.setSelectedIndex(I18n.EN.equalsIgnoreCase(settings.getLanguage()) ? 1 : 0);
        languageBox.setFont(AppTheme.BODY_FONT);
        addRow(form, gbc, 2, this.i18n.text("语言", "Language"), languageBox);

        JPanel colorPanel = new JPanel(new BorderLayout(8, 0));
        colorPanel.setOpaque(false);
        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(42, 42));
        colorPreview.setBackground(AppTheme.parseAccent(selectedAccentHex));
        colorPreview.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        JButton chooseColorButton = createButton(this.i18n.text("选择主题色", "Choose theme color"), new Color(219, 234, 254), new Color(30, 64, 175));
        chooseColorButton.addActionListener(e -> chooseColor());
        JLabel colorCode = new JLabel(selectedAccentHex);
        colorCode.setFont(AppTheme.BODY_FONT);
        colorCode.setForeground(AppTheme.TEXT);

        JPanel colorInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        colorInfo.setOpaque(false);
        colorInfo.add(colorPreview);
        colorInfo.add(colorCode);

        colorPanel.add(colorInfo, BorderLayout.CENTER);
        colorPanel.add(chooseColorButton, BorderLayout.EAST);
        addRow(form, gbc, 3, this.i18n.text("主题色", "Theme color"), colorPanel);

        tagsModel = new DefaultListModel<String>();
        for (String tag : settings.getAvailableTags()) {
            tagsModel.addElement(tag);
        }
        tagsList = new JList<String>(tagsModel);
        tagsList.setFont(AppTheme.BODY_FONT);
        tagsList.setVisibleRowCount(6);
        JScrollPane tagsScroll = new JScrollPane(tagsList);
        tagsScroll.setPreferredSize(new Dimension(320, 130));

        newTagField = new JTextField(18);
        JButton addTagButton = createButton(this.i18n.text("添加标签", "Add tag"), new Color(226, 232, 240), AppTheme.TEXT);
        addTagButton.addActionListener(e -> addTag());
        JButton removeTagButton = createButton(this.i18n.text("删除标签", "Remove tag"), new Color(254, 226, 226), AppTheme.DANGER);
        removeTagButton.addActionListener(e -> removeTag());

        JPanel tagInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tagInput.setOpaque(false);
        tagInput.add(newTagField);
        tagInput.add(addTagButton);
        tagInput.add(removeTagButton);

        JPanel tagPanel = new JPanel(new BorderLayout(0, 8));
        tagPanel.setOpaque(false);
        tagPanel.add(tagsScroll, BorderLayout.CENTER);
        tagPanel.add(tagInput, BorderLayout.SOUTH);
        addRow(form, gbc, 4, this.i18n.text("标签管理", "Tag management"), tagPanel);

        List<String> enabledModes = settings.getEnabledLaunchModes();
        defaultOpenBox = new JCheckBox(this.i18n.text("默认打开", "Open"), enabledModes.contains(LauncherService.LaunchMode.DEFAULT.name()));
        cmdOpenBox = new JCheckBox(this.i18n.text("CMD打开", "Open with CMD"), enabledModes.contains(LauncherService.LaunchMode.CMD.name()));
        powershellOpenBox = new JCheckBox(this.i18n.text("PowerShell打开", "Open with PowerShell"), enabledModes.contains(LauncherService.LaunchMode.POWERSHELL.name()));
        explorerOpenBox = new JCheckBox(this.i18n.text("资源管理器", "Explorer"), enabledModes.contains(LauncherService.LaunchMode.EXPLORER.name()));

        JPanel modePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        modePanel.setOpaque(false);
        modePanel.add(styleCheckBox(defaultOpenBox));
        modePanel.add(styleCheckBox(cmdOpenBox));
        modePanel.add(styleCheckBox(powershellOpenBox));
        modePanel.add(styleCheckBox(explorerOpenBox));
        addRow(form, gbc, 5, this.i18n.text("打开方式", "Launch modes"), modePanel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(AppTheme.BACKGROUND);
        JButton cancelButton = createButton(this.i18n.text("取消", "Cancel"), new Color(241, 245, 249), AppTheme.TEXT);
        JButton saveButton = createButton(this.i18n.text("保存", "Save"), new Color(219, 234, 254), new Color(30, 64, 175));
        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, this.i18n.text("工具箱名称不能为空", "Toolbox name cannot be empty"), this.i18n.text("提示", "Notice"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!defaultOpenBox.isSelected() && !cmdOpenBox.isSelected() && !powershellOpenBox.isSelected() && !explorerOpenBox.isSelected()) {
                JOptionPane.showMessageDialog(this, this.i18n.text("至少保留一种打开方式", "Keep at least one launch mode enabled"), this.i18n.text("提示", "Notice"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            confirmed = true;
            dispose();
        });

        actions.add(cancelButton);
        actions.add(saveButton);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private JCheckBox styleCheckBox(JCheckBox box) {
        box.setOpaque(false);
        box.setFont(AppTheme.BODY_FONT.deriveFont(Font.BOLD, 15f));
        box.setForeground(AppTheme.TEXT);
        return box;
    }

    private void chooseColor() {
        Color color = JColorChooser.showDialog(this, i18n.text("选择主题色", "Choose theme color"), AppTheme.parseAccent(selectedAccentHex));
        if (color != null) {
            selectedAccentHex = AppTheme.toHex(color);
            colorPreview.setBackground(color);
        }
    }

    private void addTag() {
        String tag = newTagField.getText().trim();
        if (tag.isEmpty()) {
            return;
        }
        for (int i = 0; i < tagsModel.size(); i++) {
            if (tag.equals(tagsModel.get(i))) {
                newTagField.setText("");
                return;
            }
        }
        tagsModel.addElement(tag);
        newTagField.setText("");
    }

    private void removeTag() {
        int index = tagsList.getSelectedIndex();
        if (index >= 0) {
            tagsModel.remove(index);
        }
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        form.add(createLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(component, gbc);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.BODY_FONT.deriveFont(Font.BOLD, 15f));
        label.setForeground(AppTheme.TEXT);
        return label;
    }

    private JButton createButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(AppTheme.BODY_FONT.deriveFont(Font.BOLD, 15f));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254)),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)));
        return button;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getTitleValue() {
        return titleField.getText().trim();
    }

    public String getLanguage() {
        return languageBox.getSelectedIndex() == 1 ? I18n.EN : I18n.ZH;
    }

    public String getToolsRootPath() {
        return toolsRootField.getText().trim();
    }

    public String getAccentHex() {
        return selectedAccentHex;
    }

    public List<String> getTags() {
        List<String> tags = new ArrayList<String>();
        for (int i = 0; i < tagsModel.size(); i++) {
            tags.add(tagsModel.get(i));
        }
        return tags;
    }

    public List<String> getEnabledModes() {
        List<String> modes = new ArrayList<String>();
        if (defaultOpenBox.isSelected()) {
            modes.add(LauncherService.LaunchMode.DEFAULT.name());
        }
        if (cmdOpenBox.isSelected()) {
            modes.add(LauncherService.LaunchMode.CMD.name());
        }
        if (powershellOpenBox.isSelected()) {
            modes.add(LauncherService.LaunchMode.POWERSHELL.name());
        }
        if (explorerOpenBox.isSelected()) {
            modes.add(LauncherService.LaunchMode.EXPLORER.name());
        }
        return modes;
    }
}
