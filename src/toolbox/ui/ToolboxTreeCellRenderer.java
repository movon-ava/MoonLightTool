package toolbox.ui;

import toolbox.model.NodeType;
import toolbox.model.ToolboxNode;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Color;
import java.awt.Component;

public class ToolboxTreeCellRenderer extends DefaultTreeCellRenderer {
    private final Color accent;

    public ToolboxTreeCellRenderer(Color accent) {
        this.accent = accent;
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        setFont(AppTheme.BODY_FONT);
        setBackgroundNonSelectionColor(AppTheme.PANEL);
        setBackgroundSelectionColor(new Color(219, 234, 254));
        setTextNonSelectionColor(AppTheme.TEXT);
        setTextSelectionColor(AppTheme.TEXT);

        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
        Object userObject = treeNode.getUserObject();
        if (userObject instanceof ToolboxNode) {
            ToolboxNode node = (ToolboxNode) userObject;
            Color iconColor = AppTheme.resolveNodeColor(node, accent);
            setIcon(new ImageIcon(IconFactory.createNodeIcon(node, iconColor, 22)));
        }
        return this;
    }
}
