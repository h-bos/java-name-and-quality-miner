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
    static final List<String> repositoryHeaders = List.of
    (
        "repository_id", "repository", "repository_loc", "violations", "parse_success_amount", "parse_fail_amount"
    );

    static final List<String> sourceFileHeaders = List.of
    (
        "repository_id", "repository", "source_file", "violations", "parsed_successfully", "source_file_loc",
        "enum_amount", "enum_constant_amount", "interface_amount", "class_amount", "field_amount", "method_amount",
        "parameter_amount", "local_variable_amount"
    );

    static final List<String> identifierHeaders = List.of
    (
        "file", "names", "type", "avg_length", "avg_words", "avg_numbers", "casing_consistency", "source_file_loc", "violations", "violations_density",
        "parsed_successfully", "repository_id", "repository", "repository_loc"
    );

    public static void appendHeaders(String fileName, List<String> headers)
    {
        Path filePath = Paths.get(fileName);
        String headersRow = String.join(",", headers);
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

    public static void appendRecords(String fileName, List<List<Object>> records)
    {
        Path filePath = Paths.get(fileName);
        List<String> lines = new ArrayList<>();
        for (List<Object> record : records)
        {
            lines.add(record.stream().map(CsvWriter::toString).collect(Collectors.joining(",")));
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

    public static void clearFile(String fileName)
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

    public static String toString(Object value)
    {
        if (value instanceof Boolean)
        {
            return String.valueOf(value).toUpperCase();
        }
        return String.valueOf(value).replace(",", "");
    }
}
