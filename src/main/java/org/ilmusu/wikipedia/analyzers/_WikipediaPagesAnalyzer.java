package org.ilmusu.wikipedia.analyzers;

import org.ilmusu.wikipedia.requesters._WikipediaPageRequester;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class _WikipediaPagesAnalyzer
{
    protected final _WikipediaPageRequester requester;

    public _WikipediaPagesAnalyzer(_WikipediaPageRequester requester)
    {
        this.requester = requester;
    }

    public abstract String formatStandardLink(String link);

    public abstract Map<String, List<String>> getValidLinksInPages(Supplier<Boolean> shouldStop, List<String> pages);
}
