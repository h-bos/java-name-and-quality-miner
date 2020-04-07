package io.hbp.com;

import java.util.*;

/*
Independent variables:
    * Avg length of: methods, classes, fields, parameters
    * Casing consistency: methods, classes, fields, parameters
    * Number of words: methods, classes, fields, parameters
    * Number of numbers: methods, classes, fields, parameters
    * Project LOC

Dependent variables:
    * Number of violations
    * Number of violations in category X
    * Average violation priority
    TODO:
    * WMC
    * CBO
    * DIT
    * RFC
    * LCOM
*/

class Project
{
    String projectId;

    // Independent variables
    long projectLOC = 0L;

    // Compilation unit properties that can be used to find independent variables
    List<String> classOrInterfaceNames = new ArrayList<>();
    List<String> methodNames           = new ArrayList<>();
    List<String> fieldNames            = new ArrayList<>();
    List<String> parameterNames        = new ArrayList<>();

    // PMD violations
    List<PmdRecord> pmdRecords = new ArrayList<>();

    Project(String projectId)
    {
        this.projectId = projectId;
    }

    List<Statistic> statistics()
    {
        List<Statistic> statistics = Arrays.asList
        (
            new Statistic("avg_length",                    IdentifiersCharacteristics.averageIdentifierLength(ListUtils.join(classOrInterfaceNames, methodNames, fieldNames, parameterNames))),
            // new Statistic("avg_length_class_or_interface", IdentifiersCharacteristics.averageIdentifierLength(classOrInterfaceNames)),
            // new Statistic("avg_length_method",             IdentifiersCharacteristics.averageIdentifierLength(methodNames)),
            // new Statistic("avg_length_field",              IdentifiersCharacteristics.averageIdentifierLength(fieldNames)),
            // new Statistic("avg_length_parameter",          IdentifiersCharacteristics.averageIdentifierLength(parameterNames)),

            new Statistic("avg_casing_consistency",                    IdentifiersCharacteristics.casingConsistency(ListUtils.join(classOrInterfaceNames, methodNames, fieldNames, parameterNames))),
            // new Statistic("avg_casing_consistency_class_or_interface", IdentifiersCharacteristics.casingConsistency(classOrInterfaceNames)),
            // new Statistic("avg_casing_consistency_method",             IdentifiersCharacteristics.casingConsistency(methodNames)),
            // new Statistic("avg_casing_consistency_field",              IdentifiersCharacteristics.casingConsistency(fieldNames)),
            // new Statistic("avg_casing_consistency_parameter",          IdentifiersCharacteristics.casingConsistency(parameterNames)),

            new Statistic("avg_number_of_words",                    IdentifiersCharacteristics.averageNumberOfWords(ListUtils.join(classOrInterfaceNames, methodNames, fieldNames, parameterNames))),
            // new Statistic("avg_number_of_words_class_or_interface", IdentifiersCharacteristics.averageNumberOfWords(classOrInterfaceNames)),
            // new Statistic("avg_number_of_words_method",             IdentifiersCharacteristics.averageNumberOfWords(methodNames)),
            // new Statistic("avg_number_of_words_field",              IdentifiersCharacteristics.averageNumberOfWords(fieldNames)),
            // new Statistic("avg_number_of_words_parameter",          IdentifiersCharacteristics.averageNumberOfWords(parameterNames)),

            new Statistic("avg_number_of_numbers",                    IdentifiersCharacteristics.averageNumberOfNumbers(ListUtils.join(classOrInterfaceNames, methodNames, fieldNames, parameterNames))),
            // new Statistic("avg_number_of_numbers_class_or_interface", IdentifiersCharacteristics.averageNumberOfNumbers(classOrInterfaceNames)),
            // new Statistic("avg_number_of_numbers_method",             IdentifiersCharacteristics.averageNumberOfNumbers(methodNames)),
            // new Statistic("avg_number_of_numbers_field",              IdentifiersCharacteristics.averageNumberOfNumbers(fieldNames)),
            // new Statistic("avg_number_of_numbers_parameter",          IdentifiersCharacteristics.averageNumberOfNumbers(parameterNames)),

            new Statistic("project_loc", String.valueOf(projectLOC)),

            new Statistic("p_number_of_violations", Violations.totalNumberOfViolations(pmdRecords)),

            new Statistic("p_number_of_violations_security",        Violations.numberOfViolations(pmdRecords, QualityType.SECURITY)),
            new Statistic("p_number_of_violations_maintainability", Violations.numberOfViolations(pmdRecords, QualityType.MAINTAINABILITY)),
            new Statistic("p_number_of_violations_performance",     Violations.numberOfViolations(pmdRecords, QualityType.PERFORMANCE)),
            new Statistic("p_number_of_violations_reliability",     Violations.numberOfViolations(pmdRecords, QualityType.RELIABILITY)),

            new Statistic("number_of_identifiers", String.valueOf(classOrInterfaceNames.size() + methodNames.size() + fieldNames.size() + parameterNames.size())),

            new Statistic("number_of_identifiers_class_or_interface", String.valueOf(classOrInterfaceNames.size())),
            new Statistic("number_of_identifiers_method", String.valueOf(methodNames.size())),
            new Statistic("number_of_identifiers_field", String.valueOf(fieldNames.size())),
            new Statistic("number_of_identifiers_parameter", String.valueOf(parameterNames.size()))
        );
        return statistics;
    }
}
