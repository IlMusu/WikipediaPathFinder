package org.ilmusu.wikipedia.analyzers;

import org.ilmusu.wikipedia.requesters.HTTPPageRequester;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Supplier;

public class WikipediaApiAnalyzer extends _WikipediaPagesAnalyzer
{
    protected static final String WIKIPEDIA_RADIX_LINK = "https://it.wikipedia.org/wiki/";
    protected static final String WIKIPEDIA_API_LINK = "https://it.wikipedia.org/w/api.php";

    public WikipediaApiAnalyzer(HTTPPageRequester requester)
    {
        super(requester);
    }

    @Override
    public String formatStandardLink(String link)
    {
        return link.substring(WIKIPEDIA_RADIX_LINK.length());
    }

    @Override
    public Map<String, List<String>> getValidLinksInPages(Supplier<Boolean> shouldStop, List<String> pagesNames)
    {
        Map<String, List<String>> titles = new HashMap<>();
        String nextContinueString = "";
        do
        {
            if(shouldStop.get())
                break;
            JSONObject json = performWikipediaApiRequest(pagesNames, nextContinueString);
            JSONObject query = json.getJSONObject("query");
            JSONObject pages = query.getJSONObject("pages");
            Iterator<String> keysIterator = pages.keys();
            while(keysIterator.hasNext())
            {
                String key = keysIterator.next();
                JSONObject pageData = pages.getJSONObject(key);
                if(!pageData.has("links"))
                    continue;
                String pageName = pageData.getString("title").replaceAll(" ", "_");
                List<String> pageTitles = getOrCreateList(titles, pageName);
                JSONArray pageLinks = pageData.getJSONArray("links");
                pageLinks.forEach(link -> {
                    JSONObject linkObject = (JSONObject) link;
                    pageTitles.add(linkObject.getString("title").replaceAll(" ", "_"));
                });
            }
            if(!json.has("continue"))
            {
                nextContinueString = "";
                continue;
            }
            JSONObject continueData = json.getJSONObject("continue");
            if(!continueData.has("plcontinue"))
                continue;
            nextContinueString = continueData.getString("plcontinue");
        }while(!nextContinueString.isEmpty());

        return titles;
    }

    private static List<String> getOrCreateList(Map<String, List<String>> map, String key)
    {
        if(map.containsKey(key))
            return map.get(key);
        List<String> list = new ArrayList<>();
        map.put(key, list);
        return list;
    }

    protected JSONObject performWikipediaApiRequest(List<String> titles, String continueString)
    {
        String params = String.join("&",List.of(
                "titles="+String.join("|", titles),
                "action=query",
                "format=json",
                "prop=links",
                "pllimit=max"));
        if(!continueString.isEmpty())
            params = String.join("&",List.of(
                    params,
                    "plcontinue="+continueString));

        String page = this.requester.getPage(WIKIPEDIA_API_LINK, params);
        return new JSONObject(page);
    }
}
