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
            record.add(new RecordValue("repository_id" , id));
            record.add(new RecordValue("repository", repositoryName));
            record.add(new RecordValue("repository_loc" , this.sourceFiles.stream().mapToLong(x -> x.linesOfCode).sum()));
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

    public List<RecordValue> repositoryRecord()
    {
        return List.of
        (
            new RecordValue("repository_id", repositoryID),
            new RecordValue("repository", repositoryName),
            new RecordValue("repository_loc", this.sourceFiles.stream().mapToLong(x -> x.linesOfCode).sum()),
            new RecordValue("violations", this.sourceFiles.stream().mapToInt(x -> x.violations.size()).sum()),
            new RecordValue("parse_success_amount", this.sourceFiles.stream().filter(x -> x.parsedSuccessfully).count()),
            new RecordValue("parse_fail_amount", this.sourceFiles.stream().filter(x -> !x.parsedSuccessfully).count())
        );
    }
}
