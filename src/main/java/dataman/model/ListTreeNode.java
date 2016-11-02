package dataman.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

/**
 * ListTreeNode
 * @author kazuhiko arase
 */
public class ListTreeNode implements TreeNode {

  private List<ListTreeNode> nodeList = null;

  private ListTreeNode parent = null;

  public List<ListTreeNode> getNodeList() {
    if (nodeList == null) {
      nodeList = new ArrayList<ListTreeNode>();
    }
    return nodeList;
  }

  public void removeAllChildern() {
    getNodeList().clear();
  }

  public void addChild(ListTreeNode node) {
    node.parent = this;
    getNodeList().add(node);
  }

  @Override
  public Enumeration<? extends TreeNode> children() {
    return Collections.enumeration(getNodeList() );
  }

  @Override
  public TreeNode getChildAt(int childIndex) {
    return getNodeList().get(childIndex);
  }

  @Override
  public int getChildCount() {
    return getNodeList().size();
  }

  @Override
  public int getIndex(TreeNode node) {
    return getNodeList().indexOf(node);
  }

  @Override
  public boolean getAllowsChildren() {
    return true;
  }

  @Override
  public TreeNode getParent() {
    return parent;
  }

  @Override
  public boolean isLeaf() {
    return false;
  }
}
