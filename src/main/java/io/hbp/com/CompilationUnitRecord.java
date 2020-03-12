package io.hbp.com;

import java.util.ArrayList;
import java.util.List;

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

class CompilationUnitRecord
{
    public static final String CSV_HEADER =
        "avg_length," +
        "avg_length_class_or_interface," +
        "avg_length_method," +
        "avg_length_field," +
        "avg_length_parameter," +

        "avg_casing_consistency," +
        "avg_casing_consistency_class_or_interface," +
        "avg_casing_consistency_method," +
        "avg_casing_consistency_field," +
        "avg_casing_consistency_parameter," +

        "avg_number_of_words," +
        "avg_number_of_words_class_or_interface," +
        "avg_number_of_words_method," +
        "avg_number_of_words_field," +
        "avg_number_of_words_parameter," +

        "avg_number_of_numbers," +
        "avg_number_of_numbers_class_or_interface," +
        "avg_number_of_numbers_method," +
        "avg_number_of_numbers_field," +
        "avg_number_of_numbers_parameter," +

        "project_loc," +
        "number_of_violations," +

        "number_of_best_practices_violations," +
        "number_of_code_style_violations," +
        "number_of_design_violations," +
        "number_of_documentation_violations," +
        "number_of_error_prone_violations," +
        "number_of_multithreading_violations," +
        "number_of_performance_violations," +
        "number_of_security_violations," +

        "avg_violation_priority";

    // {repo-name}\...\file.java
    String compilationUnitId;

    // Dependent variables
    int numberOfViolations = 0;

    // Independent variables
    long projectLOC = 0L;

    // Compilation unit properties that can be used to find independent variables
    List<String> classOrInterfaceNames = new ArrayList<>();
    List<String> methodNames           = new ArrayList<>();
    List<String> fieldNames            = new ArrayList<>();
    List<String> parameterNames        = new ArrayList<>();

    // PMD violations
    List<PmdRecord> pmdRecords = new ArrayList<>();

    CompilationUnitRecord(String compilationUnitId)
    {
        this.compilationUnitId = compilationUnitId;
    }

    public String asCsvRow()
    {
        return
            IdentifierUtils.averageIdentifierLength(ListUtils.join(classOrInterfaceNames, methodNames, fieldNames, parameterNames)) + "," +
            IdentifierUtils.averageIdentifierLength(classOrInterfaceNames) + "," +
            IdentifierUtils.averageIdentifierLength(methodNames) + "," +
            IdentifierUtils.averageIdentifierLength(fieldNames) + "," +
            IdentifierUtils.averageIdentifierLength(parameterNames) + "," +

            IdentifierUtils.casingConsistency(ListUtils.join(classOrInterfaceNames, methodNames, fieldNames, parameterNames)) + "," +
            IdentifierUtils.casingConsistency(classOrInterfaceNames) + "," +
            IdentifierUtils.casingConsistency(methodNames) + "," +
            IdentifierUtils.casingConsistency(fieldNames) + "," +
            IdentifierUtils.casingConsistency(parameterNames) + "," +

            IdentifierUtils.averageNumberOfWords(ListUtils.join(classOrInterfaceNames, methodNames, fieldNames, parameterNames)) + "," +
            IdentifierUtils.averageNumberOfWords(classOrInterfaceNames) + "," +
            IdentifierUtils.averageNumberOfWords(methodNames) + "," +
            IdentifierUtils.averageNumberOfWords(fieldNames) + "," +
            IdentifierUtils.averageNumberOfWords(parameterNames) + "," +

            IdentifierUtils.averageNumberOfNumbers(ListUtils.join(classOrInterfaceNames, methodNames, fieldNames, parameterNames)) + "," +
            IdentifierUtils.averageNumberOfNumbers(classOrInterfaceNames) + "," +
            IdentifierUtils.averageNumberOfNumbers(methodNames) + "," +
            IdentifierUtils.averageNumberOfNumbers(fieldNames) + "," +
            IdentifierUtils.averageNumberOfNumbers(parameterNames) + "," +

            projectLOC + "," +
            numberOfViolations + "," +

            ViolationsUtils.numberOfViolations(pmdRecords, "Best Practices") + "," +
            ViolationsUtils.numberOfViolations(pmdRecords, "Code Style") + "," +
            ViolationsUtils.numberOfViolations(pmdRecords, "Design") + "," +
            ViolationsUtils.numberOfViolations(pmdRecords, "Documentation") + "," +
            ViolationsUtils.numberOfViolations(pmdRecords, "Error Prone") + "," +
            ViolationsUtils.numberOfViolations(pmdRecords, "Multithreading") + "," +
            ViolationsUtils.numberOfViolations(pmdRecords, "Performance") + "," +
            ViolationsUtils.numberOfViolations(pmdRecords, "Security") + "," +

            ViolationsUtils.averagePriority(pmdRecords);
    }
}
