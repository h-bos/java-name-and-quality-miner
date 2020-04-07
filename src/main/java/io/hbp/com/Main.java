package io.hbp.com;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main
{
    public static void main(String[] args)
    {
        Map<String, List<PmdRecord>> pmdRecords = findPmdRecords();
        Map<String, Project> projects = findProjects();

        // Add PMD violations to the compilation units that have them.
        for (Map.Entry<String, List<PmdRecord>> pmdRecord : pmdRecords.entrySet())
        {
            if (projects.containsKey(pmdRecord.getKey()))
            {
                projects.get(pmdRecord.getKey()).pmdRecords.addAll(pmdRecord.getValue());
            }
        }

        // Resulting records are all the values of the compilation units hash map
        List<Project> resultingProjects = projects
                .entrySet()
                .stream().map(x -> x.getValue())
                .collect(Collectors.toList());

        List<List<Statistic>> projectStatistics = resultingProjects
                .stream()
                .map(compilationUnitRecord -> compilationUnitRecord.statistics())
                .collect(Collectors.toList());

        if (projectStatistics.size() < 1) return;

        String header = projectStatistics.get(0).stream().map(statistic -> statistic.id).collect(Collectors.joining(","));
        List<String> lines = new ArrayList<>();
        lines.add(header);
        for (List<Statistic> statistics : projectStatistics)
        {
            lines.add(statistics.stream().map(statistic -> statistic.value).collect(Collectors.joining(",")));
        }

        // Write result to file
        Path file = Paths.get("result-1000.csv");
        try
        {
            Files.write(file, lines);
        }
        catch (IOException e)
        {
            Log.error(e);
            System.exit(1);
        }

        Log.writeToFile("log.txt");
    }

    private static Map<String, List<PmdRecord>> findPmdRecords()
    {
        Map<String, List<PmdRecord>> compilationUnitIdToPmdRecords = new HashMap<>();
        try
        {
            Files.lines(Paths.get(".", "pmd-report-1000.csv")).skip(1).forEach(line -> {
                PmdRecord pmdRecord = new PmdRecord(line);
                if (!compilationUnitIdToPmdRecords.containsKey(pmdRecord.projectName)) {
                    List<PmdRecord> initialPmdRecordList = new ArrayList<>();
                    initialPmdRecordList.add(pmdRecord);
                    compilationUnitIdToPmdRecords.put(pmdRecord.projectName, initialPmdRecordList);
                } else {
                    compilationUnitIdToPmdRecords.get(pmdRecord.projectName).add(pmdRecord);
                }
            });
        }
        catch (IOException e)
        {
            Log.error(e);
        }
        return compilationUnitIdToPmdRecords;
    }

    private static Map<String, Project> findProjects()
    {
        Map<String, Project> projects = new HashMap<>();
        File[] repositoryRootDirectories = new File("repositories/").listFiles(File::isDirectory);
        int numberOfRepositoriesChecked = 0;
        for (File repositoryRootDirectory : repositoryRootDirectories)
        {
            Project project = findProjectCharacteristics(repositoryRootDirectory.toPath());
            projects.put(project.projectId, project);
            Log.info("Checked " + ++numberOfRepositoriesChecked + " out of " + repositoryRootDirectories.length);
        }
        return projects;
    }

    private static Project findProjectCharacteristics(Path repositoryPath)
    {
        Log.info(repositoryPath.getFileName().toString());
        Project project = new Project(repositoryPath.getFileName().toString());

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
            return project;
        }

        // Sum LOC of all project files
        project.projectLOC = paths
                .stream()
                .map(path ->
                {
                    try
                    {
                        return Files.lines(path).count();
                    }
                    catch (Throwable e)
                    {
                        try
                        {
                            return Files.lines(path, Charset.forName("ISO-8859-1")).count();
                        }
                        catch (Throwable ex)
                        {
                            try
                            {
                                return Files.lines(path, Charset.forName("UTF-16")).count();
                            }
                            catch (Throwable exc)
                            {
                                exc.printStackTrace();
                            }
                        }
                        e.printStackTrace();
                    }
                    return 0L;
                })
                .reduce(0L, Long::sum);

        JavaParser parser = new JavaParser();

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

            CompilationUnit compilationUnit = compilationUnitOptional.get();

            // Classes and interfaces
            project.classOrInterfaceNames.addAll(
                    compilationUnit
                            .findAll(ClassOrInterfaceDeclaration.class)
                            .stream()
                            .map(NodeWithSimpleName::getNameAsString)
                            .collect(Collectors.toList()));

            // Methods
            project.methodNames.addAll(
                    compilationUnit
                        .findAll(MethodDeclaration.class)
                        .stream()
                        .map(NodeWithSimpleName::getNameAsString)
                        .collect(Collectors.toList()));

            // Fields
            project.fieldNames.addAll(
                    compilationUnit.findAll(FieldDeclaration.class)
                            .stream()
                            .map(fieldDeclaration -> fieldDeclaration.getVariables())
                            .flatMap(Collection::stream)
                            .map(NodeWithSimpleName::getNameAsString)
                            .collect(Collectors.toList()));

            // Constructor parameters
            project.parameterNames.addAll(
                    compilationUnit.findAll(ConstructorDeclaration.class)
                    .stream()
                    .map(constructorDeclaration -> constructorDeclaration.getParameters())
                    .flatMap(Collection::stream)
                    .map(NodeWithSimpleName::getNameAsString)
                    .collect(Collectors.toList()));

            // Method parameters
            project.parameterNames.addAll(
                    compilationUnit.findAll(MethodDeclaration.class)
                    .stream()
                    .map(methodDeclaration -> methodDeclaration.getParameters())
                    .flatMap(Collection::stream)
                    .map(NodeWithSimpleName::getNameAsString)
                    .collect(Collectors.toList())
            );
        }

        return project;
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
        return compilationUnitId;
    }
}
