package io.hbp.com;

import java.util.*;

class ViolationMaps
{
    Map<String, List<Violation>> compilationUnitToViolations = new HashMap<>();
    Map<String, List<Violation>> repositoryToViolations = new HashMap<>();

    ViolationMaps(List<Violation> violations)
    {
        for (Violation violation : violations)
        {
            // Class -> [Violation]
            if (!compilationUnitToViolations.containsKey(violation.compilationUnitId)) {
                List<Violation> initialViolationList = new ArrayList<>();
                initialViolationList.add(violation);
                compilationUnitToViolations.put(violation.compilationUnitId, initialViolationList);
            }
            else
            {
                compilationUnitToViolations.get(violation.compilationUnitId).add(violation);
            }

            // Repository -> [Violation]
            if (!repositoryToViolations.containsKey(violation.projectName))
            {
                List<Violation> initialViolationList = new ArrayList<>();
                initialViolationList.add(violation);
                repositoryToViolations.put(violation.projectName, initialViolationList);
            }
            else
            {
                repositoryToViolations.get(violation.projectName).add(violation);
            }
        }
    }

    static String averagePriority(List<Violation> violations)
    {
        if (violations.isEmpty()) return String.valueOf(0);
        return String.valueOf(violations.stream().mapToInt(record -> record.priority).sum() / (float) violations.size());
    }

    static long numberOfViolations(List<Violation> violations, String ruleSet)
    {
        if (violations.isEmpty()) return 0;
        return violations.stream().filter(record -> record.ruleSet.equals(ruleSet)).count();
    }

    static String numberOfViolations(List<Violation> violations, QualityType qualityType)
    {
        long numberOfViolations = 0;
        switch (qualityType)
        {
            case SECURITY:
                numberOfViolations = numberOfViolations(violations, "Security");
                break;
            case PERFORMANCE:
                numberOfViolations = numberOfViolations(violations, "Performance");
                break;
            case MAINTAINABILITY:
                numberOfViolations =
                    numberOfViolations(violations, "Best Practices") +
                    numberOfViolations(violations, "Code Style") +
                    numberOfViolations(violations, "Documentation") +
                    numberOfViolations(violations, "Design");
                break;
            case RELIABILITY:
                numberOfViolations =
                    numberOfViolations(violations, "Multithreading") +
                    numberOfViolations(violations, "Error Prone");
                break;
        }
        Log.info("Violations found " + String.valueOf(numberOfViolations));
        return String.valueOf(numberOfViolations);
    }

    static int totalNumberOfViolations(List<Violation> violations)
    {
        return violations.size();
    }
}
