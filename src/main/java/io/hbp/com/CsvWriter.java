package io.hbp.com;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CsvWriter
{
    public void appendHeaders(String fileName, List<RecordValue> row)
    {
        Path filePath = Paths.get(fileName);
        String headersRow = row.stream().map(recordValue -> recordValue.id).collect(Collectors.joining(","));
        try
        {
            Files.writeString(filePath, headersRow + '\n', StandardOpenOption.APPEND);
            Log.info("Appended headers to " + filePath.getFileName());
        }
        catch (IOException e)
        {
            Log.error(e);
        }
    }

    public void appendRecords(String fileName, List<List<RecordValue>> records)
    {
        Path filePath = Paths.get(fileName);
        List<String> lines = new ArrayList<>();
        for (List<RecordValue> record : records)
        {
            lines.add(record.stream().map(recordValue -> recordValue.value).collect(Collectors.joining(",")));
        }
        try
        {
            Files.write(filePath, lines, StandardOpenOption.APPEND);
            Log.info("Appended " + lines.size() + " records to " + filePath.getFileName());
        }
        catch (IOException e)
        {
            Log.error(e);
        }
    }

    public void clearRecords(String fileName)
    {
        try
        {
            new PrintWriter(fileName).close();
        }
        catch (FileNotFoundException e)
        {
            Log.error(e);
        }
    }
}
