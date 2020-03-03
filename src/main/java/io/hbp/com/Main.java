package io.hbp.com;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main
{
    public static void main(String[] args)
    {
        Map<String, List<PmdRecord>> compilationUnitIdToPmdRecords = null;
        try
        {
            compilationUnitIdToPmdRecords = findPmdRecords();
        } catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        Map<String, CompilationUnitRecord> compilationUnitIdToCompilationUnits = findCompilationUnits();

        for (Map.Entry<String, List<PmdRecord>> record : compilationUnitIdToPmdRecords.entrySet())
        {
            int numberOfViolations = record.getValue().size();
            if (compilationUnitIdToCompilationUnits.containsKey(record.getKey())) {
                compilationUnitIdToCompilationUnits.get(record.getKey()).numberOfViolations += numberOfViolations;
            }
        }

        List<CompilationUnitRecord> finalRecords = new ArrayList<>();

        for (Map.Entry<String, CompilationUnitRecord> record : compilationUnitIdToCompilationUnits.entrySet())
        {
            finalRecords.add(record.getValue());
        }

        List<String> lines = new ArrayList<>();
        lines.add(CompilationUnitRecord.CSV_HEADER);
        lines.addAll(finalRecords.stream().map(record -> record.asCsvRow()).collect(Collectors.toList()));

        Path file = Paths.get("result.csv");
        try
        {
            Files.write(file, lines);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static Map<String, List<PmdRecord>> findPmdRecords() throws IOException
    {
        // Put pmd records in a map { compilationUnitId -> [PmdRecord] }
        Map<String, List<PmdRecord>> compilationUnitIdToPmdRecords = new HashMap<>();
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
                continue;
            }
            if (!parseResult.isSuccessful()) {
                continue;
            }

            Optional<CompilationUnit> compilationUnitOptional = parseResult.getResult();

            if (!compilationUnitOptional.isPresent()) {
                continue;
            }

            CompilationUnit compilationUnit = compilationUnitOptional.get();

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

            CompilationUnitRecord compilationUnitRecord = new CompilationUnitRecord(compilationUnitId);
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
}
