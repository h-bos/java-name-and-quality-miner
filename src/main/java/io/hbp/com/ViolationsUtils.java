package io.hbp.com;

import java.util.*;

class ViolationsUtils
{
    static float averagePriority(List<PmdRecord> pmdRecords)
    {
        if (pmdRecords.isEmpty()) return 0;
        return pmdRecords.stream().mapToInt(record -> record.priority).sum() / (float) pmdRecords.size();
    }

    static float numberOfViolations(List<PmdRecord> pmdRecords, String ruleSet)
    {
        if (pmdRecords.isEmpty()) return 0;
        return pmdRecords.stream().filter(record -> record.ruleSet == ruleSet).count();
    }

    static float totalNumberOfViolations(List<PmdRecord> pmdRecords)
    {
        return pmdRecords.size();
    }
}
