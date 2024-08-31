package org.ilmusu.wikipedia.requesters;

import org.ilmusu.utils.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DiskPageRequester extends _WikipediaPageRequester
{
    protected String storageFolder;
    public long millisOverride = -1;

    public DiskPageRequester(String storageFolder)
    {
        this.storageFolder = storageFolder;
    }

    public DiskPageRequester overrideMillis(long millis)
    {
        this.millisOverride = millis;
        return this;
    }

    @Override
    public String getPage(String pageLink, String params)
    {
        String name = StringUtils.getPageNameFromLink(pageLink);
        String filePath = String.valueOf(Paths.get(this.storageFolder, name+".wikipage"));
        StringUtils.WikipediaRequest page = StringUtils.retrieveWikipediaPage(Path.of(filePath));
        if(page == null)
            return "";
        long pageMillis = this.millisOverride != -1 ? this.millisOverride : page.millis();
        try{ Thread.sleep(pageMillis); }
        catch (InterruptedException exception)
        {
            System.out.println("THREAD SLEEPING INTERRUPTED");
            exception.printStackTrace();
        }
        return page.data();
    }
}
