package toolbox.ui;

import toolbox.model.AppSettings;
import toolbox.model.LauncherType;
import toolbox.model.NodeType;
import toolbox.model.ToolKind;
import toolbox.model.ToolboxConfig;
import toolbox.model.ToolboxNode;
import toolbox.service.ConfigService;
import toolbox.service.LauncherService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private static final String[] PRESET_COLORS = {
            "#1F6FEB", "#F59E0B", "#10B981", "#EF4444", "#7C3AED", "#0EA5E9", "#EC4899", "#64748B"
    };
    private static final String[] ICON_KEYS = {
            "", "terminal", "globe", "archive", "star", "book", "moon"
    };

    private final ConfigService configService;
    private final LauncherService launcherService;
    private final DefaultListModel<ToolboxNode> entryListModel;
    private final Object saveLock = new Object();
    private ToolboxConfig config;
    private I18n i18n;
    private JTree tree;
    private DefaultTreeModel treeModel;
    private JList<ToolboxNode> entryList;
    private JTextField searchField;
    private JLabel titleValue;
    private JLabel typeValue;
    private JLabel kindValue;
    private JLabel targetValue;
    private JLabel tagValue;
    private JLabel pathValue;
    private JLabel launcherValue;
    private JTextArea descValue;
    private JLabel statusLabel;
    private JLabel heroTitle;
    private JLabel heroSubtitle;
    private JLabel countValue;
    private JLabel listTitle;
    private JLabel listSubtitle;
    private JLabel quickHintLabel;
    private JButton openButton;
    private JButton cmdButton;
    private JButton powerShellButton;
    private JButton locateButton;
    private JButton copyButton;
    private JButton editButton;
    private JButton deleteButton;
    private Color accentColor;
    private ToolboxNode currentCategory;
    private ToolboxNode detailNode;

    public MainFrame(ConfigService configService, LauncherService launcherService, ToolboxConfig config) {
        this.configService = configService;
        this.launcherService = launcherService;
        this.config = config;
        this.entryListModel = new DefaultListModel<ToolboxNode>();
        normalizeSettings();
        this.i18n = new I18n(config.getSettings().getLanguage());
        this.accentColor = AppTheme.parseAccent(config.getSettings().getAccentHex());

        setTitle(config.getSettings().getAppTitle());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1400, 840));
        setAppIcons();
        setContentPane(buildContent());
        refreshTree(null);
        selectInitialCategory();
        setLocationRelativeTo(null);
    }

    private void normalizeSettings() {
        if (config.getSettings() == null) {
            config.setSettings(new AppSettings());
        }
        AppSettings settings = config.getSettings();
        if (isBlank(settings.getLanguage())) {
            settings.setLanguage(I18n.ZH);
        }
        I18n texts = new I18n(settings.getLanguage());
        if (isBlank(settings.getAppTitle()) || settings.getAppTitle().contains("FoxShelf")
                || isDefaultTitleForAnotherLanguage(settings.getAppTitle(), settings.getLanguage())) {
            settings.setAppTitle(texts.appTitle());
        }
        if (isBlank(settings.getWelcomeText())
                || isDefaultWelcomeForAnotherLanguage(settings.getWelcomeText(), settings.getLanguage())) {
            settings.setWelcomeText(texts.welcomeText());
        }
        if (isBlank(settings.getAccentHex())) {
            settings.setAccentHex("#1F6FEB");
        }
        if (settings.getAvailableTags().isEmpty()) {
            settings.getAvailableTags().add(texts.text("常用", "common"));
        }
        if (settings.getEnabledLaunchModes().isEmpty()) {
            settings.getEnabledLaunchModes().add(LauncherService.LaunchMode.DEFAULT.name());
        }
    }

    private JPanel buildContent() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 16));
        rootPanel.setBackground(AppTheme.BACKGROUND);
        rootPanel.setBorder(new EmptyBorder(14, 14, 14, 14));
        rootPanel.add(buildHeroPanel(), BorderLayout.NORTH);
        rootPanel.add(buildWorkbench(), BorderLayout.CENTER);
        rootPanel.add(buildFooter(), BorderLayout.SOUTH);
        return rootPanel;
    }

    private Component buildHeroPanel() {
        GradientPanel hero = new GradientPanel(new Color(15, 23, 42), new Color(30, 64, 175), new Color(56, 189, 248));
        hero.setLayout(new BorderLayout(18, 0));
        hero.setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("MOONLIGHT TOOLBOX");
        eyebrow.setFont(AppTheme.SMALL_FONT.deriveFont(Font.BOLD, 13f));
        eyebrow.setForeground(new Color(219, 234, 254));

        heroTitle = new JLabel(config.getSettings().getAppTitle());
        heroTitle.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 34));
        heroTitle.setForeground(Color.WHITE);

        heroSubtitle = new JLabel(config.getSettings().getWelcomeText());
        heroSubtitle.setFont(AppTheme.BODY_FONT.deriveFont(16f));
        heroSubtitle.setForeground(new Color(226, 232, 240));

        left.add(eyebrow);
        left.add(Box.createVerticalStrut(8));
        left.add(heroTitle);
        left.add(Box.createVerticalStrut(8));
        left.add(heroSubtitle);

        JPanel right = new JPanel(new GridLayout(1, 2, 12, 0));
        right.setOpaque(false);
        right.add(createHeroStatCard(i18n.text("内容总数", "Entries"), "0"));
        right.add(createHeroLanguagePanel());

        hero.add(left, BorderLayout.WEST);
        hero.add(buildHeroActions(), BorderLayout.SOUTH);
        hero.add(right, BorderLayout.EAST);
        return hero;
    }

    private Component buildHeroActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        panel.setOpaque(false);
        panel.add(createPrimaryHeroButton(i18n.text("新建分类", "New category"), e -> addFolder()));
        panel.add(createPrimaryHeroButton(i18n.text("新建内容", "New entry"), e -> addContent(NodeType.TOOL)));
        panel.add(createSecondaryHeroButton(i18n.text("工具箱设置", "Settings"), e -> editSettings()));
        return panel;
    }

    private JPanel createHeroLanguagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JButton button = createSecondaryHeroButton(i18n.isChinese() ? "EN" : "中", e -> toggleLanguage());
        button.setToolTipText(i18n.text("切换到英文", "Switch to Chinese"));
        panel.add(button, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createHeroStatCard(String label, String value) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 56)),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel top = new JLabel(label);
        top.setFont(AppTheme.SMALL_FONT);
        top.setForeground(new Color(191, 219, 254));

        JLabel bottom = new JLabel(value);
        bottom.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 28));
        bottom.setForeground(Color.WHITE);

        if (i18n.text("内容总数", "Entries").equals(label)) {
            countValue = bottom;
        }

        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);
        return card;
    }

    private Component buildWorkbench() {
        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSplit.setBorder(null);
        rightSplit.setContinuousLayout(true);
        rightSplit.setResizeWeight(0.42d);
        rightSplit.setDividerLocation(440);
        rightSplit.setLeftComponent(buildEntryPanel());
        rightSplit.setRightComponent(buildPreview());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.22d);
        splitPane.setDividerLocation(300);
        splitPane.setLeftComponent(buildNavigator());
        splitPane.setRightComponent(rightSplit);
        return splitPane;
    }

    private Component buildNavigator() {
        JPanel shell = createCardPanel();
        shell.setLayout(new BorderLayout(0, 14));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);

        JLabel title = new JLabel(i18n.text("分类标签", "Category tags"));
        title.setFont(AppTheme.HEADING_FONT);
        title.setForeground(AppTheme.TEXT);

        quickHintLabel = new JLabel(i18n.text("左侧只显示分类，双击中间分类可进入下一层。", "The left side shows categories only. Double-click a middle category to enter it."));
        quickHintLabel.setFont(AppTheme.SMALL_FONT);
        quickHintLabel.setForeground(AppTheme.SUBTEXT);

        searchField = new JTextField();
        searchField.setFont(AppTheme.BODY_FONT);
        searchField.setBackground(AppTheme.PANEL_SOFT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(217, 226, 238)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshTree(searchField.getText().trim());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshTree(searchField.getText().trim());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshTree(searchField.getText().trim());
            }
        });

        JPanel labelWrap = new JPanel();
        labelWrap.setOpaque(false);
        labelWrap.setLayout(new BoxLayout(labelWrap, BoxLayout.Y_AXIS));
        labelWrap.add(title);
        labelWrap.add(Box.createVerticalStrut(4));
        labelWrap.add(quickHintLabel);

        top.add(labelWrap, BorderLayout.NORTH);
        top.add(searchField, BorderLayout.SOUTH);

        tree = new JTree();
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setBackground(AppTheme.PANEL);
        tree.setRowHeight(34);
        tree.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        tree.setCellRenderer(new ToolboxTreeCellRenderer(accentColor));
        tree.setDragEnabled(true);
        tree.setDropMode(javax.swing.DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new NodeTransferHandler());
        tree.addTreeSelectionListener(this::onCategorySelected);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        tree.setSelectionPath(path);
                        showTreePopup(e.getX(), e.getY());
                    }
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setBorder(null);
        treeScroll.getViewport().setBackground(AppTheme.PANEL);

        shell.add(top, BorderLayout.NORTH);
        shell.add(treeScroll, BorderLayout.CENTER);
        shell.add(buildNavigatorFooter(), BorderLayout.SOUTH);
        return shell;
    }

    private Component buildNavigatorFooter() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setOpaque(false);
        panel.add(createGhostButton(i18n.text("展开全部", "Expand all"), e -> expandAll()));
        panel.add(createGhostButton(i18n.text("打开配置目录", "Open config"), e -> openConfigFolder()));
        return panel;
    }

    private Component buildEntryPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        listTitle = new JLabel(i18n.text("内容列表", "Entry list"));
        listTitle.setFont(AppTheme.HEADING_FONT);
        listTitle.setForeground(AppTheme.TEXT);

        listSubtitle = new JLabel(i18n.text("选择左侧分类后，这里展示该分类下的内容。", "Select a category on the left to show its entries here."));
        listSubtitle.setFont(AppTheme.SMALL_FONT);
        listSubtitle.setForeground(AppTheme.SUBTEXT);

        header.add(listTitle);
        header.add(Box.createVerticalStrut(4));
        header.add(listSubtitle);

        entryList = new JList<ToolboxNode>(entryListModel);
        entryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        entryList.setCellRenderer(new EntryListCellRenderer());
        entryList.setBackground(AppTheme.PANEL);
        entryList.setSelectionBackground(new Color(239, 246, 255));
        entryList.setSelectionForeground(AppTheme.TEXT);
        entryList.setFixedCellHeight(82);
        entryList.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        entryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onEntrySelected(entryList.getSelectedValue());
            }
        });
        entryList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int index = entryList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    entryList.setSelectedIndex(index);
                }
                if (SwingUtilities.isRightMouseButton(e) && index >= 0) {
                    showEntryPopup(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ToolboxNode node = entryList.getSelectedValue();
                    if (node != null && node.isContainer()) {
                        selectCategoryById(node.getId());
                    } else {
                        openNode(node, LauncherService.LaunchMode.DEFAULT);
                    }
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(entryList);
        listScroll.setBorder(null);
        listScroll.getViewport().setBackground(AppTheme.PANEL);

        JPanel footer = new JPanel(new GridLayout(1, 3, 10, 0));
        footer.setOpaque(false);
        footer.add(createGhostButton(i18n.text("新建子分类", "New subcategory"), e -> addFolder()));
        footer.add(createGhostButton(i18n.text("新建内容", "New entry"), e -> addContent(NodeType.TOOL)));
        footer.add(createGhostButton(i18n.text("编辑当前项", "Edit selected"), e -> editSelectedNode()));

        panel.add(header, BorderLayout.NORTH);
        panel.add(listScroll, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private Component buildPreview() {
        JPanel wrap = createCardPanel();
        wrap.setLayout(new BorderLayout(0, 16));

        JPanel textWrap = new JPanel();
        textWrap.setOpaque(false);
        textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));

        titleValue = new JLabel();
        titleValue.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 30));
        titleValue.setForeground(AppTheme.TEXT);
        kindValue = createKindRow("");
        launcherValue = createInfoRow(i18n.text("安装配置", "Setup"), "");

        typeValue = createInfoRow(i18n.text("类型", "Type"), "");
        targetValue = createInfoRow(i18n.text("目标", "Target"), "");
        pathValue = createInfoRow(i18n.text("位置", "Location"), "");
        tagValue = createInfoRow(i18n.text("标签", "Tag"), "");

        textWrap.add(titleValue);
        textWrap.add(Box.createVerticalStrut(8));
        textWrap.add(typeValue);
        textWrap.add(kindValue);
        textWrap.add(targetValue);
        textWrap.add(pathValue);
        textWrap.add(launcherValue);

        JPanel actionWrap = new JPanel(new BorderLayout(0, 10));
        actionWrap.setOpaque(false);

        JPanel openActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        openActions.setOpaque(false);
        openButton = createActionButton(i18n.text("默认打开", "Open"), new Color(219, 234, 254), new Color(30, 64, 175), e -> openNode(detailNode, LauncherService.LaunchMode.DEFAULT));
        cmdButton = createActionButton(i18n.text("CMD打开", "CMD"), new Color(226, 232, 240), AppTheme.TEXT, e -> openNode(detailNode, LauncherService.LaunchMode.CMD));
        powerShellButton = createActionButton("PowerShell", new Color(226, 232, 240), AppTheme.TEXT, e -> openNode(detailNode, LauncherService.LaunchMode.POWERSHELL));
        locateButton = createActionButton(i18n.text("定位文件", "Locate"), new Color(226, 232, 240), AppTheme.TEXT, e -> openNode(detailNode, LauncherService.LaunchMode.EXPLORER));
        copyButton = createActionButton(i18n.text("复制目标", "Copy"), new Color(226, 232, 240), AppTheme.TEXT, e -> copyTarget(detailNode));
        openActions.add(openButton);
        openActions.add(cmdButton);
        openActions.add(powerShellButton);
        openActions.add(locateButton);
        openActions.add(copyButton);

        JPanel manageActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        manageActions.setOpaque(false);
        editButton = createActionButton(i18n.text("编辑", "Edit"), new Color(226, 232, 240), AppTheme.TEXT, e -> editSelectedNode());
        deleteButton = createActionButton(i18n.text("删除", "Delete"), new Color(254, 226, 226), AppTheme.DANGER, e -> removeSelectedNode());
        manageActions.add(editButton);
        manageActions.add(deleteButton);

        actionWrap.add(openActions, BorderLayout.NORTH);

        JPanel headerWrap = new JPanel(new BorderLayout(0, 14));
        headerWrap.setOpaque(false);
        headerWrap.add(textWrap, BorderLayout.NORTH);
        headerWrap.add(actionWrap, BorderLayout.CENTER);

        descValue = new JTextArea();
        descValue.setEditable(false);
        descValue.setLineWrap(true);
        descValue.setWrapStyleWord(true);
        descValue.setFont(AppTheme.BODY_FONT.deriveFont(15.5f));
        descValue.setForeground(AppTheme.SUBTEXT);
        descValue.setBackground(new Color(244, 247, 251));
        descValue.setBorder(new EmptyBorder(16, 16, 16, 16));

        wrap.add(headerWrap, BorderLayout.NORTH);
        wrap.add(new JScrollPane(descValue), BorderLayout.CENTER);
        wrap.add(manageActions, BorderLayout.SOUTH);
        return wrap;
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(AppTheme.PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 239)),
                new EmptyBorder(18, 18, 18, 18)));
        return panel;
    }

    private JButton createPrimaryHeroButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setFont(AppTheme.BODY_FONT.deriveFont(Font.BOLD, 15f));
        button.setForeground(new Color(15, 23, 42));
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setFocusPainted(false);
        return button;
    }

    private JButton createSecondaryHeroButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setFont(AppTheme.BODY_FONT.deriveFont(Font.BOLD, 15f));
        button.setForeground(new Color(15, 23, 42));
        button.setBackground(new Color(255, 255, 255, 220));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 120)),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        button.setFocusPainted(false);
        return button;
    }

    private JButton createGhostButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setFont(AppTheme.BODY_FONT.deriveFont(15f));
        button.setForeground(AppTheme.TEXT);
        button.setBackground(new Color(244, 247, 252));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(217, 226, 238)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        button.setFocusPainted(false);
        return button;
    }

    private JButton createActionButton(String text, Color background, Color foreground, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setFont(AppTheme.BODY_FONT.deriveFont(Font.BOLD, 15f));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        button.setFocusPainted(false);
        return button;
    }

    private JLabel createInfoRow(String key, String value) {
        JLabel label = new JLabel();
        label.setFont(AppTheme.BODY_FONT.deriveFont(15f));
        label.setForeground(AppTheme.SUBTEXT);
        setInfoRow(label, key, value);
        return label;
    }

    private JLabel createKindRow(String value) {
        JLabel label = new JLabel();
        label.setFont(AppTheme.BODY_FONT.deriveFont(Font.BOLD, 15f));
        setKindRow(label, value, ToolKind.BUILTIN);
        return label;
    }

    private void setInfoRow(JLabel label, String key, String value) {
        label.setText("<html><span style='color:#64748B;'>" + key + ":</span> " + escape(value) + "</html>");
    }

    private void setKindRow(JLabel label, String value, ToolKind kind) {
        Color color = resolveKindColor(kind);
        String bg = AppTheme.toHex(new Color(
                Math.min(255, color.getRed() + 205),
                Math.min(255, color.getGreen() + 205),
                Math.min(255, color.getBlue() + 205)));
        String fg = AppTheme.toHex(color);
        label.setText("<html><span style='color:#64748B;'>" + i18n.text("分类", "Kind") + ":</span> "
                + "<span style='background-color:" + bg + ";color:" + fg
                + ";font-weight:700;padding:3px 8px;border-radius:10px;'>"
                + escape(value) + "</span></html>");
    }

    private Component buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.PANEL);
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));
        statusLabel = new JLabel(i18n.text("就绪", "Ready"));
        statusLabel.setFont(AppTheme.SMALL_FONT);
        statusLabel.setForeground(AppTheme.SUBTEXT);
        panel.add(statusLabel, BorderLayout.WEST);
        return panel;
    }

    private void refreshTree(String keyword) {
        DefaultMutableTreeNode swingRoot = buildCategoryTree(config.getRootNode(), normalizeKeyword(keyword));
        if (swingRoot == null) {
            swingRoot = new DefaultMutableTreeNode(config.getRootNode());
        }
        treeModel = new DefaultTreeModel(swingRoot);
        tree.setModel(treeModel);
        expandAll();
        updateStats();
        restoreCategorySelection();
    }

    private DefaultMutableTreeNode buildCategoryTree(ToolboxNode node, String keyword) {
        if (node == null || !node.isContainer()) {
            return null;
        }

        boolean matches = keyword.isEmpty() || nodeMatches(node, keyword);
        DefaultMutableTreeNode swingNode = new DefaultMutableTreeNode(node);
        for (ToolboxNode child : node.getChildren()) {
            if (child.isContainer()) {
                DefaultMutableTreeNode childSwing = buildCategoryTree(child, keyword);
                if (childSwing != null) {
                    swingNode.add(childSwing);
                    matches = true;
                }
            } else if (keyword.isEmpty() || nodeMatches(child, keyword)) {
                matches = true;
            }
        }
        return matches ? swingNode : null;
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase();
    }

    private boolean nodeMatches(ToolboxNode node, String keyword) {
        return contains(node.getName(), keyword)
                || contains(node.getDescription(), keyword)
                || contains(node.getTags(), keyword)
                || contains(node.getTarget(), keyword)
                || contains(node.getLauncherPath(), keyword)
                || contains(node.getLaunchArguments(), keyword)
                || contains(node.getWorkingDirectory(), keyword)
                || contains(node.getSetupCommand(), keyword)
                || contains(node.getToolKind().getDisplayName(), keyword)
                || contains(i18n.toolKind(node.getToolKind()), keyword)
                || contains(node.getColorHex(), keyword)
                || contains(node.getIconKey(), keyword);
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.toLowerCase().contains(keyword);
    }

    private void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    private void updateStats() {
        countValue.setText(String.valueOf(Math.max(countLeafEntries(config.getRootNode()), 0)));
    }

    private int countLeafEntries(ToolboxNode node) {
        int count = node.isContainer() ? 0 : 1;
        for (ToolboxNode child : node.getChildren()) {
            count += countLeafEntries(child);
        }
        return count;
    }

    private int countCategories(ToolboxNode node) {
        int count = node.isContainer() ? 1 : 0;
        for (ToolboxNode child : node.getChildren()) {
            count += countCategories(child);
        }
        return count;
    }

    private void selectInitialCategory() {
        if (!config.getRootNode().getChildren().isEmpty()) {
            ToolboxNode first = config.getRootNode().getChildren().get(0);
            if (first.isContainer()) {
                selectCategoryById(first.getId());
                return;
            }
        }
        currentCategory = config.getRootNode();
        refreshEntryList(null);
        updateDetail(config.getRootNode());
    }

    private void restoreCategorySelection() {
        if (currentCategory != null && selectCategoryById(currentCategory.getId())) {
            return;
        }
        if (tree.getRowCount() > 0) {
            tree.setSelectionRow(0);
        }
    }

    private void onCategorySelected(TreeSelectionEvent event) {
        ToolboxNode node = getSelectedCategory();
        if (node == null) {
            node = config.getRootNode();
        }
        currentCategory = node;
        refreshEntryList(null);
        updateDetail(node);
        setStatus(i18n.text("已切换分类: ", "Category: ") + node.getName());
    }

    private void refreshEntryList(String preferredEntryId) {
        entryListModel.clear();
        ToolboxNode category = currentCategory == null ? config.getRootNode() : currentCategory;
        String keyword = normalizeKeyword(searchField == null ? "" : searchField.getText());
        for (ToolboxNode child : category.getChildren()) {
            if (keyword.isEmpty() || nodeMatches(child, keyword)) {
                entryListModel.addElement(child);
            }
        }

        listTitle.setText(category == config.getRootNode() ? i18n.text("全部内容", "All entries") : category.getName());
        listSubtitle.setText(i18n.text("共 ", "") + entryListModel.getSize()
                + i18n.text(" 条内容，双击分类进入下一层。", " entries. Double-click a category to enter it."));

        if (preferredEntryId != null) {
            selectEntryById(preferredEntryId);
            return;
        }

        entryList.clearSelection();
        detailNode = category;
        updateActionState(category);
    }

    private void onEntrySelected(ToolboxNode node) {
        if (node == null) {
            detailNode = currentCategory;
            updateDetail(currentCategory);
            return;
        }
        detailNode = node;
        updateDetail(node);
    }

    private void updateDetail(ToolboxNode node) {
        if (node == null) {
            updateWelcome();
            return;
        }

        titleValue.setText(node == config.getRootNode() ? config.getSettings().getAppTitle() : emptyAsDash(node.getName()));
        ToolKind kind = resolveToolKind(node);
        setKindRow(kindValue, localizeToolKind(node), kind);
        kindValue.setVisible(!node.isContainer());
        setInfoRow(typeValue, i18n.text("类型", "Type"), localizeType(node.getType()));
        setInfoRow(targetValue, i18n.text("目标", "Target"), buildTargetText(node));
        setInfoRow(pathValue, i18n.text("位置", "Location"), buildPathText(node));
        setInfoRow(tagValue, i18n.text("标签", "Tag"), emptyAsDash(node.getTags()));
        setInfoRow(launcherValue, i18n.text("安装配置", "Setup"), buildSetupText(node));
        launcherValue.setVisible(shouldShowSetupInfo(node));
        descValue.setText(buildDescription(node));
        updateActionState(node);
    }

    private void updateActionState(ToolboxNode node) {
        boolean hasNode = node != null;
        boolean canEdit = hasNode && node.getType() != NodeType.ROOT;
        boolean isToolLike = hasNode && (node.getType() == NodeType.TOOL || node.getType() == NodeType.DOCUMENT);
        boolean isWeb = hasNode && node.getType() == NodeType.WEB;
        boolean hasTarget = hasNode && !isBlank(node.getTarget());
        ToolKind kind = resolveToolKind(node);
        boolean pluginWeb = kind == ToolKind.PLUGIN && isWeb;
        boolean pluginInstallable = kind == ToolKind.PLUGIN && !isWeb;
        boolean webKind = kind == ToolKind.WEB || pluginWeb;
        boolean installableKind = kind == ToolKind.INSTALLABLE || pluginInstallable;
        boolean linuxKind = kind == ToolKind.LINUX;

        openButton.setEnabled(hasNode && (isToolLike || isWeb) && !linuxKind);
        cmdButton.setEnabled(hasNode && isToolLike && !webKind && !linuxKind);
        powerShellButton.setEnabled(hasNode && isToolLike && !webKind && !linuxKind);
        locateButton.setEnabled(hasNode && isToolLike);
        copyButton.setEnabled(hasTarget && !linuxKind);
        editButton.setEnabled(canEdit);
        deleteButton.setEnabled(canEdit);

        applyActionVisibility(openButton, LauncherService.LaunchMode.DEFAULT, hasNode && (isToolLike || isWeb) && !linuxKind);
        applyActionVisibility(cmdButton, LauncherService.LaunchMode.CMD, hasNode && isToolLike && !webKind && !linuxKind && !installableKind);
        applyActionVisibility(powerShellButton, LauncherService.LaunchMode.POWERSHELL, hasNode && isToolLike && !webKind && !linuxKind && !installableKind);
        applyActionVisibility(locateButton, LauncherService.LaunchMode.EXPLORER, hasNode && isToolLike && (linuxKind || !webKind));
        copyButton.setVisible(hasTarget && !linuxKind);
    }

    private void applyLaunchModeVisibility(JButton button, LauncherService.LaunchMode mode) {
        boolean visible = config.getSettings().getEnabledLaunchModes().contains(mode.name());
        button.setVisible(visible);
        if (!visible) {
            button.setEnabled(false);
        }
    }

    private void applyActionVisibility(JButton button, LauncherService.LaunchMode mode, boolean allowed) {
        applyLaunchModeVisibility(button, mode);
        if (!allowed) {
            button.setVisible(false);
            button.setEnabled(false);
        }
    }

    private String buildTargetText(ToolboxNode node) {
        if (node == null) {
            return "-";
        }
        if (node.isContainer()) {
            return i18n.text("当前分类下共有 ", "This category contains ") + node.getChildren().size()
                    + i18n.text(" 条内容", " entries");
        }
        return emptyAsDash(node.getTarget());
    }

    private String buildPathText(ToolboxNode node) {
        if (node == null) {
            return "-";
        }
        List<String> names = new ArrayList<String>();
        ToolboxNode cursor = node;
        while (cursor != null && cursor != config.getRootNode()) {
            names.add(0, cursor.getName());
            cursor = findParentNode(cursor);
        }
        return names.isEmpty() ? i18n.text("根分类", "Root category") : joinPath(names);
    }

    private String joinPath(List<String> names) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                builder.append(" / ");
            }
            builder.append(names.get(i));
        }
        return builder.toString();
    }

    private String buildDescription(ToolboxNode node) {
        if (node == null) {
            return config.getSettings().getWelcomeText();
        }
        if (node == config.getRootNode()) {
            return config.getSettings().getWelcomeText()
                    + i18n.text(
                    "\n\n左侧显示分类，中间显示该分类下的内容，右侧显示工具、网址或文档详情，并支持你在设置里控制可见的打开方式。",
                    "\n\nCategories appear on the left, entries in the middle, and details on the right. Visible launch modes can be controlled in settings.");
        }
        if (!isBlank(node.getDescription())) {
            return node.getDescription();
        }
        if (node.isContainer()) {
            return i18n.text("这是一个分类。点击左侧分类或双击中间列表中的子分类，可以继续查看这一层下面的所有内容。",
                    "This is a category. Select it on the left or double-click it in the middle list to view its entries.");
        }
        if (node.getType() == NodeType.WEB) {
            return i18n.text("这是一个网址入口。支持默认浏览器打开、CMD 打开、PowerShell 打开，也可以直接复制网址。",
                    "This is a web entry. Open it with the default browser or copy the URL directly.");
        }
        if (node.getType() == NodeType.DOCUMENT) {
            return i18n.text("这是一个文档入口。支持默认打开、CMD 打开、PowerShell 打开以及定位到文件所在目录。",
                    "This is a document entry. Open it, run it through a shell, or locate it in Explorer.");
        }
        return i18n.text("这是一个工具入口。支持默认打开、CMD 打开、PowerShell 打开以及定位到文件所在目录。",
                "This is a tool entry. Open it, run it through a shell, or locate it in Explorer.");
    }

    private void updateWelcome() {
        updateDetail(config.getRootNode());
        setStatus(i18n.text("就绪", "Ready"));
    }

    private void addFolder() {
        ToolboxNode parent = currentCategory == null ? config.getRootNode() : currentCategory;
        ToolboxNode draft = new ToolboxNode();
        draft.setType(NodeType.FOLDER);
        draft.setColorHex(PRESET_COLORS[1]);
        draft.setIconKey("moon");
        EntryEditorDialog dialog = new EntryEditorDialog(this, i18n.text("新增分类", "New category"), draft, true, config.getSettings().getAvailableTags(), i18n);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) {
            return;
        }
        dialog.fillNode(draft);
        draft.setType(NodeType.FOLDER);
        parent.addChild(draft);
        saveAndRefresh(draft, i18n.text("已新增分类: ", "Added category: ") + draft.getName());
    }

    private void addContent(NodeType preferredType) {
        ToolboxNode parent = currentCategory == null ? config.getRootNode() : currentCategory;
        ToolboxNode draft = new ToolboxNode();
        draft.setType(preferredType);
        EntryEditorDialog dialog = new EntryEditorDialog(this, i18n.text("新增内容", "New entry"), draft, false, config.getSettings().getAvailableTags(), i18n);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) {
            return;
        }
        dialog.fillNode(draft);
        parent.addChild(draft);
        saveAndRefresh(draft, i18n.text("已新增内容: ", "Added entry: ") + draft.getName());
    }

    private void editSelectedNode() {
        ToolboxNode node = getActiveNode();
        if (node == null || node.getType() == NodeType.ROOT) {
            JOptionPane.showMessageDialog(this, i18n.text("请选择一个可编辑的分类或内容。", "Select an editable category or entry."), i18n.text("提示", "Notice"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ToolboxNode snapshot = cloneForEdit(node);
        EntryEditorDialog dialog = new EntryEditorDialog(this, i18n.text("编辑内容", "Edit entry"), snapshot, node.isContainer(), config.getSettings().getAvailableTags(), i18n);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) {
            return;
        }
        dialog.fillNode(node);
        if (node.isContainer()) {
            node.setType(NodeType.FOLDER);
        }
        saveAndRefresh(node, i18n.text("已更新: ", "Updated: ") + node.getName());
    }

    private ToolboxNode cloneForEdit(ToolboxNode node) {
        ToolboxNode snapshot = new ToolboxNode();
        snapshot.setId(node.getId());
        snapshot.setName(node.getName());
        snapshot.setType(node.getType());
        snapshot.setToolKind(node.getToolKind());
        snapshot.setTarget(node.getTarget());
        snapshot.setLauncherType(node.getLauncherType());
        snapshot.setLauncherPath(node.getLauncherPath());
        snapshot.setLaunchArguments(node.getLaunchArguments());
        snapshot.setWorkingDirectory(node.getWorkingDirectory());
        snapshot.setSetupCommand(node.getSetupCommand());
        snapshot.setTags(node.getTags());
        snapshot.setColorHex(node.getColorHex());
        snapshot.setIconKey(node.getIconKey());
        snapshot.setDescription(node.getDescription());
        snapshot.setChildren(node.getChildren());
        return snapshot;
    }

    private void removeSelectedNode() {
        ToolboxNode node = getActiveNode();
        if (node == null || node.getType() == NodeType.ROOT) {
            JOptionPane.showMessageDialog(this, i18n.text("根分类不能删除。", "The root category cannot be deleted."), i18n.text("提示", "Notice"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                i18n.text("确认删除 “", "Delete \"") + node.getName() + i18n.text("” 以及其下内容？", "\" and all entries under it?"),
                i18n.text("确认删除", "Confirm delete"),
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        ToolboxNode parent = findParentNode(node);
        if (parent != null) {
            parent.removeChild(node);
            saveAndRefresh(parent, i18n.text("已删除: ", "Deleted: ") + node.getName());
        }
    }

    private void openNode(ToolboxNode node, LauncherService.LaunchMode mode) {
        if (node == null) {
            return;
        }
        if (node.isContainer()) {
            selectCategoryById(node.getId());
            return;
        }

        try {
            launcherService.open(node, mode);
            setStatus(i18n.text("已打开: ", "Opened: ") + node.getName() + " [" + i18n.launchMode(mode) + "]");
        } catch (IOException e) {
            showError(i18n.text("打开失败", "Open failed"), e.getMessage());
        } catch (URISyntaxException e) {
            showError(i18n.text("目标格式错误", "Invalid target"), e.getMessage());
        }
    }

    private void copyTarget(ToolboxNode node) {
        if (node == null || isBlank(node.getTarget())) {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(node.getTarget()), null);
        setStatus(i18n.text("已复制目标: ", "Copied target: ") + node.getTarget());
    }

    private void editSettings() {
        SettingsDialog dialog = new SettingsDialog(this, config.getSettings(), i18n);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) {
            return;
        }

        AppSettings settings = config.getSettings();
        String oldLanguage = settings.getLanguage();
        settings.setAppTitle(dialog.getTitleValue());
        settings.setLanguage(dialog.getLanguage());
        I18n newI18n = new I18n(settings.getLanguage());
        if (!settings.getLanguage().equals(oldLanguage)
                && (dialog.getTitleValue().equals(i18n.appTitle()) || dialog.getTitleValue().equals(newI18n.appTitle()))) {
            settings.setAppTitle(newI18n.appTitle());
        }
        if (!settings.getLanguage().equals(oldLanguage)
                && (settings.getWelcomeText().equals(i18n.welcomeText()) || settings.getWelcomeText().equals(newI18n.welcomeText()))) {
            settings.setWelcomeText(newI18n.welcomeText());
        }
        settings.setAccentHex(dialog.getAccentHex());
        settings.setAvailableTags(dialog.getTags());
        settings.setEnabledLaunchModes(dialog.getEnabledModes());
        i18n = newI18n;
        accentColor = AppTheme.parseAccent(settings.getAccentHex());
        applySettings();
        saveAndRefresh(null, i18n.text("设置已保存", "Settings saved"));
    }

    private void toggleLanguage() {
        AppSettings settings = config.getSettings();
        String oldLanguage = settings.getLanguage();
        I18n newI18n = new I18n(i18n.isChinese() ? I18n.EN : I18n.ZH);
        settings.setLanguage(newI18n.getLanguage());
        if (settings.getAppTitle().equals(i18n.appTitle()) || isDefaultTitleForAnotherLanguage(settings.getAppTitle(), oldLanguage)) {
            settings.setAppTitle(newI18n.appTitle());
        }
        if (settings.getWelcomeText().equals(i18n.welcomeText()) || isDefaultWelcomeForAnotherLanguage(settings.getWelcomeText(), oldLanguage)) {
            settings.setWelcomeText(newI18n.welcomeText());
        }
        i18n = newI18n;
        applySettings();
        saveAndRefresh(null, i18n.text("语言已切换", "Language switched"));
    }

    private boolean isDefaultTitleForAnotherLanguage(String value, String currentLanguage) {
        if (isBlank(value)) {
            return false;
        }
        I18n other = new I18n(I18n.ZH.equals(currentLanguage) ? I18n.EN : I18n.ZH);
        return value.equals(other.appTitle());
    }

    private boolean isDefaultWelcomeForAnotherLanguage(String value, String currentLanguage) {
        if (isBlank(value)) {
            return false;
        }
        I18n other = new I18n(I18n.ZH.equals(currentLanguage) ? I18n.EN : I18n.ZH);
        return value.equals(other.welcomeText());
    }

    private void applySettings() {
        setTitle(config.getSettings().getAppTitle());
        setAppIcons();
        getContentPane().removeAll();
        setContentPane(buildContent());
        refreshTree(searchField == null ? "" : searchField.getText().trim());
        if (currentCategory != null) {
            selectCategoryById(currentCategory.getId());
        } else {
            selectInitialCategory();
        }
        revalidate();
        repaint();
    }

    private void setAppIcons() {
        List<Image> icons = new ArrayList<Image>();
        icons.add(IconFactory.createAppIcon(accentColor, 16));
        icons.add(IconFactory.createAppIcon(accentColor, 32));
        icons.add(IconFactory.createAppIcon(accentColor, 48));
        setIconImages(icons);
    }

    private void openConfigFolder() {
        File parent = configService.getConfigFile().getParentFile();
        if (parent == null || !parent.exists()) {
            showError(i18n.text("无法打开", "Cannot open"), i18n.text("配置目录不存在。", "Config directory does not exist."));
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(parent);
                setStatus(i18n.text("已打开配置目录", "Opened config directory"));
            }
        } catch (IOException e) {
            showError(i18n.text("打开失败", "Open failed"), e.getMessage());
        }
    }

    private void saveAndRefresh(ToolboxNode nodeToSelect, String message) {
        refreshTree(searchField == null ? "" : searchField.getText().trim());
        if (nodeToSelect != null) {
            if (nodeToSelect.isContainer()) {
                selectCategoryById(nodeToSelect.getId());
                updateDetail(nodeToSelect);
            } else {
                ToolboxNode parent = findParentNode(nodeToSelect);
                if (parent != null) {
                    selectCategoryById(parent.getId());
                    refreshEntryList(nodeToSelect.getId());
                }
                detailNode = nodeToSelect;
                updateDetail(nodeToSelect);
            }
        } else {
            refreshEntryList(null);
            updateDetail(currentCategory == null ? config.getRootNode() : currentCategory);
        }
        setStatus(message);
        persistConfigAsync();
    }

    private void persistConfigAsync() {
        final I18n saveI18n = i18n;
        Thread saver = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (saveLock) {
                        configService.save(config);
                    }
                } catch (final IOException e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            showError(saveI18n.text("保存失败", "Save failed"), e.getMessage());
                        }
                    });
                }
            }
        }, "MoonLight-config-save");
        saver.setDaemon(true);
        saver.start();
    }

    private boolean selectCategoryById(String id) {
        if (id == null || treeModel == null) {
            return false;
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        TreePath path = findPath(root, id);
        if (path != null) {
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
            return true;
        }
        return false;
    }

    private void selectEntryById(String id) {
        if (id == null) {
            return;
        }
        for (int i = 0; i < entryListModel.size(); i++) {
            ToolboxNode node = entryListModel.get(i);
            if (id.equals(node.getId())) {
                entryList.setSelectedIndex(i);
                entryList.ensureIndexIsVisible(i);
                return;
            }
        }
    }

    private TreePath findPath(DefaultMutableTreeNode current, String id) {
        ToolboxNode currentNode = (ToolboxNode) current.getUserObject();
        if (currentNode != null && id.equals(currentNode.getId())) {
            return new TreePath(current.getPath());
        }
        for (int i = 0; i < current.getChildCount(); i++) {
            TreePath path = findPath((DefaultMutableTreeNode) current.getChildAt(i), id);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    private ToolboxNode findParentNode(ToolboxNode target) {
        return findParentNode(config.getRootNode(), target);
    }

    private ToolboxNode findParentNode(ToolboxNode current, ToolboxNode target) {
        for (ToolboxNode child : current.getChildren()) {
            if (child == target) {
                return current;
            }
            ToolboxNode parent = findParentNode(child, target);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    private DefaultMutableTreeNode getSelectedSwingNode() {
        Object object = tree.getLastSelectedPathComponent();
        return object instanceof DefaultMutableTreeNode ? (DefaultMutableTreeNode) object : null;
    }

    private ToolboxNode getSelectedCategory() {
        DefaultMutableTreeNode swingNode = getSelectedSwingNode();
        return swingNode == null ? null : (ToolboxNode) swingNode.getUserObject();
    }

    private ToolboxNode getActiveNode() {
        ToolboxNode listNode = entryList == null ? null : entryList.getSelectedValue();
        if (listNode != null) {
            return listNode;
        }
        if (detailNode != null) {
            return detailNode;
        }
        return currentCategory;
    }

    private void showTreePopup(int x, int y) {
        ToolboxNode selected = getSelectedCategory();
        JPopupMenu menu = new JPopupMenu();

        JMenuItem addFolder = new JMenuItem(i18n.text("新增分类", "New category"));
        addFolder.addActionListener(e -> addFolder());
        JMenuItem addContent = new JMenuItem(i18n.text("新增内容", "New entry"));
        addContent.addActionListener(e -> addContent(NodeType.TOOL));
        JMenuItem edit = new JMenuItem(i18n.text("编辑分类", "Edit category"));
        edit.addActionListener(e -> editSelectedNode());
        JMenuItem delete = new JMenuItem(i18n.text("删除分类", "Delete category"));
        delete.addActionListener(e -> removeSelectedNode());
        JMenuItem palette = new JMenuItem(i18n.text("设置分类颜色", "Set category color"));
        palette.addActionListener(e -> quickSetColor());
        JMenuItem icon = new JMenuItem(i18n.text("设置分类图标", "Set category icon"));
        icon.addActionListener(e -> quickSetIcon());

        menu.add(addFolder);
        menu.add(addContent);
        if (selected != null && selected.getType() != NodeType.ROOT) {
            menu.addSeparator();
            menu.add(edit);
            menu.add(palette);
            menu.add(icon);
            menu.add(delete);
        }
        menu.show(tree, x, y);
    }

    private void showEntryPopup(int x, int y) {
        ToolboxNode selected = entryList.getSelectedValue();
        if (selected == null) {
            return;
        }

        JPopupMenu menu = new JPopupMenu();
        JMenuItem open = new JMenuItem(i18n.text("默认打开", "Open"));
        open.addActionListener(e -> openNode(selected, LauncherService.LaunchMode.DEFAULT));
        JMenuItem cmd = new JMenuItem(i18n.text("CMD打开", "Open with CMD"));
        cmd.addActionListener(e -> openNode(selected, LauncherService.LaunchMode.CMD));
        JMenuItem ps = new JMenuItem(i18n.text("PowerShell打开", "Open with PowerShell"));
        ps.addActionListener(e -> openNode(selected, LauncherService.LaunchMode.POWERSHELL));
        JMenuItem locate = new JMenuItem(i18n.text("定位文件", "Locate file"));
        locate.addActionListener(e -> openNode(selected, LauncherService.LaunchMode.EXPLORER));
        JMenuItem copy = new JMenuItem(i18n.text("复制目标", "Copy target"));
        copy.addActionListener(e -> copyTarget(selected));
        JMenuItem edit = new JMenuItem(i18n.text("编辑", "Edit"));
        edit.addActionListener(e -> editSelectedNode());
        JMenuItem delete = new JMenuItem(i18n.text("删除", "Delete"));
        delete.addActionListener(e -> removeSelectedNode());

        if (selected.isContainer()) {
            JMenuItem enter = new JMenuItem(i18n.text("进入分类", "Enter category"));
            enter.addActionListener(e -> selectCategoryById(selected.getId()));
            menu.add(enter);
        } else {
            ToolKind kind = resolveToolKind(selected);
            boolean webKind = kind == ToolKind.WEB || (kind == ToolKind.PLUGIN && selected.getType() == NodeType.WEB);
            boolean linuxKind = kind == ToolKind.LINUX;
            boolean installableKind = kind == ToolKind.INSTALLABLE || (kind == ToolKind.PLUGIN && selected.getType() != NodeType.WEB);
            boolean localEntry = selected.getType() == NodeType.TOOL || selected.getType() == NodeType.DOCUMENT;
            if (!linuxKind && config.getSettings().getEnabledLaunchModes().contains(LauncherService.LaunchMode.DEFAULT.name())) {
                menu.add(open);
            }
            if (localEntry && !webKind && !linuxKind && !installableKind
                    && config.getSettings().getEnabledLaunchModes().contains(LauncherService.LaunchMode.CMD.name())) {
                menu.add(cmd);
            }
            if (localEntry && !webKind && !linuxKind && !installableKind
                    && config.getSettings().getEnabledLaunchModes().contains(LauncherService.LaunchMode.POWERSHELL.name())) {
                menu.add(ps);
            }
            if (localEntry
                    && config.getSettings().getEnabledLaunchModes().contains(LauncherService.LaunchMode.EXPLORER.name())) {
                menu.add(locate);
            }
            if (!linuxKind && !isBlank(selected.getTarget())) {
                menu.add(copy);
            }
        }
        menu.addSeparator();
        menu.add(edit);
        menu.add(delete);
        menu.show(entryList, x, y);
    }

    private void quickSetColor() {
        ToolboxNode node = getActiveNode();
        if (node == null || node.getType() == NodeType.ROOT) {
            return;
        }
        JComboBox<String> comboBox = new JComboBox<String>(new DefaultComboBoxModel<String>(PRESET_COLORS));
        comboBox.setEditable(true);
        comboBox.setSelectedItem(emptyAsDash(node.getColorHex()).equals("-")
                ? AppTheme.toHex(accentColor) : node.getColorHex());
        int option = JOptionPane.showConfirmDialog(this, comboBox, i18n.text("设置颜色", "Set color"), JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Object selected = comboBox.getSelectedItem();
            node.setColorHex(selected == null ? "" : selected.toString().trim());
            saveAndRefresh(node, i18n.text("颜色已更新", "Color updated"));
        }
    }

    private void quickSetIcon() {
        ToolboxNode node = getActiveNode();
        if (node == null || node.getType() == NodeType.ROOT) {
            return;
        }
        JComboBox<String> comboBox = new JComboBox<String>(new DefaultComboBoxModel<String>(ICON_KEYS));
        comboBox.setEditable(true);
        comboBox.setSelectedItem(node.getIconKey());
        int option = JOptionPane.showConfirmDialog(this, comboBox, i18n.text("设置图标标识", "Set icon key"), JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Object selected = comboBox.getSelectedItem();
            node.setIconKey(selected == null ? "" : selected.toString().trim());
            saveAndRefresh(node, i18n.text("图标已更新", "Icon updated"));
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        setStatus(title + ": " + message);
    }

    private String localizeType(NodeType type) {
        if (type == NodeType.TOOL) {
            return i18n.typeName(NodeType.TOOL);
        }
        if (type == NodeType.WEB) {
            return i18n.typeName(NodeType.WEB);
        }
        if (type == NodeType.DOCUMENT) {
            return i18n.typeName(NodeType.DOCUMENT);
        }
        if (type == NodeType.ROOT) {
            return i18n.typeName(NodeType.ROOT);
        }
        return i18n.typeName(NodeType.FOLDER);
    }

    private ToolKind resolveToolKind(ToolboxNode node) {
        if (node == null || node.isContainer()) {
            return ToolKind.BUILTIN;
        }
        return node.getToolKind();
    }

    private String localizeToolKind(ToolboxNode node) {
        if (node == null || node.isContainer()) {
            return "-";
        }
        ToolKind kind = resolveToolKind(node);
        if (kind == ToolKind.PLUGIN && node.getType() == NodeType.WEB) {
            return i18n.toolKind(kind) + " / " + i18n.toolKind(ToolKind.WEB);
        }
        if (kind == ToolKind.PLUGIN && shouldShowSetupInfo(node)) {
            return i18n.toolKind(kind) + " / " + i18n.toolKind(ToolKind.INSTALLABLE);
        }
        return i18n.toolKind(kind);
    }

    private boolean shouldShowSetupInfo(ToolboxNode node) {
        if (node == null || node.isContainer()) {
            return false;
        }
        ToolKind kind = resolveToolKind(node);
        return kind == ToolKind.INSTALLABLE
                || (kind == ToolKind.PLUGIN && node.getType() != NodeType.WEB && !isBlank(node.getSetupCommand()));
    }

    private String buildSetupText(ToolboxNode node) {
        if (!shouldShowSetupInfo(node)) {
            return "-";
        }
        if (!isBlank(node.getSetupCommand())) {
            return node.getSetupCommand();
        }
        if (!isBlank(node.getLauncherPath())) {
            return node.getLauncherPath() + (isBlank(node.getLaunchArguments()) ? "" : " " + node.getLaunchArguments());
        }
        return i18n.text("请在编辑中补充安装配置命令", "Add the setup command while editing");
    }

    private Color resolveKindColor(ToolKind kind) {
        if (kind == ToolKind.WEB) {
            return new Color(16, 185, 129);
        }
        if (kind == ToolKind.INSTALLABLE) {
            return new Color(245, 158, 11);
        }
        if (kind == ToolKind.LINUX) {
            return new Color(15, 23, 42);
        }
        if (kind == ToolKind.PLUGIN) {
            return new Color(124, 58, 237);
        }
        return new Color(30, 64, 175);
    }

    private String emptyAsDash(String value) {
        return isBlank(value) ? "-" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String escape(String value) {
        String text = value == null ? "" : value;
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static void prepareLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private final class NodeTransferHandler extends TransferHandler {
        private final DataFlavor nodesFlavor = new DataFlavor(ToolboxNode.class, "ToolboxNode");
        private ToolboxNode draggedNode;

        @Override
        protected Transferable createTransferable(JComponent c) {
            draggedNode = getSelectedCategory();
            return new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{nodesFlavor};
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return nodesFlavor.equals(flavor);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    if (!isDataFlavorSupported(flavor)) {
                        throw new UnsupportedFlavorException(flavor);
                    }
                    return draggedNode;
                }
            };
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop() || !support.isDataFlavorSupported(nodesFlavor) || draggedNode == null) {
                return false;
            }

            JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
            TreePath path = dropLocation.getPath();
            if (path == null) {
                return false;
            }

            DefaultMutableTreeNode swingTarget = (DefaultMutableTreeNode) path.getLastPathComponent();
            ToolboxNode targetNode = (ToolboxNode) swingTarget.getUserObject();
            if (!targetNode.isContainer() || targetNode == draggedNode) {
                return false;
            }
            return !isDescendant(draggedNode, targetNode);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
            DefaultMutableTreeNode swingTarget = (DefaultMutableTreeNode) dropLocation.getPath().getLastPathComponent();
            ToolboxNode targetNode = (ToolboxNode) swingTarget.getUserObject();

            ToolboxNode sourceParent = findParentNode(draggedNode);
            if (sourceParent == null) {
                return false;
            }

            sourceParent.removeChild(draggedNode);
            targetNode.addChild(draggedNode);
            saveAndRefresh(draggedNode, i18n.text("分类移动已完成", "Category moved"));
            return true;
        }

        private boolean isDescendant(ToolboxNode parent, ToolboxNode candidate) {
            for (ToolboxNode child : parent.getChildren()) {
                if (child == candidate || isDescendant(child, candidate)) {
                    return true;
                }
            }
            return false;
        }
    }

    private final class EntryListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ToolboxNode) {
                ToolboxNode node = (ToolboxNode) value;
                setFont(AppTheme.BODY_FONT);
                setIcon(new javax.swing.ImageIcon(IconFactory.createNodeIcon(node, AppTheme.resolveNodeColor(node, accentColor), 24)));
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(isSelected ? new Color(191, 219, 254) : new Color(226, 232, 240)),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)));
                setBackground(isSelected ? new Color(239, 246, 255) : Color.WHITE);
                setForeground(AppTheme.TEXT);
                String type = node.isContainer() ? i18n.text("分类", "Category") : localizeType(node.getType());
                String extra = node.isContainer() ? i18n.text("双击进入该分类", "Double-click to enter") : emptyAsDash(node.getTarget());
                setText("<html><div style='font-size:13px;font-weight:700;color:#0F172A;'>"
                        + escape(node.getName())
                        + "</div><div style='font-size:11px;color:#64748B;'>"
                        + escape(type + " · " + extra)
                        + "</div></html>");
            }
            return this;
        }
    }

    private static final class GradientPanel extends JPanel {
        private final Color start;
        private final Color middle;
        private final Color end;

        private GradientPanel(Color start, Color middle, Color end) {
            this.start = start;
            this.middle = middle;
            this.end = end;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint first = new GradientPaint(0, 0, start, getWidth() / 2f, getHeight(), middle);
            g2.setPaint(first);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            GradientPaint second = new GradientPaint(getWidth() / 2f, 0,
                    new Color(middle.getRed(), middle.getGreen(), middle.getBlue(), 180),
                    getWidth(), getHeight(), end);
            g2.setPaint(second);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            g2.setColor(new Color(255, 255, 255, 30));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g2.dispose();
        }
    }
}
