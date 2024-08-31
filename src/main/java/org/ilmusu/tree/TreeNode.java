package org.ilmusu.tree;


import java.util.ArrayList;
import java.util.List;

public class TreeNode<T>
{
    public T value;
    protected TreeNode<T> parent;
    protected final List<TreeNode<T>> children;

    public TreeNode(T value, TreeNode<T> parent)
    {
        this.children = new ArrayList<>();
        this.value = value;
        if(parent != null)
        {
            this.parent = parent;
            this.parent.children.add(this);
        }
    }

    public TreeNode<T> getParent()
    {
        return this.parent;
    }

    public List<TreeNode<T>> getChildren()
    {
        return this.children;
    }

    public void disconnectFromParent()
    {
        this.parent.children.remove(this);
        this.parent = null;
    }

    public void disconnectFromChildren()
    {
        this.children.stream().parallel().forEach((child) -> child.parent = null);
        this.children.clear();
    }
}
