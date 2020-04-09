package io.hbp.com;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class Main
{
    static String REPOSITORY_STATS_FILE = "repositories.csv";
    static String COMPILATION_UNIT_STATS_FILE = "compilation-units.csv";
    static String IDENTIFIER_STATS_FILE = "identifiers.csv";

    public static void main(String[] args)
    {
        ViolationMaps violationMaps = findViolationMaps("pmd-report-1000.csv");

        // Clear files
        clearFile(REPOSITORY_STATS_FILE);
        clearFile(COMPILATION_UNIT_STATS_FILE);
        clearFile(IDENTIFIER_STATS_FILE);

        // Write CSV headers
        appendToFile(IDENTIFIER_STATS_FILE, new Identifier().statistics().stream().map(statistic -> statistic.id).collect(Collectors.joining(",")));
        appendToFile(COMPILATION_UNIT_STATS_FILE, new io.hbp.com.CompilationUnit().statistics().stream().map(statistic -> statistic.id).collect(Collectors.joining(",")));
        appendToFile(REPOSITORY_STATS_FILE, new Repository().statistics().stream().map(statistic -> statistic.id).collect(Collectors.joining(",")));

        List<Repository> repositories = new ArrayList<>();
        File[] repositoryRootDirectories = new File("repositories/").listFiles(File::isDirectory);
        int numberOfRepositoriesChecked = 0;
        if (repositoryRootDirectories.length < 1)
        {
            return;
        }
        for (File repositoryDirectory : repositoryRootDirectories)
        {
            Repository repository = findRepository(repositoryDirectory.toPath());
            writeRepositoryStatisticsToFiles(repository, violationMaps);
            Log.info("Checked " + ++numberOfRepositoriesChecked + " out of " + repositoryRootDirectories.length);
        }

        Log.writeToFile("log.txt");
    }

    private static ViolationMaps findViolationMaps(String reportName)
    {
        List<Violation> violations = new ArrayList<>();
        try
        {
            Files.lines(Paths.get(".", reportName)).skip(1).forEach(line -> {
                violations.add(new Violation(line));
            });
        }
        catch (IOException e)
        {
            Log.error(e);
        }
        return new ViolationMaps(violations);
    }

    private static Repository findRepository(Path repositoryPath)
    {
        Log.info(repositoryPath.getFileName().toString());
        Repository repository = new Repository(repositoryPath.getFileName().toString());

        List<Path> paths;
        try
        {
            paths = Files.walk(repositoryPath)
                    .filter(path -> path.toAbsolutePath().toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            Log.error(e);
            return repository;
        }

        JavaParser parser = new JavaParser();

        long repositoryLOC = 0;

        for (Path path : paths)
        {
            ParseResult<CompilationUnit> parseResult = null;
            try
            {
                parseResult = parser.parse(path);
            }
            catch (Throwable th)
            {
                Log.info("File could not be parsed " + path.toString());
                continue;
            }

            if (!parseResult.isSuccessful()) {
                Log.info("Parse result was not successful " + path.toString());
                continue;
            }

            Optional<CompilationUnit> compilationUnitOptional = parseResult.getResult();

            if (!compilationUnitOptional.isPresent()) {
                Log.info("Compilation unit is not present " + path.toString());
                continue;
            }

            CompilationUnit javaParserCompilationUnit = compilationUnitOptional.get();

            String compilationUnitId = findCompilationUnitId(javaParserCompilationUnit, path);
            io.hbp.com.CompilationUnit compilationUnit = new io.hbp.com.CompilationUnit(compilationUnitId);

            // Find LOC of compilation unit
            long compilationUnitLOC = 0;
            try
            {
                compilationUnitLOC = Files.lines(path).count();
            }
            catch (Throwable e)
            {
                try
                {
                    compilationUnitLOC = Files.lines(path, Charset.forName("ISO-8859-1")).count();
                }
                catch (Throwable ex)
                {
                    try
                    {
                        compilationUnitLOC = Files.lines(path, Charset.forName("UTF-16")).count();
                    }
                    catch (Throwable exp)
                    {
                        Log.error(exp);
                    }
                }
            }
            compilationUnit.compilationUnitLOC = compilationUnitLOC;
            repositoryLOC += compilationUnitLOC;

            // Find identifiers
            // Classes and interfaces
            compilationUnit.identifiers.addAll(
                    javaParserCompilationUnit
                            .findAll(ClassOrInterfaceDeclaration.class)
                            .stream()
                            .map(x -> new Identifier(x.getNameAsString(), Identifier.Type.CLASS_INTERFACE_ENUM))
                            .collect(Collectors.toList()));

            // Enums
            compilationUnit.identifiers.addAll(
                    javaParserCompilationUnit
                            .findAll(EnumDeclaration.class)
                            .stream()
                            .map(x -> new Identifier(x.getNameAsString(), Identifier.Type.CLASS_INTERFACE_ENUM))
                            .collect(Collectors.toList()));

            // Methods
            compilationUnit.identifiers.addAll(
                    javaParserCompilationUnit
                        .findAll(MethodDeclaration.class)
                        .stream()
                        .map(x -> new Identifier(x.getNameAsString(), Identifier.Type.METHOD))
                        .collect(Collectors.toList()));

            // Fields
            compilationUnit.identifiers.addAll(
                    javaParserCompilationUnit.findAll(FieldDeclaration.class)
                            .stream()
                            .map(fieldDeclaration -> fieldDeclaration.getVariables())
                            .flatMap(Collection::stream)
                            .map(x -> new Identifier(x.getNameAsString(), Identifier.Type.FIELD))
                            .collect(Collectors.toList()));

            // Constructor parameters
            compilationUnit.identifiers.addAll(
                    javaParserCompilationUnit.findAll(ConstructorDeclaration.class)
                    .stream()
                    .map(constructorDeclaration -> constructorDeclaration.getParameters())
                    .flatMap(Collection::stream)
                    .map(x -> new Identifier(x.getNameAsString(), Identifier.Type.PARAMETER))
                    .collect(Collectors.toList()));

            // Method parameters
            compilationUnit.identifiers.addAll(
                    javaParserCompilationUnit.findAll(MethodDeclaration.class)
                    .stream()
                    .map(methodDeclaration -> methodDeclaration.getParameters())
                    .flatMap(Collection::stream)
                    .map(x -> new Identifier(x.getNameAsString(), Identifier.Type.PARAMETER))
                    .collect(Collectors.toList())
            );

            // Set compilation unit LOC of all identifiers in compilation unit
            for (Identifier identifier : compilationUnit.identifiers)
            {
                identifier.compilationUnitLOC = compilationUnitLOC;
            }

            repository.compilationUnits.add(compilationUnit);
        }

        // Set repository LOC to the current repository and its compilation units / identifiers.
        repository.repositoryLOC = repositoryLOC;
        for (io.hbp.com.CompilationUnit compilationUnit : repository.compilationUnits)
        {
            compilationUnit.repositoryLOC = repositoryLOC;
            for (Identifier identifier : compilationUnit.identifiers)
            {
                identifier.repositoryLOC = repositoryLOC;
            }
        }

        return repository;
    }

    private static String findCompilationUnitId(CompilationUnit compilationUnit, Path path)
    {
        String compilationUnitId;
        Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
        if (packageDeclaration.isPresent())
        {
            compilationUnitId = packageDeclaration.get().getNameAsString() + "." + path.getFileName().toString();
        }
        else
        {
            compilationUnitId = path.getFileName().toString();
        }
        // Fixes CSV issues.
        compilationUnitId = compilationUnitId.replace(",", "");
        return compilationUnitId;
    }

    private static void writeRepositoryStatisticsToFiles(Repository repository, ViolationMaps violationMaps)
    {
        repository.violations = violationMaps.repositoryToViolations.get(repository.id);
        for (io.hbp.com.CompilationUnit compilationUnit : repository.compilationUnits)
        {
            compilationUnit.repositoryViolations = violationMaps.repositoryToViolations.get(repository.id);
            compilationUnit.compilationUnitViolations = violationMaps.compilationUnitToViolations.get(compilationUnit.id);
            for (Identifier identifier : compilationUnit.identifiers)
            {
                identifier.numberOfCompilationUnitViolations = violationMaps.compilationUnitToViolations.get(compilationUnit.id) == null ?
                        0 : violationMaps.compilationUnitToViolations.get(compilationUnit.id).size();
                identifier.numberOfRepositoryViolations = violationMaps.repositoryToViolations.get(repository.id) == null ?
                        0 : violationMaps.repositoryToViolations.get(repository.id).size();
            }
        }

        appendStatisticsToFile(REPOSITORY_STATS_FILE, Arrays.asList(repository.statistics()));
        List<List<Statistic>> compilationUnitStatistics = repository
                .compilationUnits
                .stream()
                .map(x -> x.statistics())
                .collect(Collectors.toList());
        appendStatisticsToFile(COMPILATION_UNIT_STATS_FILE, compilationUnitStatistics);
        List<List<Statistic>> identifierStatistics =
                repository.compilationUnits
                        .stream()
                        .map(x -> x.identifiers)
                        .flatMap(Collection::stream)
                        .map(x -> x.statistics())
                        .collect(Collectors.toList());
        appendStatisticsToFile(IDENTIFIER_STATS_FILE, identifierStatistics);
    }

    private static void appendStatisticsToFile(String fileName, List<List<Statistic>> statistics)
    {
        if (statistics.size() < 1) return;
        List<String> lines = new ArrayList<>();
        for (List<Statistic> line : statistics)
        {
            lines.add(line.stream().map(statistic -> statistic.value).collect(Collectors.joining(",")));
        }
        appendToFile(fileName, lines);
    }

    private static void appendToFile(String fileName, List<String> lines)
    {
        Path file = Paths.get(fileName);
        try
        {
            Files.write(file, lines, StandardOpenOption.APPEND);
            Log.info("Appended " + lines.size() + " records to " + fileName);
        }
        catch (IOException e)
        {
            Log.error(e);
        }
    }

    private static void appendToFile(String fileName, String line)
    {
        Path file = Paths.get(fileName);
        try
        {
            Files.writeString(file, line + "\n", StandardOpenOption.APPEND);
            Log.info("Appended header record to " + fileName);
        }
        catch (IOException e)
        {
            Log.error(e);
        }
    }

    private static void clearFile(String fileName)
    {
        try
        {
            new PrintWriter(fileName).close();
        }
        catch (FileNotFoundException e)
        {
            Log.error(e);
        }
    }
}
