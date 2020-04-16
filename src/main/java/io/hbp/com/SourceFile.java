package io.hbp.com;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SourceFile
{
    public String fileName;
    public Path filePath;

    public List<Identifier> identifiers = new ArrayList<>();
    public List<Violation> violations = new ArrayList<>();

    public long linesOfCode;

    public boolean parsedSuccessfully = false;

    public SourceFile(Path filePath)
    {
        this.filePath = filePath;
        this.fileName = filePath.toString();
    }

    public void evaluateCasingConsistency()
    {
        if (identifiers.isEmpty())
        {
            return;
        }

        Map<Identifier.Type, List<Identifier>> identifiersGroupedByType = identifiers
                .stream()
                .collect(Collectors.groupingBy(identifier -> identifier.type));

        for (List<Identifier> identifierGroup : identifiersGroupedByType.values())
        {
            int numberOfCamel = 0;
            int numberOfPascal = 0;
            int numberOfUnderline = 0;
            int numberOfHungarian = 0;
            int numberOfOther = 0;

            for (Identifier identifier : identifierGroup)
            {
                // Regexes from:
                //  "How are Identifiers Named in Open Source Software On Popularity and Consistency"
                //  Authors: Yanqing Wang a* , Chong Wang b , Xiaojie Li a , Sijing Yun a , Minjing Song
                // Camel case
                switch (identifier.type())
                {
                    case CAMEL:
                        numberOfCamel++;
                        break;
                    case PASCAL:
                        numberOfPascal++;
                        break;
                    case UNDERLINE:
                        numberOfUnderline++;
                        break;
                    case HUNGARIAN:
                        numberOfHungarian++;
                        break;
                    case OTHER:
                        numberOfOther++;
                        break;
                }
            }

            int max =
                    Integer.max(numberOfCamel,
                            Integer.max(numberOfPascal,
                                    Integer.max(numberOfUnderline,
                                            Integer.max(numberOfHungarian, numberOfOther))));


            int sum =  numberOfCamel + numberOfPascal + numberOfUnderline + numberOfHungarian + numberOfOther;

            float consistency = sum == 0 ? 1.0f : (float) max / sum;

            for (Identifier identifier : identifierGroup)
            {
                identifier.casingConsistency = consistency;
            }
        }
    }

    public List<List<RecordValue>> asRecords()
    {
        evaluateCasingConsistency();

        // Down sampling by only including first occurrence of each identifier type.
        Map<Identifier.Type, List<Identifier>> identifiersGroup = identifiers
                .stream()
                .collect(Collectors.groupingBy(x -> x.type));

        List<List<RecordValue>> compilationUnitStatistics = new ArrayList<>();
        for (Map.Entry<Identifier.Type, List<Identifier>> entry : identifiersGroup.entrySet())
        {
            Identifier identifier = entry.getValue().get(0);
            List<RecordValue> identifierRecordValues = new ArrayList<>();
            identifierRecordValues.add(new RecordValue("file", fileName));
            identifierRecordValues.add(new RecordValue("name", identifier.name));
            identifierRecordValues.add(new RecordValue("type", identifier.type.value));
            identifierRecordValues.add(new RecordValue("length", identifier.name.length()));
            identifierRecordValues.add(new RecordValue("words", identifier.numberOfWords()));
            identifierRecordValues.add(new RecordValue("numbers", identifier.numberOfNumbers()));
            identifierRecordValues.add(new RecordValue("casing_consistency", identifier.casingConsistency));
            identifierRecordValues.add(new RecordValue("source_file_loc", linesOfCode));
            identifierRecordValues.add(new RecordValue("violations", violations.size()));
            identifierRecordValues.add(new RecordValue("parsed_successfully", parsedSuccessfully));
            compilationUnitStatistics.add(identifierRecordValues);
        }
        return compilationUnitStatistics;
    }
}
