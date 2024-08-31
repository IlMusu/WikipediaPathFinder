package org.ilmusu.tree;

import java.util.HashMap;
import java.util.Map;

public class Tree<T>
{
    protected final Map<T, TreeNode<T>> nodesByValue;
    protected final TreeNode<T> root;

    public Tree(T root)
    {
        this.nodesByValue = new HashMap<>();
        this.root = new TreeNode<>(root, null);
        this.nodesByValue.put(root, this.root);
    }

    public int size()
    {
        return this.nodesByValue.size();
    }

    public TreeNode<T> getRoot()
    {
        return this.root;
    }

    public boolean containsValue(T value)
    {
        return this.nodesByValue.containsKey(value);
    }

    public TreeNode<T> addNode(T value, TreeNode<T> parent)
    {
        TreeNode<T> node = new TreeNode<>(value, parent);
        this.nodesByValue.put(value, node);
        return node;
    }

    public TreeNode<T> getNode(T value)
    {
        return this.nodesByValue.get(value);
    }

    public void removeNode(T value)
    {
        TreeNode<T> node = this.nodesByValue.remove(value);
        if(node == null)
            return;
        node.disconnectFromParent();
        node.disconnectFromChildren();
    }
}
