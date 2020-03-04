package io.hbp.com;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;

import java.io.File;
import java.io.IOException;
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
        Map<String, CompilationUnitRecord> compilationUnits = findCompilationUnits();

        for (Map.Entry<String, List<PmdRecord>> pmdRecord : pmdRecords.entrySet())
        {
            int numberOfViolations = pmdRecord.getValue().size();
            if (compilationUnits.containsKey(pmdRecord.getKey())) {
                compilationUnits.get(pmdRecord.getKey()).numberOfViolations += numberOfViolations;
            }
        }

        List<CompilationUnitRecord> resultingRecords = new ArrayList<>();
        for (Map.Entry<String, CompilationUnitRecord> record : compilationUnits.entrySet())
        {
            resultingRecords.add(record.getValue());
        }

        List<String> lines = new ArrayList<>();
        lines.add(CompilationUnitRecord.CSV_HEADER);
        lines.addAll(resultingRecords.stream().map(record -> record.asCsvRow()).collect(Collectors.toList()));

        Path file = Paths.get("result.csv");
        try
        {
            Files.write(file, lines);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Map<String, List<PmdRecord>> findPmdRecords()
    {
        Map<String, List<PmdRecord>> compilationUnitIdToPmdRecords = new HashMap<>();
        try
        {
            Files.lines(Paths.get(".", "pmd-report.csv")).skip(1).forEach(line -> {
                PmdRecord pmdRecord = new PmdRecord(line);
                if (!compilationUnitIdToPmdRecords.containsKey(pmdRecord.compilationUnitId)) {
                    List<PmdRecord> initialPmdRecordList = new ArrayList<>();
                    initialPmdRecordList.add(pmdRecord);
                    compilationUnitIdToPmdRecords.put(pmdRecord.compilationUnitId, initialPmdRecordList);
                } else {
                    compilationUnitIdToPmdRecords.get(pmdRecord.compilationUnitId).add(pmdRecord);
                }
            });
        }
        catch (IOException e)
        {
            System.out.println("[ERROR] No PMD records found.");
            e.printStackTrace();
        }
        return compilationUnitIdToPmdRecords;
    }

    private static Map<String, CompilationUnitRecord> findCompilationUnits()
    {
        Map<String, CompilationUnitRecord> compilationUnits = new HashMap<>();
        File[] repositoryRootDirectories = new File("repositories/").listFiles(File::isDirectory);
        for (File repositoryRootDirectory : repositoryRootDirectories)
        {
            List<CompilationUnitRecord> records = null;
            records = findCompilationUnitsOfRepository(repositoryRootDirectory.toPath());
            records.forEach(record -> {
                compilationUnits.put(record.compilationUnitId, record);
            });
        }
        return compilationUnits;
    }

    private static List<CompilationUnitRecord> findCompilationUnitsOfRepository(Path repositoryPath)
    {
        List<CompilationUnitRecord> compilationUnitRecords = new ArrayList<>();

        List<Path> paths;
        try
        {
            paths = Files.walk(repositoryPath)
                    .filter(path -> path.toAbsolutePath().toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return compilationUnitRecords;
        }

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
                System.out.println("[WARNING] File could not be parsed: " + path.toString());
                continue;
            }
            if (!parseResult.isSuccessful()) {
                System.out.println("[WARNING] Parse result was not successful: " + path.toString());
                continue;
            }

            Optional<CompilationUnit> compilationUnitOptional = parseResult.getResult();

            if (!compilationUnitOptional.isPresent()) {
                System.out.println("[WARNING] Compilation unit is not present: " + path.toString());
                continue;
            }

            CompilationUnit compilationUnit = compilationUnitOptional.get();

            CompilationUnitRecord compilationUnitRecord = new CompilationUnitRecord(findCompilationUnitId(compilationUnit, path));
            compilationUnitRecord.classOrInterfaceNames.addAll(
                    compilationUnit
                            .findAll(ClassOrInterfaceDeclaration.class)
                            .stream()
                            .map(NodeWithSimpleName::getNameAsString)
                            .collect(Collectors.toList()));

            compilationUnitRecord.methodNames.addAll(
                    compilationUnit
                        .findAll(MethodDeclaration.class)
                        .stream()
                        .map(NodeWithSimpleName::getNameAsString)
                        .collect(Collectors.toList()));

            compilationUnitRecords.add(compilationUnitRecord);
        }

        return compilationUnitRecords;
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
