package io.hbp.com;

import java.util.*;
import java.util.stream.Collectors;

class Repository
{
    public static Random random = new Random(123);

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

        List<SourceFile> parsedSourceFiles = sourceFiles
                .stream()
                .filter(sourceFile -> sourceFile.parsedSuccessfully)
                .collect(Collectors.toList());

        if (parsedSourceFiles.isEmpty())
        {
            return records;
        }

        int sampleSize = (int) Math.ceil(parsedSourceFiles.size() * 0.45);
        int fitSize = (int) Math.ceil(sampleSize * 0.7);
        int testSize = sampleSize - fitSize;

        Collections.shuffle(parsedSourceFiles, random);

        int i = 0;
        for (; i < fitSize; i++)
        {
            addSourceFileRecords(parsedSourceFiles.get(i), records, SourceFile.FIT);
        }

        for (; i < fitSize + testSize; i++)
        {
            addSourceFileRecords(parsedSourceFiles.get(i), records, SourceFile.TEST);
        }

        return records;
    }

    public void addSourceFileRecords(SourceFile sourceFile, List<List<Object>> records, String usage)
    {
        List<Identifier> identifiers = sourceFile
                .identifierGroups
                .stream()
                .map(x -> x.identifiers)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        float avgCasingConsistency;
        if (sourceFile.identifierGroups.size() < 1)
        {
            avgCasingConsistency = 1.0f;
        }
        else
        {
            avgCasingConsistency = 0.0f;
            for (IdentifierGroup identifierGroup : sourceFile.identifierGroups)
            {
                avgCasingConsistency += identifierGroup.casingConsistency();
            }
            avgCasingConsistency /= sourceFile.identifierGroups.size();
        }
        float avgLength = 0;
        float avgWords = 0;
        float avgDigits = 0;
        if (identifiers.size() > 0)
        {
            avgLength = identifiers.stream().mapToInt(identifier -> identifier.name.length()).sum() / (float) identifiers.size();
            avgWords  = identifiers.stream().mapToInt(Identifier::numberOfWords).sum() / (float) identifiers.size();
            avgDigits = identifiers.stream().mapToInt(Identifier::numberOfNumbers).sum() / (float) identifiers.size();
        }
        records.add(List.of
        (
            repositoryID,
            repositoryName,
            sourceFile.fileName,
            sourceFile.violations.size(),
            sourceFile.linesOfCode,
            avgLength,
            avgWords,
            avgDigits,
            avgCasingConsistency,
            usage,
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

    public List<Object> repositoryRecord()
    {
        return List.of
        (
            repositoryName,
            this.sourceFiles.stream().mapToLong(x -> x.linesOfCode).sum(),
            this.sourceFiles.stream().mapToInt(x -> x.violations.size()).sum(),
            this.sourceFiles.stream().filter(x -> x.parsedSuccessfully).count(),
            this.sourceFiles.stream().filter(x -> !x.parsedSuccessfully).count()
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
