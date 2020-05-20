package io.hbp.com;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SourceFile
{
    public String fileName;
    public Path filePath;

    public List<IdentifierGroup> identifierGroups = new ArrayList<>();
    public List<Violation> violations = new ArrayList<>();

    public long linesOfCode;

    public boolean parsedSuccessfully = false;

    public SourceFile(Path filePath)
    {
        this.filePath = filePath;
        this.fileName = filePath.toString();
    }

    public void addIdentifiers(List<Identifier> identifiers, IdentifierGroup.EntityType entityType)
    {
        if (identifiers.isEmpty())
        {
            return;
        }
        Optional<IdentifierGroup> identifierGroupOptional = identifierGroups
                .stream()
                .filter(identifierGroup -> identifierGroup.entityType == entityType)
                .findAny();
        if (identifierGroupOptional.isPresent())
        {
            identifierGroupOptional.get().identifiers.addAll(identifiers);
        }
        else
        {
            identifierGroups.add(new IdentifierGroup(identifiers, entityType));
        }
    }

    public List<List<Object>> getIdentifierGroupRecords()
    {
        List<List<Object>> sourceFileStatistics = new ArrayList<>();

        int totalNumberOfIdentifiers = identifierGroups.stream().mapToInt(group -> group.identifiers.size()).sum();

        for (IdentifierGroup identifierGroup : identifierGroups)
        {
            List<Object> identifierRecordValues = new ArrayList<>();
            identifierRecordValues.add(fileName);
            identifierRecordValues.add(identifierGroup.names());
            identifierRecordValues.add(identifierGroup.entityType.value);
            identifierRecordValues.add(identifierGroup.averageLength());
            identifierRecordValues.add(identifierGroup.averageNumberOfWords());
            identifierRecordValues.add(identifierGroup.averageNumberOfNumbers());
            identifierRecordValues.add(identifierGroup.casingConsistency());
            identifierRecordValues.add(linesOfCode);
            identifierRecordValues.add(violations.size());
            identifierRecordValues.add(identifierGroup.identifiers.size() / (float) totalNumberOfIdentifiers * violations.size());
            identifierRecordValues.add(parsedSuccessfully);
            sourceFileStatistics.add(identifierRecordValues);
        }

        return sourceFileStatistics;
    }
}
