package io.hbp.com;

import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

public class Identifier
{
    public enum Type
    {
        ENUM("enum"),
        ENUM_CONSTANT("enum_constant"),
        INTERFACE("interface"),
        CLASS("class"),
        FIELD("field"),
        METHOD("method"),
        PARAMETER("parameter"),
        LOCAL_VARIABLE("variable");

        String value;

        Type(String type)
        {
            this.value = type;
        }
    }

    public enum CasingType
    {
        CAMEL, PASCAL, UNDERLINE, HUNGARIAN, OTHER
    }

    public String name;

    public Type type;

    public float casingConsistency;

    public Identifier(String name, Type type)
    {
        this.name = name;
        this.type = type;
    }

    public CasingType type()
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

    public int numberOfWords()
    {
        if (name.length() <= 0)
        {
            return 0;
        }

        if (name.length() <= 1)
        {
            return 1;
        }

        if (name.length() <= 2)
        {
            return 2;
        }

        char[] chars = name.toCharArray();

        int numberOfWords = 0;

        int i = 1;
        while (i < chars.length - 1)
        {
            ++numberOfWords;
            while ((i + 1) < chars.length - 1 && !hasCasingChanged(chars[i], chars[i + 1]) && !isSplitter(chars[i + 1]))
            {
                ++i;
            }
            if (isSplitter(chars[i + 1]))
            {
                i += 2;
            }
            else
            {
                ++i;
            }
        }

        return numberOfWords;
    }

    public int numberOfNumbers()
    {
        if (name.length() <= 0)
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
        return true;
    }

    private static boolean isSplitter(char c)
    {
        return c == '_' || c == '-';
    }
}
