package toolbox.ui;

import toolbox.model.LauncherType;
import toolbox.model.NodeType;
import toolbox.model.ToolKind;
import toolbox.model.ToolboxNode;
import toolbox.service.ToolTemplateService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class EntryEditorDialog extends JDialog {
    private static final File DEFAULT_TOOLS_ROOT = new File("tools").getAbsoluteFile();
    private static final String[] RANDOM_COLORS = {
            "#1F6FEB", "#F59E0B", "#10B981", "#EF4444", "#7C3AED", "#0EA5E9", "#EC4899", "#64748B"
    };
    private static final String[] RANDOM_ICONS = {
            "terminal", "archive", "star", "book", "moon", "globe"
    };

    private final JTextField nameField;
    private final JComboBox<NodeType> typeBox;
    private final JComboBox<ToolKind> kindBox;
    private final JTextField targetField;
    private final JComboBox<String> tagsBox;
    private final JComboBox<LauncherType> launcherTypeBox;
    private final JTextField launcherPathField;
    private final JTextField launchArgumentsField;
    private final JTextField workingDirectoryField;
    private final JTextField setupCommandField;
    private final JTextArea descriptionArea;
    private final boolean folderMode;
    private final ToolTemplateService toolTemplateService;
    private final I18n i18n;
    private JPanel customLauncherPanel;
    private boolean confirmed;
    private String randomColorHex;
    private String randomIconKey;

    public EntryEditorDialog(Frame owner, String title, ToolboxNode source, boolean folderMode,
                             List<String> availableTags) {
        this(owner, title, source, folderMode, availableTags, new I18n(I18n.ZH), DEFAULT_TOOLS_ROOT);
    }

    public EntryEditorDialog(Frame owner, String title, ToolboxNode source, boolean folderMode,
                             List<String> availableTags, I18n i18n) {
        this(owner, title, source, folderMode, availableTags, i18n, DEFAULT_TOOLS_ROOT);
    }

    public EntryEditorDialog(Frame owner, String title, ToolboxNode source, boolean folderMode,
                             List<String> availableTags, File toolsRootDirectory) {
        this(owner, title, source, folderMode, availableTags, new I18n(I18n.ZH), toolsRootDirectory);
    }

    public EntryEditorDialog(Frame owner, String title, ToolboxNode source, boolean folderMode,
                             List<String> availableTags, I18n i18n, File toolsRootDirectory) {
        super(owner, title, true);
        this.folderMode = folderMode;
        this.i18n = i18n == null ? new I18n(I18n.ZH) : i18n;
        this.toolTemplateService = new ToolTemplateService(toolsRootDirectory);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(AppTheme.BACKGROUND);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppTheme.PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        nameField = new JTextField(source == null ? "" : source.getName(), 24);
        typeBox = new JComboBox<NodeType>(new NodeType[]{NodeType.TOOL, NodeType.WEB, NodeType.DOCUMENT});
        typeBox.setSelectedItem(resolveType(source));
        typeBox.setRenderer(new TypeRenderer());

        kindBox = new JComboBox<ToolKind>(ToolKind.values());
        kindBox.setSelectedItem(resolveKind(source));
        kindBox.setRenderer(new KindRenderer());

        targetField = new JTextField(source == null ? "" : source.getTarget(), 24);
        tagsBox = new JComboBox<String>(availableTags.toArray(new String[0]));
        tagsBox.setEditable(false);
        tagsBox.setSelectedItem(resolveTag(source == null ? "" : source.getTags(), availableTags));

        launcherTypeBox = new JComboBox<LauncherType>(LauncherType.values());
        launcherTypeBox.setSelectedItem(source == null ? LauncherType.SYSTEM_OPEN : source.getLauncherType());
        launcherTypeBox.setRenderer(new LauncherRenderer());

        launcherPathField = new JTextField(source == null ? "" : source.getLauncherPath(), 24);
        launchArgumentsField = new JTextField(source == null ? "" : source.getLaunchArguments(), 24);
        workingDirectoryField = new JTextField(source == null ? "" : source.getWorkingDirectory(), 24);
        setupCommandField = new JTextField(source == null ? "" : source.getSetupCommand(), 24);

        descriptionArea = new JTextArea(source == null ? "" : source.getDescription(), 5, 24);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(AppTheme.BODY_FONT);

        randomColorHex = isBlank(source == null ? null : source.getColorHex()) ? randomFrom(RANDOM_COLORS) : source.getColorHex();
        randomIconKey = isBlank(source == null ? null : source.getIconKey()) ? randomIcon(resolveType(source)) : source.getIconKey();

        addRow(form, gbc, 0, this.i18n.text("名称", "Name"), nameField);

        if (!folderMode) {
            addRow(form, gbc, 1, this.i18n.text("类型", "Type"), typeBox);
            addRow(form, gbc, 2, this.i18n.text("分类", "Kind"), kindBox);
            addRow(form, gbc, 3, this.i18n.text("目标", "Target"), createTargetSelector());
            addRow(form, gbc, 4, this.i18n.text("标签", "Tag"), tagsBox);
            addRow(form, gbc, 5, this.i18n.text("启动方式", "Launch mode"), launcherTypeBox);
            addRow(form, gbc, 6, this.i18n.text("命令", "Command"), createCustomLauncherPanel());
            addRow(form, gbc, 7, this.i18n.text("安装配置命令", "Setup command"), setupCommandField);
            addRow(form, gbc, 8, this.i18n.text("模板", "Template"), createTemplatePanel());

            JLabel randomHint = createLabel(this.i18n.text("颜色 / 图标", "Color / icon"));
            gbc.gridx = 0;
            gbc.gridy = 9;
            gbc.weightx = 0;
            form.add(randomHint, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            form.add(createRandomInfoPanel(), gbc);
        } else {
            JLabel folderHint = new JLabel(this.i18n.text("分类节点不使用目标路径或启动方式设置。", "Category nodes do not use target paths or launch settings."));
            folderHint.setFont(AppTheme.BODY_FONT);
            folderHint.setForeground(AppTheme.SUBTEXT);
            addRow(form, gbc, 1, this.i18n.text("说明", "Note"), folderHint);
        }

        gbc.gridx = 0;
        gbc.gridy = folderMode ? 2 : 10;
        gbc.weightx = 0;
        gbc.weighty = 0;
        form.add(createLabel(this.i18n.text("备注", "Notes")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setPreferredSize(new Dimension(360, 120));
        form.add(scrollPane, gbc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(AppTheme.BACKGROUND);
        JButton cancelButton = createButton(this.i18n.text("取消", "Cancel"), new Color(241, 245, 249), AppTheme.TEXT);
        JButton confirmButton = createButton(this.i18n.text("保存", "Save"), new Color(219, 234, 254), new Color(30, 64, 175));

        cancelButton.addActionListener(e -> dispose());
        confirmButton.addActionListener(e -> confirm());

        launcherTypeBox.addActionListener(e -> updateLauncherPanelState());
        kindBox.addActionListener(e -> updateKindState());
        typeBox.addActionListener(e -> {
            randomIconKey = randomIcon((NodeType) typeBox.getSelectedItem());
            updateTypeState();
        });
        updateTypeState();
        updateLauncherPanelState();

        actions.add(cancelButton);
        actions.add(confirmButton);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void confirm() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, i18n.text("名称不能为空", "Name cannot be empty"), i18n.text("提示", "Notice"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!folderMode && targetField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, i18n.text("目标不能为空", "Target cannot be empty"), i18n.text("提示", "Notice"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!folderMode && launcherTypeBox.getSelectedItem() == LauncherType.CUSTOM_COMMAND
                && launcherPathField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    i18n.text("选择自定义命令时，启动程序路径不能为空", "Launcher path cannot be empty when using a custom command"),
                    i18n.text("提示", "Notice"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        confirmed = true;
        dispose();
    }

    private NodeType resolveType(ToolboxNode source) {
        if (source == null || source.getType() == null || source.getType() == NodeType.FOLDER || source.getType() == NodeType.ROOT) {
            return NodeType.TOOL;
        }
        return source.getType();
    }

    private ToolKind resolveKind(ToolboxNode source) {
        if (source == null || source.getType() == NodeType.FOLDER || source.getType() == NodeType.ROOT) {
            return ToolKind.BUILTIN;
        }
        return source.getToolKind();
    }

    private String resolveTag(String tag, List<String> availableTags) {
        if (availableTags == null || availableTags.isEmpty()) {
            return "";
        }
        if (isBlank(tag)) {
            return availableTags.get(0);
        }
        for (String option : availableTags) {
            if (option.equals(tag)) {
                return option;
            }
        }
        return availableTags.get(0);
    }

    private JPanel createTargetSelector() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);

        targetField.setFont(AppTheme.BODY_FONT);
        JButton browseButton = createButton(i18n.text("浏览", "Browse"), new Color(226, 232, 240), AppTheme.TEXT);
        browseButton.addActionListener(e -> chooseTarget());

        panel.add(targetField, BorderLayout.CENTER);
        panel.add(browseButton, BorderLayout.EAST);
        return panel;
    }

    private JPanel createCustomLauncherPanel() {
        customLauncherPanel = new JPanel(new GridBagLayout());
        customLauncherPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        customLauncherPanel.add(createFieldWithButton(launcherPathField, i18n.text("浏览", "Browse"), e -> chooseLauncherPath()), gbc);

        gbc.gridy = 1;
        launchArgumentsField.setFont(AppTheme.BODY_FONT);
        customLauncherPanel.add(launchArgumentsField, gbc);

        gbc.gridy = 2;
        customLauncherPanel.add(createFieldWithButton(workingDirectoryField, i18n.text("工作目录", "Working directory"), e -> chooseWorkingDirectory()), gbc);

        gbc.gridy = 3;
        JLabel hint = new JLabel(i18n.text("参数支持占位符：{target} {name} {launcher}", "Arguments support placeholders: {target} {name} {launcher}"));
        hint.setFont(AppTheme.SMALL_FONT);
        hint.setForeground(AppTheme.SUBTEXT);
        customLauncherPanel.add(hint, gbc);
        return customLauncherPanel;
    }

    private JPanel createTemplatePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);

        JTextField toolsRootField = new JTextField(toolTemplateService.getToolsRootDirectory().getAbsolutePath(), 24);
        toolsRootField.setEditable(false);
        toolsRootField.setFont(AppTheme.BODY_FONT);

        JButton createButton = createButton(i18n.text("创建模板", "Create template"), new Color(226, 232, 240), AppTheme.TEXT);
        createButton.addActionListener(e -> createTemplateDirectory());

        panel.add(toolsRootField, BorderLayout.CENTER);
        panel.add(createButton, BorderLayout.EAST);
        return panel;
    }

    private JPanel createFieldWithButton(JTextField field, String buttonText, java.awt.event.ActionListener action) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        field.setFont(AppTheme.BODY_FONT);
        JButton button = createButton(buttonText, new Color(226, 232, 240), AppTheme.TEXT);
        button.addActionListener(action);
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    private JPanel createRandomInfoPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        colorPanel.setOpaque(false);
        JPanel colorSwatch = new JPanel();
        colorSwatch.setPreferredSize(new Dimension(18, 18));
        colorSwatch.setBackground(AppTheme.parseAccent(randomColorHex));
        colorSwatch.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        JLabel colorLabel = new JLabel(i18n.text("颜色：", "Color: ") + randomColorHex);
        colorLabel.setFont(AppTheme.BODY_FONT);
        colorLabel.setForeground(AppTheme.TEXT);
        colorPanel.add(colorSwatch);
        colorPanel.add(colorLabel);

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        iconPanel.setOpaque(false);
        JLabel iconPreview = new JLabel(new javax.swing.ImageIcon(
                IconFactory.createNodeIcon(buildPreviewNode(), AppTheme.parseAccent(randomColorHex), 24)));
        JLabel iconLabel = new JLabel(i18n.text("图标：", "Icon: ") + randomIconKey);
        iconLabel.setFont(AppTheme.BODY_FONT);
        iconLabel.setForeground(AppTheme.TEXT);
        iconPanel.add(iconPreview);
        iconPanel.add(iconLabel);

        JButton rerollButton = createButton(i18n.text("换一组", "Reroll"), new Color(226, 232, 240), AppTheme.TEXT);
        rerollButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        rerollButton.addActionListener(e -> {
            randomColorHex = randomFrom(RANDOM_COLORS);
            randomIconKey = randomIcon((NodeType) typeBox.getSelectedItem());
            colorSwatch.setBackground(AppTheme.parseAccent(randomColorHex));
            colorLabel.setText(i18n.text("颜色：", "Color: ") + randomColorHex);
            iconPreview.setIcon(new javax.swing.ImageIcon(
                    IconFactory.createNodeIcon(buildPreviewNode(), AppTheme.parseAccent(randomColorHex), 24)));
            iconLabel.setText(i18n.text("图标：", "Icon: ") + randomIconKey);
        });

        typeBox.addActionListener(e -> {
            iconPreview.setIcon(new javax.swing.ImageIcon(
                    IconFactory.createNodeIcon(buildPreviewNode(), AppTheme.parseAccent(randomColorHex), 24)));
            iconLabel.setText(i18n.text("图标：", "Icon: ") + randomIconKey);
        });
        kindBox.addActionListener(e -> iconPreview.setIcon(new javax.swing.ImageIcon(
                IconFactory.createNodeIcon(buildPreviewNode(), AppTheme.parseAccent(randomColorHex), 24))));

        panel.add(colorPanel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(iconPanel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(rerollButton);
        return panel;
    }

    private void createTemplateDirectory() {
        try {
            ToolboxNode draft = new ToolboxNode();
            fillNode(draft);
            File templateDir = toolTemplateService.createTemplate(draft);
            workingDirectoryField.setText(templateDir.getAbsolutePath());
            if (draft.getType() == NodeType.TOOL && isBlank(targetField.getText())) {
                targetField.setText(templateDir.getAbsolutePath());
            }
            JOptionPane.showMessageDialog(this, i18n.text("模板已创建：\n", "Template created:\n") + templateDir.getAbsolutePath(),
                    i18n.text("模板", "Template"), JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), i18n.text("模板创建失败", "Template creation failed"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private ToolboxNode buildPreviewNode() {
        ToolboxNode node = new ToolboxNode();
        node.setType((NodeType) typeBox.getSelectedItem());
        node.setToolKind((ToolKind) kindBox.getSelectedItem());
        node.setIconKey(randomIconKey);
        return node;
    }

    private String randomIcon(NodeType type) {
        if (type == NodeType.WEB) {
            return "globe";
        }
        if (type == NodeType.DOCUMENT) {
            return "book";
        }
        return RANDOM_ICONS[new Random().nextInt(RANDOM_ICONS.length)];
    }

    private void chooseTarget() {
        NodeType type = (NodeType) typeBox.getSelectedItem();
        if (type == NodeType.WEB) {
            String input = JOptionPane.showInputDialog(this, i18n.text("请输入 URL", "Enter URL"), targetField.getText().trim());
            if (input != null) {
                targetField.setText(input.trim());
            }
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(i18n.text("选择目标", "Select target"));
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setAcceptAllFileFilterUsed(true);
        setChooserInitialPath(chooser, targetField.getText());
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            targetField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void chooseLauncherPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(i18n.text("选择启动程序", "Select launcher"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(true);
        setChooserInitialPath(chooser, launcherPathField.getText());
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            launcherPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void chooseWorkingDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(i18n.text("选择工作目录", "Select working directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        setChooserInitialPath(chooser, workingDirectoryField.getText());
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            workingDirectoryField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void setChooserInitialPath(JFileChooser chooser, String pathValue) {
        if (isBlank(pathValue)) {
            return;
        }
        File file = new File(pathValue.trim());
        File initial = file.exists() ? file : file.getParentFile();
        if (initial != null && initial.exists()) {
            chooser.setCurrentDirectory(initial.isDirectory() ? initial : initial.getParentFile());
            if (file.exists()) {
                chooser.setSelectedFile(file);
            }
        }
    }

    private void updateTypeState() {
        NodeType type = (NodeType) typeBox.getSelectedItem();
        if (type == NodeType.WEB) {
            kindBox.setSelectedItem(ToolKind.WEB);
        } else if (kindBox.getSelectedItem() == ToolKind.WEB) {
            kindBox.setSelectedItem(ToolKind.BUILTIN);
        }
        updateKindState();
    }

    private void updateKindState() {
        ToolKind kind = (ToolKind) kindBox.getSelectedItem();
        if (kind == ToolKind.WEB && typeBox.getSelectedItem() != NodeType.WEB) {
            typeBox.setSelectedItem(NodeType.WEB);
            return;
        }
        if (kind != ToolKind.WEB && typeBox.getSelectedItem() == NodeType.WEB) {
            typeBox.setSelectedItem(NodeType.TOOL);
            return;
        }

        boolean installable = kind == ToolKind.INSTALLABLE || kind == ToolKind.PLUGIN;
        setupCommandField.setEnabled(installable);
        if (!installable) {
            setupCommandField.setText("");
        }
        updateLauncherPanelState();
    }

    private void updateLauncherPanelState() {
        boolean web = (NodeType) typeBox.getSelectedItem() == NodeType.WEB;
        boolean custom = launcherTypeBox.getSelectedItem() == LauncherType.CUSTOM_COMMAND;

        if (web) {
            launcherTypeBox.setEnabled(false);
            launcherTypeBox.setSelectedItem(LauncherType.SYSTEM_OPEN);
        } else {
            launcherTypeBox.setEnabled(true);
        }

        if (customLauncherPanel != null) {
            customLauncherPanel.setVisible(!web && custom);
        }
        pack();
    }

    private String randomFrom(String[] values) {
        return values[new Random().nextInt(values.length)];
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void fillNode(ToolboxNode node) {
        node.setName(nameField.getText().trim());
        node.setDescription(descriptionArea.getText().trim());
        if (!folderMode) {
            NodeType selectedType = (NodeType) typeBox.getSelectedItem();
            ToolKind selectedKind = (ToolKind) kindBox.getSelectedItem();
            node.setType(selectedType);
            node.setToolKind(selectedKind);
            node.setTarget(targetField.getText().trim());
            Object selectedTag = tagsBox.getSelectedItem();
            node.setTags(selectedTag == null ? "" : selectedTag.toString());
            node.setColorHex(randomColorHex);
            node.setIconKey(randomIconKey);
            LauncherType launcherType = selectedType == NodeType.WEB
                    ? LauncherType.SYSTEM_OPEN
                    : (LauncherType) launcherTypeBox.getSelectedItem();
            node.setLauncherType(launcherType);
            node.setLauncherPath(customOrEmpty(launcherType, launcherPathField.getText()));
            node.setLaunchArguments(customOrEmpty(launcherType, launchArgumentsField.getText()));
            node.setWorkingDirectory(customOrEmpty(launcherType, workingDirectoryField.getText()));
            node.setSetupCommand(setupCommandField.getText().trim());
        }
    }

    private String customOrEmpty(LauncherType launcherType, String value) {
        if (launcherType != LauncherType.CUSTOM_COMMAND) {
            return "";
        }
        return value == null ? "" : value.trim();
    }

    private final class TypeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == NodeType.TOOL) {
                setText(i18n.typeName(NodeType.TOOL));
            } else if (value == NodeType.WEB) {
                setText(i18n.typeName(NodeType.WEB));
            } else if (value == NodeType.DOCUMENT) {
                setText(i18n.typeName(NodeType.DOCUMENT));
            }
            return this;
        }
    }

    private final class KindRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ToolKind) {
                setText(i18n.toolKind((ToolKind) value));
            }
            return this;
        }
    }

    private final class LauncherRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof LauncherType) {
                setText(i18n.launcherType((LauncherType) value));
            }
            return this;
        }
    }
}
