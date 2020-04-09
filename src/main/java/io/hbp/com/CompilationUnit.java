package io.hbp.com;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompilationUnit
{
    String id;
    List<Identifier> identifiers = new ArrayList<>();

    List<Violation> compilationUnitViolations;
    List<Violation> repositoryViolations;

    long compilationUnitLOC;
    long repositoryLOC;

    CompilationUnit() {}

    CompilationUnit(String id)
    {
        this.id = id;
    }

    float avgLength()
    {
        if (identifiers.isEmpty())
        {
            return -1.0f;
        }
        int totalLength = 0;
        for (Identifier identifier : identifiers)
        {
            totalLength += identifier.length();
        }
        return (float) totalLength / identifiers.size();
    }

    float avgNumberOfWords()
    {
        if (identifiers.isEmpty())
        {
            return -1.0f;
        }
        int totalNumberOfWords = 0;
        for (Identifier identifier : identifiers)
        {
            totalNumberOfWords += identifier.numberOfWords();
        }
        return (float) totalNumberOfWords / identifiers.size();
    }

    float avgNumberOfNumbers()
    {
        if (identifiers.isEmpty())
        {
            return -1.0f;
        }
        int totalNumberOfNumbers = 0;
        for (Identifier identifier : identifiers)
        {
            totalNumberOfNumbers += identifier.numberOfNumbers();
        }
        return (float) totalNumberOfNumbers / identifiers.size();
    }

    float casingConsistency()
    {
        Map<Identifier.Type, List<Identifier>> identifiersGroupedByType = identifiers
                .stream()
                .collect(Collectors.groupingBy(x -> x.type));

        if (identifiersGroupedByType.isEmpty())
        {
            return 1.0f;
        }

        float consistencyAccumulated = 0.0f;

        for (List<Identifier> identifierGroup : identifiersGroupedByType.values())
        {
            if (identifiers.isEmpty())
            {
                return 1.0f;
            }

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

            float localConsistency = sum == 0 ? 1.0f : (float) max / sum;

            consistencyAccumulated += localConsistency;
        }

        return consistencyAccumulated / identifiersGroupedByType.values().size();
    }

    List<Statistic> statistics()
    {
        return Arrays.asList
            (
                new Statistic("id", id),
                new Statistic("compilation_unit_violations", compilationUnitViolations == null ? 0 : compilationUnitViolations.size()),
                new Statistic("repository_violations", repositoryViolations == null ? 0 : repositoryViolations.size()),
                new Statistic("avg_length", avgLength()),
                new Statistic("avg_words", avgNumberOfWords()),
                new Statistic("avg_numbers", avgNumberOfNumbers()),
                new Statistic("casing_consistency", casingConsistency()),
                new Statistic("compilation_unit_loc", compilationUnitLOC),
                new Statistic("repository_loc", repositoryLOC)
            );
    }
}
