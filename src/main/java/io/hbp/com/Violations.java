package io.hbp.com;

import java.util.*;
import java.util.stream.Collectors;

class Violations
{
    static String averagePriority(List<PmdRecord> pmdRecords)
    {
        if (pmdRecords.isEmpty()) return String.valueOf(0);
        return String.valueOf(pmdRecords.stream().mapToInt(record -> record.priority).sum() / (float) pmdRecords.size());
    }

    static long numberOfViolations(List<PmdRecord> pmdRecords, String ruleSet)
    {
        if (pmdRecords.isEmpty()) return 0;
        return pmdRecords.stream().filter(record -> record.ruleSet.equals(ruleSet)).count();
    }

    static String numberOfViolations(List<PmdRecord> pmdRecords, QualityType qualityType)
    {
        switch (qualityType)
        {
            case SECURITY:
                return String.valueOf(numberOfViolations(pmdRecords, "Security"));
            case PERFORMANCE:
                return String.valueOf(numberOfViolations(pmdRecords, "Performance"));
            case MAINTAINABILITY:
                return String.valueOf
                (
                    numberOfViolations(pmdRecords, "Best Practices") +
                    numberOfViolations(pmdRecords, "Code Style") +
                    numberOfViolations(pmdRecords, "Documentation") +
                    numberOfViolations(pmdRecords, "Design")
                );
            case RELIABILITY:
                return String.valueOf
                    (
                        numberOfViolations(pmdRecords, "Multithreading") +
                        numberOfViolations(pmdRecords, "Error Prone")
                    );
        }
        return "0";
    }

    static String totalNumberOfViolations(List<PmdRecord> pmdRecords)
    {
        return String.valueOf(pmdRecords.size());
    }
}
