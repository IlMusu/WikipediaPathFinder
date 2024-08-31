package org.ilmusu.wikipedia.requesters;

import org.ilmusu.utils.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class HTTPPageRequester extends _WikipediaPageRequester
{
    protected boolean cachePages;
    protected String storageFolder;

    public void setCachePages(String storageFolder)
    {
        this.cachePages = true;
        this.storageFolder = storageFolder;
    }

    @Override
    public String getPage(String pageLink, String params)
    {
        long requestDurationMillis = 0;
        String pageData;
        HttpURLConnection connection = null;
        try
        {
            URL url = URI.create(pageLink).toURL();
            connection = (HttpURLConnection) url.openConnection();
            // Create request
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            if(!params.isEmpty())
            {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(params);
                wr.close();
            }
            long millisBeforeRequest = System.currentTimeMillis();
            // Get response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while((line = rd.readLine()) != null)
            {
                response.append(line);
                response.append("\n");
            }
            rd.close();
            connection.disconnect();
            requestDurationMillis = System.currentTimeMillis() - millisBeforeRequest;
            pageData = response.toString();
        }
        catch(IOException exception)
        {
            // If the connection is null, nothing can be done
            if(connection == null)
            {
                System.err.println("CONNECTION IS NULL");
                exception.printStackTrace();
                return "";
            }

            // Disconnect the current connection before trying to do anything else
            connection.disconnect();
            // Handle the current error
            String result = handleErrorCode(pageLink, params, connection);
            // If the error could not be handled, nothing can be done
            if(result.isEmpty())
            {
                System.out.println("COULD NOT GET PAGE");
                exception.printStackTrace();
                return "";
            }
            pageData = result;
        }

        if(this.cachePages)
        {
            String name = StringUtils.getPageNameFromLink(pageLink);
            String filePath = String.valueOf(Paths.get(this.storageFolder, name+".wikipage"));
            StringUtils.storeWikipediaPage(Paths.get(filePath), new StringUtils.WikipediaRequest(pageData, requestDurationMillis));
            System.out.println("Request for page "+name+" ("+pageLink+") took "+requestDurationMillis+" ms");
        }

        return pageData;
    }

    public String handleErrorCode(String pageLink, String params, HttpURLConnection connection)
    {
        int responseCode;
        try { responseCode = connection.getResponseCode(); }
        catch(IOException exception)
        {
            System.out.println("COULD NOT RETRIEVE RESPONSE CODE");
            exception.printStackTrace();
            return "";
        }

        if(responseCode == 429)
        {
            // Wait for a bit and then start again
            try { Thread.sleep(1000); }
            catch (InterruptedException exception)
            {
                System.out.println("THREAD SLEEPING HAS BEEN INTERRUPTED");
                exception.printStackTrace();
                return "";
            }
            return getPage(pageLink, params);
        }

        return "";
    }
}
