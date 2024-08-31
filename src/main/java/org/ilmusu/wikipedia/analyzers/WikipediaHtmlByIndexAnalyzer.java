package org.ilmusu.wikipedia.analyzers;

import org.ilmusu.wikipedia.requesters._WikipediaPageRequester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class WikipediaHtmlByIndexAnalyzer extends _WikipediaPagesAnalyzer
{
    protected static final String WIKIPEDIA_RADIX_LINK = "https://it.wikipedia.org";

    protected static List<String> INVALID_LINKS = List.of(
            "/wiki/Speciale",
            "/wiki/Wikipedia",
            "/wiki/Template",
            "/wiki/Discussione",
            "/wiki/File",
            "/wiki/Portale",
            "/wiki/Categoria",
            "/wiki/Discussioni_template",
            "/wiki/Aiuto",
            "/wiki/Pagina_principale",
            "/wiki/Bozza"
    );

    public WikipediaHtmlByIndexAnalyzer(_WikipediaPageRequester requester)
    {
        super(requester);
    }

    @Override
    public String formatStandardLink(String link)
    {
        return link.substring(WIKIPEDIA_RADIX_LINK.length());
    }

    @Override
    public Map<String, List<String>> getValidLinksInPages(Supplier<Boolean> shouldStop, List<String> pages)
    {
        Map<String, List<String>> parentToChildren = new HashMap<>();
        for(String page : pages)
        {
            if(shouldStop.get())
                break;
            parentToChildren.put(page, getAllLinksInPage(page));
        }
        return parentToChildren;
    }

    public List<String> getAllLinksInPage(String pageLink)
    {
        // Getting the html page from wikipedia
        List<String> links = new ArrayList<>();
        String htmlPage = formatWikipediaLink(pageLink);
        // Parsing the page
        int nextStartIndex = 0;
        while(true)
        {
            int nextATagStartIndex = htmlPage.indexOf("<a ", nextStartIndex);
            if(nextATagStartIndex == -1)
                break;
            int nextATagEndIndex = htmlPage.indexOf("</a>", nextATagStartIndex);
            if(nextATagEndIndex == -1)
                break;
            int nextHrefPropertyIndex = htmlPage.indexOf("href", nextATagStartIndex);
            if(nextHrefPropertyIndex == -1)
                break;
            // Check if the <a> tag does not have the href
            nextStartIndex = nextATagEndIndex;
            if(nextHrefPropertyIndex > nextATagEndIndex)
                continue;
            int nextLinkStartIndex = nextHrefPropertyIndex+6;
            int nextLinkEndIndex = htmlPage.indexOf("\"", nextLinkStartIndex);
            // Found a new link
            String link = htmlPage.substring(nextLinkStartIndex, nextLinkEndIndex);
            // Check if it is valid
            if(isLinkInvalid(link))
                continue;
            links.add(link);
        }
        return links;
    }

    public String formatWikipediaLink(String pageLink)
    {
        // For some reason the ' are not parsed correctly, there might be other chars, this is
        // the only one that I have found that generates problems
        pageLink = pageLink.replaceAll("&#39;", "'");
        return this.requester.getPage(WIKIPEDIA_RADIX_LINK+pageLink, "");
    }

    public boolean isLinkInvalid(String link)
    {
        if(!link.startsWith("/wiki/"))
            return true;
        return INVALID_LINKS.stream().parallel().anyMatch(link::startsWith);
    }
}
