package org.ilmusu.wikipedia.analyzers;

import org.ilmusu.wikipedia.requesters._WikipediaPageRequester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikipediaHtmlByRegexAnalyzer extends _WikipediaPagesAnalyzer
{
    protected static final String WIKIPEDIA_RADIX_LINK = "https://it.wikipedia.org";
    private static final Pattern HREF_FINDER = Pattern.compile(
        "href=\"/wiki/(?!" +
            "Speciale|"+
            "Wikipedia|"+
            "Template|"+
            "Discussione|"+
            "File|"+
            "Portale|"+
            "Categoria|"+
            "Discussioni_template|"+
            "Aiuto|"+
            "Pagina_principale|"+
            "Bozza"+
        ")[^\"]*\"");

    public WikipediaHtmlByRegexAnalyzer(_WikipediaPageRequester requester)
    {
        super(requester);
    }

    @Override
    public String formatStandardLink(String link)
    {
        return link.substring(WIKIPEDIA_RADIX_LINK.length());
    }

    public String getWikipediaPage(String pageLink)
    {
        pageLink = pageLink.replaceAll("&#39;", "'");
        return this.requester.getPage(WIKIPEDIA_RADIX_LINK+pageLink, "");
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
        String htmlPage = getWikipediaPage(pageLink);
        // Parsing the page
        Matcher m = HREF_FINDER.matcher(htmlPage);
        while(m.find())
            links.add(htmlPage.substring(m.start()+6, m.end()-1));
        return links;
    }
}

