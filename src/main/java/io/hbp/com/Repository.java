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

    public List<List<Object>> identifierGroupRecords()
    {
        List<List<Object>> records = sourceFiles
                .stream()
                .map(SourceFile::getIdentifierGroupRecords)
                .flatMap(List::stream)
                .collect(Collectors.toList());
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
                numberOfIdentifiersOf(sourceFile, IdentifierGroup.EntityType.ENUM),
                numberOfIdentifiersOf(sourceFile, IdentifierGroup.EntityType.ENUM_CONSTANT),
                numberOfIdentifiersOf(sourceFile, IdentifierGroup.EntityType.INTERFACE),
                numberOfIdentifiersOf(sourceFile, IdentifierGroup.EntityType.CLASS),
                numberOfIdentifiersOf(sourceFile, IdentifierGroup.EntityType.FIELD),
                numberOfIdentifiersOf(sourceFile, IdentifierGroup.EntityType.METHOD),
                numberOfIdentifiersOf(sourceFile, IdentifierGroup.EntityType.PARAMETER),
                numberOfIdentifiersOf(sourceFile, IdentifierGroup.EntityType.LOCAL_VARIABLE)
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
            this.sourceFiles.stream().filter(x -> !x.parsedSuccessfully).count(),
            this.sourceFiles.stream().mapToInt(x -> x.violations.size()).sum() / (float) this.identifierGroupRecords().size()
        );
    }

    public int numberOfIdentifiersOf(SourceFile sourceFile, IdentifierGroup.EntityType entityType)
    {
        Optional<IdentifierGroup> identifierGroupOptional = sourceFile.identifierGroups
                .stream()
                .filter(identifierGroup -> identifierGroup.entityType == entityType)
                .findAny();
        return identifierGroupOptional.map(identifierGroup -> identifierGroup.identifiers.size()).orElse(0);
    }
}
