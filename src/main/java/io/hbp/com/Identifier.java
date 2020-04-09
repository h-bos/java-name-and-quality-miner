package io.hbp.com;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Identifier
{
    enum Type
    {
        CLASS_INTERFACE_ENUM("class_interface_enum"), METHOD("method"), FIELD("field"), PARAMETER("parameter");

        String value;

        Type(String type)
        {
            this.value = type;
        }
    }

    enum CasingType
    {
        CAMEL, PASCAL, UNDERLINE, HUNGARIAN, OTHER
    }

    String name;

    int numberOfCompilationUnitViolations;
    int numberOfRepositoryViolations;

    long compilationUnitLOC;
    long repositoryLOC;

    Type type;

    Identifier() {}

    Identifier(String name, Type type)
    {
        this.name = name;
        this.type = type;
    }

    CasingType type()
    {
        // Regexes from:
        //  "How are Identifiers Named in Open Source Software On Popularity and Consistency"
        //  Authors: Yanqing Wang a* , Chong Wang b , Xiaojie Li a , Sijing Yun a , Minjing Song
        if (name.matches("\\b(([a-z]+([A-Z][a-z]*)+)|[a-z]{2,})\\d*\\b"))
        {
            return CasingType.CAMEL;
        }
        else if (name.matches("\\b([A-Z][a-z]+)+\\d*\\b"))
        {
            return CasingType.PASCAL;
        }
        else if (name.matches("\\b(([a-z]+(\\d*)+_)+([a-z]*\\d*)+)\\b"))
        {
            return CasingType.UNDERLINE;
        }
        else if (name.matches("\\b([gmcs]_)?(p|fn|v|h|l|b|f|dw|sz|n|d|c|ch|i|by|w|r|u)(Max|Min|Init|T|Src|Dest)?([A-Z][a-z]+)+\\d*\\b"))
        {
            return CasingType.HUNGARIAN;
        }
        return CasingType.OTHER;
    }

    int length()
    {
        return name == null ? 0 : name.length();
    }

    int numberOfWords()
    {
        if (length() <= 0)
        {
            return 0;
        }

        int numberOfWords = 0;
        char[] chars = name.toCharArray();

        numberOfWords++;

        if (chars.length < 2)
        {
            return numberOfWords;
        }

        char prev = chars[0];
        for (int i = 1;  i < chars.length; i++)
        {
            char current = chars[i];
            if (hasCasingChanged(prev, current))
            {
                numberOfWords++;
            }
        }

        return numberOfWords;
    }

    int numberOfNumbers()
    {
        if (length() <= 0)
        {
            return 0;
        }

        int numberOfNumbers = 0;
        char[] chars = name.toCharArray();
        for (char c : chars)
        {
            if (Character.isDigit(c))
            {
                numberOfNumbers ++;
            }
        }
        return numberOfNumbers;
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

    List<Statistic> statistics()
    {
        return Arrays.asList
            (
                new Statistic("id", name),
                new Statistic("compilation_unit_violations", numberOfCompilationUnitViolations),
                new Statistic("repository_violations", numberOfRepositoryViolations),
                new Statistic("type", type == null ? "NA" : type.value),
                new Statistic("length", length()),
                new Statistic("words", numberOfWords()),
                new Statistic("numbers", numberOfNumbers()),
                new Statistic("compilation_unit_loc", compilationUnitLOC),
                new Statistic("repository_loc", repositoryLOC)
            );
    }
}
