package io.hbp.com;

import java.util.*;
import java.util.stream.Collectors;

/*
Independent variables:
    * Avg length of: methods, classes, fields, parameters
    * Casing consistency: methods, classes, fields, parameters
    * Number of words: methods, classes, fields, parameters
    * Number of numbers: methods, classes, fields, parameters
    * Repository LOC

Dependent variables:
    * Number of violations
    * Number of violations in category X
    * Average violation priority
    TODO:
    * WMC
    * CBO
    * DIT
    * RFC
    * LCOM
*/

class Repository
{
    String id;

    List<CompilationUnit> compilationUnits = new ArrayList<>();

    List<Violation> violations;

    long repositoryLOC;

    Repository() {}

    Repository(String id)
    {
       this.id = id;
    }

    float avgLength()
    {
        if (compilationUnits.isEmpty()) return -1.0f;

        int totalLength = 0;
        int numberOfIdentifiers = 0;
        for (CompilationUnit compilationUnit : compilationUnits)
        {
            List<Identifier> identifiers = compilationUnit.identifiers;
            numberOfIdentifiers += identifiers.size();
            for (Identifier identifier : identifiers)
            {
                totalLength += identifier.length();
            }
        }
        return (float) totalLength / numberOfIdentifiers;
    }

    float avgNumberOfWords()
    {
        if (compilationUnits.isEmpty()) return -1.0f;

        int totalNumberOfWords = 0;
        int numberOfIdentifiers = 0;
        for (CompilationUnit compilationUnit : compilationUnits)
        {
            List<Identifier> identifiers = compilationUnit.identifiers;
            numberOfIdentifiers += identifiers.size();
            for (Identifier identifier : identifiers)
            {
                totalNumberOfWords += identifier.numberOfWords();
            }
        }
        return (float) totalNumberOfWords / numberOfIdentifiers;
    }

    float avgNumberOfNumbers()
    {
        if (compilationUnits.isEmpty()) return -1.0f;

        int totalNumberOfWords = 0;
        int numberOfIdentifiers = 0;
        for (CompilationUnit compilationUnit : compilationUnits)
        {
            List<Identifier> identifiers = compilationUnit.identifiers;
            numberOfIdentifiers += identifiers.size();
            for (Identifier identifier : identifiers)
            {
                totalNumberOfWords += identifier.numberOfNumbers();
            }
        }
        return (float) totalNumberOfWords / numberOfIdentifiers;
    }

    float casingConsistency()
    {
        if (compilationUnits.isEmpty()) return -1.0f;

        Map<Identifier.Type, List<Identifier>> identifiersGroupByType = compilationUnits
                .stream()
                .map(x -> x.identifiers)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(x -> x.type));

        if (identifiersGroupByType.isEmpty())
        {
            return 1.0f;
        }

        float consistencyAccumulated = 0.0f;

        for (List<Identifier> identifierGroup : identifiersGroupByType.values())
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

            int sum = numberOfCamel + numberOfPascal + numberOfUnderline + numberOfHungarian + numberOfOther;

            float localConsistency = sum == 0 ? 1.0f : (float) max / sum;

            consistencyAccumulated += localConsistency;
        }

        return consistencyAccumulated / identifiersGroupByType.values().size();
    }

    List<Statistic> statistics()
    {
        return Arrays.asList
            (
                new Statistic("id", id),
                new Statistic("violations", violations == null ? 0 : violations.size()),
                new Statistic("avg_length", avgLength()),
                new Statistic("avg_words", avgNumberOfWords()),
                new Statistic("avg_numbers", avgNumberOfNumbers()),
                new Statistic("casing_consistency", casingConsistency()),
                new Statistic("repository_loc", repositoryLOC)
            );
    }
}
