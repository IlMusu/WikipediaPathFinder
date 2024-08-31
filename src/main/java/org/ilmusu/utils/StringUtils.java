package org.ilmusu.utils;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class StringUtils
{
    protected static final String SEPARATOR = "#XD#";
    public record WikipediaRequest(String data, long millis) { }

    public static byte[] compressString(String string) throws IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflater = new DeflaterOutputStream(outputStream);
        deflater.write(string.getBytes());
        deflater.flush();
        deflater.close();
        return outputStream.toByteArray();
    }

    public static String decompressString(byte[] data) throws IOException
    {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        InflaterInputStream inflater = new InflaterInputStream(inputStream);
        int length;
        byte[] buf = new byte[5];
        StringBuilder result = new StringBuilder();
        while ((length = inflater.read(buf)) != -1)
            result.append(new String(Arrays.copyOf(buf, length)));
        return result.toString();
    }

    public static String getPageNameFromLink(String link)
    {
        int index = link.lastIndexOf("/");
        return link.substring(index+1)
            .replaceAll("<", "_LS_")
            .replaceAll(">", "_GR_")
            .replaceAll(":", "_SC_")
            .replaceAll("\"", "_DQ_")
            .replaceAll("/", "_FS_")
            .replaceAll("\\\\", "_BS_")
            .replaceAll("\\|", "_VS_")
            .replaceAll("\\?", "_QM_")
            .replaceAll("\\*", "_AS_");
    }

    public static void storeWikipediaPage(Path path, WikipediaRequest data)
    {
        writeFile(path, data.millis + SEPARATOR + data.data);
    }

    public static WikipediaRequest retrieveWikipediaPage(Path path)
    {
        String data = readFile(path);
        if(data == null)
        {
            System.out.println("COULD NOT RETRIEVE WIKIPEDIA PAGE "+path);
            return null;
        }
        String[] millisAndData = data.split(SEPARATOR, 2);
        return new WikipediaRequest(millisAndData[1], Long.parseLong(millisAndData[0]));
    }

    public static boolean writeFile(Path filePath, String data)
    {
        try
        {
            // Check if the file for that wikipedia page already exists
            File file = filePath.toFile();
            file.getParentFile().mkdirs();
            if(!file.createNewFile())
                return false;
            Files.write(filePath, StringUtils.compressString(data), StandardOpenOption.WRITE);
            return true;
        }
        catch(IOException exception)
        {
            System.out.println("COULD NOT WRITE FILE");
            exception.printStackTrace();
            return false;
        }
    }

    public static String readFile(Path filePath)
    {
        try
        {
            // Check if the file for that wikipedia page already exists
            File file = filePath.toFile();
            if(!file.exists())
                return null;
            // Read data from file
            return StringUtils.decompressString(Files.readAllBytes(filePath));
        }
        catch(IOException exception)
        {
            System.out.println("COULD NOT READ FILE");
            exception.printStackTrace();
            return null;
        }
    }
}
