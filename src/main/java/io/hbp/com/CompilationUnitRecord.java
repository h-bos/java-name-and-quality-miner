package io.hbp.com;

import java.util.ArrayList;
import java.util.List;

public class CompilationUnitRecord
{
    public static final String CSV_HEADER = "avg_class_or_interface_name_length,number_of_methods,method_casing_consistency,number_of_violations,log_violations,has_violation";

    // {repo-name}\...\file.java
    String compilationUnitId;

    int numberOfViolations = 0;

    List<String> classOrInterfaceNames = new ArrayList<>();
    List<String> methodNames = new ArrayList<>();

    public CompilationUnitRecord(String compilationUnitId)
    {
        this.compilationUnitId = compilationUnitId;
    }

    public String asCsvRow()
    {
        return getAverageClassOrInterfaceNameLength() + "," + methodNames.size() + "," + methodNamingConsistency() + "," + numberOfViolations + "," + logViolation() + "," + hasViolation();
    }

    private int getAverageClassOrInterfaceNameLength()
    {
        if (classOrInterfaceNames.size() == 0) return 0;
        return classOrInterfaceNames.stream().mapToInt(String::length).sum() / classOrInterfaceNames.size();
    }

    private float methodNamingConsistency()
    {
        int numberOfCamel = 0;
        int numberOfPascal = 0;
        int numberOfUnderline = 0;
        int numberOfHungarian = 0;
        int numberOfOther = 0;

        for (String methodName : methodNames)
        {
            // Camel case
            if (methodName.matches("\\b(([a-z]+([A-Z][a-z]*)+)|[a-z]{2,})\\d*\\b"))
            {
                numberOfCamel++;
            }
            // Pascal
            else if (methodName.matches("\\b([A-Z][a-z]+)+\\d*\\b"))
            {
                numberOfPascal++;
            }
            // Underline
            else if (methodName.matches("\\b(([a-z]+(\\d*)+_)+([a-z]*\\d*)+)\\b"))
            {
                numberOfUnderline++;
            }
            // Hungarian
            else if (methodName.matches("\\b([gmcs]_)?(p|fn|v|h|l|b|f|dw|sz|n|d|c|ch|i|by|w|r|u)(Max|Min|Init|T|Src|Dest)?([A-Z][a-z]+)+\\d*\\b"))
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

        float consistency;

        if (sum == 0)
        {
            consistency = 1.0f;
        }
        else
        {
            consistency = (float) max / sum;
        }

        return consistency;
    }

    private int hasViolation()
    {
        return numberOfViolations == 0 ? 0 : 1;
    }

    private double logViolation()
    {
        return (numberOfViolations == 0) ? numberOfViolations : Math.log(numberOfViolations);
    }
}
