package org.ilmusu;

import org.ilmusu.wikipedia.WikipediaKnowledge;
import org.ilmusu.wikipedia.analyzers.WikipediaHtmlByRegexAnalyzer;
import org.ilmusu.wikipedia.executors.WikipediaThreadedExecutor;
import org.ilmusu.wikipedia.requesters.DiskPageRequester;

import javax.swing.filechooser.FileSystemView;
import java.nio.file.Paths;
import java.util.List;

public class Main
{
    protected static final String STORAGE_FOLDER = String.valueOf(
            Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getPath(), "WikipediaPathFinder"));

    public static void main(String[] args)
    {
        long startMillis = System.currentTimeMillis();
        WikipediaKnowledge knowledge = new WikipediaKnowledge()
            .setAnalyzer(new WikipediaHtmlByRegexAnalyzer(new DiskPageRequester(STORAGE_FOLDER)))
            .setExecutor(new WikipediaThreadedExecutor(List.of(1, 64, 256)))
            .setPathExtrema(
                "https://it.wikipedia.org/wiki/Stazione_di_Avegno",
                "https://it.wikipedia.org/wiki/Tacca"
            );

        List<String> path = knowledge.findPath();
        long millisTaken = System.currentTimeMillis()-startMillis;

        System.out.println(path);
        System.out.println("PATH FINDING TOOK "+millisTaken/1000.0+" s");
    }
}