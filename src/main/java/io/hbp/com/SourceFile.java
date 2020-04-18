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

    public List<List<Object>> getIdentifierRecords()
    {
        evaluateCasingConsistency();

        List<List<Object>> sourceFileStatistics = new ArrayList<>();
        for (Identifier identifier : identifiers)
        {
            List<Object> identifierRecordValues = new ArrayList<>();
            identifierRecordValues.add(fileName);
            identifierRecordValues.add(identifier.name);
            identifierRecordValues.add(identifier.type.value);
            identifierRecordValues.add(identifier.name.length());
            identifierRecordValues.add(identifier.numberOfWords());
            identifierRecordValues.add(identifier.numberOfNumbers());
            identifierRecordValues.add(identifier.casingConsistency);
            identifierRecordValues.add(linesOfCode);
            identifierRecordValues.add(violations.size());
            identifierRecordValues.add(parsedSuccessfully);
            sourceFileStatistics.add(identifierRecordValues);
        }
        return sourceFileStatistics;
    }
}
