package io.hbp.com;

import java.util.List;

class IdentifiersCharacteristics
{
    static String averageIdentifierLength(List<String> identifiers)
    {
        if (identifiers.size() == 0) return String.valueOf(0);
        return String.valueOf(identifiers.stream().mapToInt(String::length).sum() / (float) identifiers.size());
    }

    static String casingConsistency(List<String> identifiers)
    {
        int numberOfCamel = 0;
        int numberOfPascal = 0;
        int numberOfUnderline = 0;
        int numberOfHungarian = 0;
        int numberOfOther = 0;

        for (String identifier : identifiers)
        {
            // Regexes from:
            //  "How are Identifiers Named in Open Source Software On Popularity and Consistency"
            //  Authors: Yanqing Wang a* , Chong Wang b , Xiaojie Li a , Sijing Yun a , Minjing Song

            // Camel case
            if (identifier.matches("\\b(([a-z]+([A-Z][a-z]*)+)|[a-z]{2,})\\d*\\b"))
            {
                numberOfCamel++;
            }
            // Pascal
            else if (identifier.matches("\\b([A-Z][a-z]+)+\\d*\\b"))
            {
                numberOfPascal++;
            }
            // Underline
            else if (identifier.matches("\\b(([a-z]+(\\d*)+_)+([a-z]*\\d*)+)\\b"))
            {
                numberOfUnderline++;
            }
            // Hungarian
            else if (identifier.matches("\\b([gmcs]_)?(p|fn|v|h|l|b|f|dw|sz|n|d|c|ch|i|by|w|r|u)(Max|Min|Init|T|Src|Dest)?([A-Z][a-z]+)+\\d*\\b"))
            {
                numberOfHungarian++;
            }
            else
            {
                numberOfOther++;
            }
        }

        int max =
                Integer.max(numberOfCamel,
                        Integer.max(numberOfPascal,
                                Integer.max(numberOfUnderline,
                                        Integer.max(numberOfHungarian, numberOfOther))));


        int sum =  numberOfCamel + numberOfPascal + numberOfUnderline + numberOfHungarian + numberOfOther;

        float avgConsistency = sum == 0 ? 1.0f : (float) max / sum;

        return String.valueOf(avgConsistency);
    }

    static String averageNumberOfNumbers(List<String> identifiers)
    {
        if (identifiers.isEmpty()) return String.valueOf(0.0f);
        float avgNumberOfNumbers = 0.0f;
        for (String identifier : identifiers)
        {
            char[] chars = identifier.toCharArray();
            for (char c : chars)
            {
                if (Character.isDigit(c))
                {
                    avgNumberOfNumbers++;
                }
            }
        }
        return String.valueOf(avgNumberOfNumbers / (float) identifiers.size());
    }

    // A word is split with underscores, casing changes, or numbers.
    static String averageNumberOfWords(List<String> identifiers)
    {
        int numberOfWordsSum = 0;
        for (String identifier : identifiers)
        {
            char[] chars = identifier.toCharArray();

            numberOfWordsSum++;

            if (chars.length < 2)
            {
                continue;
            }

            char prev = chars[0];
            for (int i = 1;  i < chars.length; i++)
            {
                char current = chars[i];
                if (hasCasingChanged(prev, current))
                {
                    numberOfWordsSum++;
                }
            }
        }
        return String.valueOf(numberOfWordsSum);
    }

    private static boolean hasCasingChanged(char left, char right)
    {
        if (Character.isDigit(left) && Character.isDigit(right))
        {
            return false;
        }
        if (Character.isLowerCase(left) && Character.isLowerCase(right))
        {
            return false;
        }
        if (Character.isUpperCase(left) && Character.isUpperCase(left))
        {
            return false;
        }
        if (left == '_' && right == '_')
        {
            return false;
        }
        return true;
    }
}
