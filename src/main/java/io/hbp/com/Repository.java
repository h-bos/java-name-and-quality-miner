package io.hbp.com;

import java.util.*;
import java.util.stream.Collectors;

class Repository
{
    public static int repositoryID = 0;

    public int id = repositoryID++;

    public String repositoryName;

    public List<SourceFile> sourceFiles = new ArrayList<>();

    public Repository(String repositoryName)
    {
       this.repositoryName = repositoryName;
    }

    public List<List<RecordValue>> identifierRecords()
    {
        List<List<RecordValue>> records = sourceFiles.stream().map(SourceFile::asRecords).flatMap(List::stream).collect(Collectors.toList());
        for (List<RecordValue> record : records)
        {
            // Add repository int ID to records for easy filtering to records
            record.add(new RecordValue("repository_id" , id));

            // Add repository folder name
            record.add(new RecordValue("repository", repositoryName));

            // Add repository LOC to records
            long repositoryLinesOfCode = this.sourceFiles.stream().mapToLong(x -> x.linesOfCode).sum();
            record.add(new RecordValue("repository_loc" , repositoryLinesOfCode));
        }
        return records;
    }

    public List<List<RecordValue>> sourceFileRecords()
    {
        List<List<RecordValue>> records = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles)
        {
            records.add(List.of
            (
                new RecordValue("repository_id", repositoryID),
                new RecordValue("repository", repositoryName),
                new RecordValue("source_file", sourceFile.fileName),
                new RecordValue("violations", sourceFile.violations.size()),
                new RecordValue("parsed_successfully", sourceFile.parsedSuccessfully)
            ));
        }
        return records;
    }
}
