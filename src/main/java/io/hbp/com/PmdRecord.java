package io.hbp.com;

class PmdRecord
{
    // {package} + {className}
    // Ex. org.jboss.as.weld.deployment.processorsWeldDeploymentCleanupProcessor.java
    String compilationUnitId;

    String problem;
    String packageName;
    String file;
    // [1,5] low value => high priority
    int priority;
    int lineNumber;
    String description;
    String ruleSet;
    String ruleId;

    PmdRecord(String csvLine)
    {
        // First we remove the leftmost and rightmost quotes from the line. After that, the rest of the line can be
        // split on ",". Ex line: "16","concurrentDS",{...},"LocalVariableCouldBeFinal"
        csvLine = csvLine.substring(1, csvLine.length() - 1);
        String[] parts = csvLine.split("\",\"");
        problem     = parts[0];
        packageName = parts[1];
        file        = parts[2];
        priority    = Integer.parseInt(parts[3]);
        lineNumber  = Integer.parseInt(parts[4]);
        description = parts[5];
        ruleSet     = parts[6];
        ruleId      = parts[7];

        // Split file path and take the last part which is the file or root class name.
        String[] fileParts = file.split("\\\\");
        String rootClassName = fileParts[fileParts.length - 1];
        compilationUnitId = packageName + '.' + rootClassName;
    }
}
