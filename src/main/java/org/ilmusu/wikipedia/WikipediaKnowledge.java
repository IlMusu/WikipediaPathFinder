package org.ilmusu.wikipedia;

import org.ilmusu.tree.Tree;
import org.ilmusu.tree.TreeNode;
import org.ilmusu.wikipedia.analyzers._WikipediaPagesAnalyzer;
import org.ilmusu.wikipedia.executors._WikipediaExecutor;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WikipediaKnowledge
{
    protected static final int ANALYZER_CAPACITY = 10;

    public String startPage;
    public String endPage;

    protected _WikipediaPagesAnalyzer analyzer;
    protected _WikipediaExecutor executor;

    public Tree<String> nodes;
    protected TreeNode<String> endNode;
    protected final Object nodesSemaphore = new Object();

    protected int checkedPages;
    protected int currentStep;
    protected int checkedPagesInStep;
    protected int pagesToCheckInStep;

    public WikipediaKnowledge setAnalyzer(_WikipediaPagesAnalyzer analyzer)
    {
        this.analyzer = analyzer;
        return this;
    }

    public WikipediaKnowledge setExecutor(_WikipediaExecutor executor)
    {
        this.executor = executor;
        return this;
    }

    public WikipediaKnowledge setPathExtrema(String startPage, String endLink)
    {
        this.startPage = this.analyzer.formatStandardLink(startPage);
        this.endPage = this.analyzer.formatStandardLink(endLink);
        return this;
    }

    public List<String> findPath()
    {
        this.endNode = null;
        this.nodes = new Tree<>(this.startPage);

        this.currentStep = 0;
        this.checkedPages = 0;

        Queue<TreeNode<String>> nextNodes = new ConcurrentLinkedQueue<>();
        nextNodes.add(this.nodes.getRoot());

        while(!nextNodes.isEmpty())
        {
            Queue<TreeNode<String>> currentNodes = nextNodes;
            Queue<TreeNode<String>> newNextNodes = new ConcurrentLinkedQueue<>();
            this.pagesToCheckInStep = currentNodes.size();
            this.checkedPagesInStep = 0;
            this.executor.executeStep(this.currentStep, () -> this.analyzeNextNodes(currentNodes, newNextNodes));
            if(this.foundSolution())
                break;
            nextNodes = newNextNodes;
            this.currentStep++;
        }
        return reconstructPath(this.endNode);
    }

    protected boolean foundSolution()
    {
        return this.endNode != null;
    }

    protected void analyzeNextNodes(Queue<TreeNode<String>> currentNodes, Queue<TreeNode<String>> nextNodes)
    {
        while(!currentNodes.isEmpty())
        {
            // Create a list of multiple pages to check for links
            List<String> pagesToCheck = new ArrayList<>();
            while(!currentNodes.isEmpty() && pagesToCheck.size() < ANALYZER_CAPACITY) {
                TreeNode<String> node;
                try { node = currentNodes.remove(); }
                catch (NoSuchElementException e) { break; }
                pagesToCheck.add(node.value);
            }
            if(pagesToCheck.isEmpty())
                return;
            // Gets all the links from multiple pages, ordered by pages
            Map<String, List<String>> newLinks = this.analyzer.getValidLinksInPages(this::foundSolution, pagesToCheck);
            // Analyze all the new links
            for(Map.Entry<String, List<String>> pageAndLinks : newLinks.entrySet())
            {
                // Check if another thread found the node
                if(this.foundSolution())
                    return;
                // Step the iteration
                this.checkedPages++;
                this.checkedPagesInStep++;
                TreeNode<String> parentNode = this.nodes.getNode(pageAndLinks.getKey());
                for(String newLink : pageAndLinks.getValue())
                {
                    // Add the page to the knowledge if not already contained
                    TreeNode<String> newNode;
                    synchronized(this.nodesSemaphore)
                    {
                        if(this.nodes.containsValue(newLink))
                            continue;
                        newNode = this.nodes.addNode(newLink, parentNode);
                    }
                    this.logProgress();
                    // Check if it is the end title
                    if(Objects.equals(newLink, this.endPage))
                    {
                        this.endNode = newNode;
                        return;
                    }
                    // Otherwise put it in the next nodes
                    nextNodes.add(newNode);
                }
            }
        }

    }

    protected void logProgress()
    {
        if(this.nodes.size() % 100 == 0)
        {
            synchronized (this)
            {
                System.out.println("--------------- added 100 nodes ---------------");
                System.out.println("Current step: "+this.currentStep);
                System.out.println("Checked pages of current step: "+this.checkedPagesInStep+"/"+this.pagesToCheckInStep);
                System.out.println("Total number of nodes: "+this.nodes.size());
            }
        }
    }

    protected List<String> reconstructPath(TreeNode<String> node)
    {
        List<String> path = new ArrayList<>();
        TreeNode<String> parent = node;
        while(parent != null)
        {
            path.add(parent.value);
            parent = parent.getParent();
        }
        return path;
    }
}
