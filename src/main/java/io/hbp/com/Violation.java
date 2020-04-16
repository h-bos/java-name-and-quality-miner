package io.hbp.com;

import net.sourceforge.pmd.RuleViolation;

public class Violation
{
    public enum QualityCharacteristic
    {
        SECURITY, MAINTAINABILITY, PERFORMANCE, RELIABILITY
    }

    public String ruleSet;

    public QualityCharacteristic qualityCharacteristic;

    public Violation(RuleViolation pmdViolation)
    {
        ruleSet = pmdViolation.getRule().getRuleSetName();

        switch (ruleSet)
        {
            case "Security":
                qualityCharacteristic = QualityCharacteristic.SECURITY;
                break;
            case "Performance":
                qualityCharacteristic = QualityCharacteristic.PERFORMANCE;
                break;
            case "Best Practices":
            case "Code Style":
            case "Documentation":
            case "Design":
                qualityCharacteristic = QualityCharacteristic.MAINTAINABILITY;
                break;
            case "Multithreading":
            case "Error Prone":
                qualityCharacteristic = QualityCharacteristic.RELIABILITY;
                break;
        }
    }
}
