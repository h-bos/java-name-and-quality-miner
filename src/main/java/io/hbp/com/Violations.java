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
        long numberOfViolations = 0;
        switch (qualityType)
        {
            case SECURITY:
                numberOfViolations = numberOfViolations(pmdRecords, "Security");
                break;
            case PERFORMANCE:
                numberOfViolations = numberOfViolations(pmdRecords, "Performance");
                break;
            case MAINTAINABILITY:
                numberOfViolations =
                    numberOfViolations(pmdRecords, "Best Practices") +
                    numberOfViolations(pmdRecords, "Code Style") +
                    numberOfViolations(pmdRecords, "Documentation") +
                    numberOfViolations(pmdRecords, "Design");
                break;
            case RELIABILITY:
                numberOfViolations =
                    numberOfViolations(pmdRecords, "Multithreading") +
                    numberOfViolations(pmdRecords, "Error Prone");
                break;
        }
        Log.info("Violations found " + String.valueOf(numberOfViolations));
        return String.valueOf(numberOfViolations);
    }

    static String totalNumberOfViolations(List<PmdRecord> pmdRecords)
    {
        return String.valueOf(pmdRecords.size());
    }
}
