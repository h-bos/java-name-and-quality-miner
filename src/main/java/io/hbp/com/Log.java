package io.hbp.com;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Log
{
    private static StringBuilder stringBuilder = new StringBuilder();

    public static void info(String info)
    {
        String message = "[INFO] " + info;
        System.out.println(message);
        stringBuilder.append(message);
        stringBuilder.append(System.getProperty("line.separator"));
    }

    public static void error(String error)
    {
        String message = "[ERROR] " + error;
        System.out.println(message);
        stringBuilder.append(message);
        stringBuilder.append(System.getProperty("line.separator"));
    }

    public static void error(Throwable exception)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        String message = "[ERROR] " + stringWriter.toString();
        System.out.println(message);
        stringBuilder.append(message);
        stringBuilder.append(System.getProperty("line.separator"));
    }

    public static void writeToFile()
    {
        try (FileWriter writer = new FileWriter(Main.LOG_FILE))
        {
            Log.info("Writing log to file " + Main.LOG_FILE);
            writer.write(stringBuilder.toString());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
