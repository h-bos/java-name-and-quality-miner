package io.hbp.com;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IdentifierGroup
{
    public enum EntityType
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

        EntityType(String type)
        {
            this.value = type;
        }
    }

    public List<Identifier> identifiers;
    public EntityType entityType;

    public IdentifierGroup(List<Identifier> identifiers, EntityType entityType)
    {
        this.identifiers = new ArrayList<>();
        this.identifiers.addAll(identifiers);
        this.entityType = entityType;
    }

    public float averageLength()
    {
        return identifiers.stream().mapToInt(identifier -> identifier.name.length()).sum() / (float) identifiers.size();
    }

    public float averageNumberOfNumbers()
    {
        return identifiers.stream().mapToInt(Identifier::numberOfNumbers).sum() / (float) identifiers.size();
    }

    public float averageNumberOfWords()
    {
        return identifiers.stream().mapToInt(Identifier::numberOfWords).sum() / (float) identifiers.size();
    }

    public float casingConsistency()
    {
        int numberOfCamel = 0;
        int numberOfPascal = 0;
        int numberOfUnderline = 0;
        int numberOfHungarian = 0;
        int numberOfOther = 0;

        for (Identifier identifier : identifiers)
        {
            // Regexes from:
            //  "How are Identifiers Named in Open Source Software On Popularity and Consistency"
            //  Authors: Yanqing Wang a* , Chong Wang b , Xiaojie Li a , Sijing Yun a , Minjing Song
            // Camel case
            switch (identifier.casingType())
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

        return max / (float) sum;
    }

    public String names()
    {
        return identifiers.stream().map(identifier -> identifier.name).collect(Collectors.joining("|"));
    }
}
