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

    public List<List<Object>> identifierRecords()
    {
        List<List<Object>> records = sourceFiles.stream().map(SourceFile::getIdentifierRecords).flatMap(List::stream).collect(Collectors.toList());
        for (List<Object> record : records)
        {
            record.add(id);
            record.add(repositoryName);
            record.add(this.sourceFiles.stream().mapToLong(x -> x.linesOfCode).sum());
        }
        return records;
    }

    public List<List<Object>> sourceFileRecords()
    {
        List<List<Object>> records = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles)
        {
            records.add(List.of
            (
                repositoryID,
                repositoryName,
                sourceFile.fileName,
                sourceFile.violations.size(),
                sourceFile.parsedSuccessfully,
                sourceFile.linesOfCode,
                sourceFile.identifiers.stream().filter(identifier -> identifier.type == Identifier.Type.ENUM).count(),
                sourceFile.identifiers.stream().filter(identifier -> identifier.type == Identifier.Type.ENUM_CONSTANT).count(),
                sourceFile.identifiers.stream().filter(identifier -> identifier.type == Identifier.Type.INTERFACE).count(),
                sourceFile.identifiers.stream().filter(identifier -> identifier.type == Identifier.Type.CLASS).count(),
                sourceFile.identifiers.stream().filter(identifier -> identifier.type == Identifier.Type.FIELD).count(),
                sourceFile.identifiers.stream().filter(identifier -> identifier.type == Identifier.Type.METHOD).count(),
                sourceFile.identifiers.stream().filter(identifier -> identifier.type == Identifier.Type.PARAMETER).count(),
                sourceFile.identifiers.stream().filter(identifier -> identifier.type == Identifier.Type.LOCAL_VARIABLE).count()
            ));
        }
        return records;
    }

    public List<Object> repositoryRecord()
    {
        return List.of
        (
            repositoryID,
            repositoryName,
            this.sourceFiles.stream().mapToLong(x -> x.linesOfCode).sum(),
            this.sourceFiles.stream().mapToInt(x -> x.violations.size()).sum(),
            this.sourceFiles.stream().filter(x -> x.parsedSuccessfully).count(),
            this.sourceFiles.stream().filter(x -> !x.parsedSuccessfully).count()
        );
    }
}
