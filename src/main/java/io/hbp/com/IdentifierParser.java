package io.hbp.com;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IdentifierParser
{
    public Repository parseRepository(Path rootFolder)
    {
        JavaParser parser = new JavaParser();

        List<Path> paths;
        try
        {
            paths = Files.walk(rootFolder)
                    .filter(path -> path.toAbsolutePath().toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            Log.error("Could not walk files in repository " + rootFolder.toString());
            return null;
        }

        Repository repository = new Repository(rootFolder.toString());

        for (Path path : paths)
        {
            SourceFile sourceFile = new SourceFile(path);

            Optional<Long> linesOfCodeOptional = tryToFindLinesOfCodeOf(path.toString());
            if (linesOfCodeOptional.isEmpty())
            {
               repository.sourceFiles.add(sourceFile);
               continue;
            }

            sourceFile.linesOfCode = linesOfCodeOptional.get();

            ParseResult<CompilationUnit> parseResult;

            try
            {
                parseResult = parser.parse(path);
            }
            catch (Throwable throwable)
            {
                Log.info("File could not be parsed " + path.toString());
                repository.sourceFiles.add(sourceFile);
                continue;
            }

            if (!parseResult.isSuccessful())
            {
                Log.info("File could not be parsed " + path.toString());
                repository.sourceFiles.add(sourceFile);
                continue;
            }

            Optional<CompilationUnit> compilationUnitOptional = parseResult.getResult();
            if (compilationUnitOptional.isEmpty())
            {
                Log.info("Compilation unit is not present " + path.toString());
                repository.sourceFiles.add(sourceFile);
                continue;
            }

            CompilationUnit compilationUnit = compilationUnitOptional.get();

            // Find classes or interface identifiers
            List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
            for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations)
            {
                if (classOrInterfaceDeclaration.isInterface())
                {
                    sourceFile.identifiers.add(new Identifier(classOrInterfaceDeclaration.getNameAsString(), Identifier.Type.INTERFACE));
                }
                else
                {
                    sourceFile.identifiers.add(new Identifier(classOrInterfaceDeclaration.getNameAsString(), Identifier.Type.CLASS));
                }
            }

            // Find enum declarations
            sourceFile.identifiers.addAll
            (
                compilationUnit
                    .findAll(EnumDeclaration.class)
                    .stream()
                    .map(enumDeclaration -> new Identifier(enumDeclaration.getNameAsString(), Identifier.Type.ENUM))
                    .collect(Collectors.toList())
            );

            // Find enum constant declarations
            sourceFile.identifiers.addAll
            (
                compilationUnit
                    .findAll(EnumConstantDeclaration.class)
                    .stream()
                    .map(enumDeclaration -> new Identifier(enumDeclaration.getNameAsString(), Identifier.Type.ENUM_CONSTANT))
                    .collect(Collectors.toList())
            );

            // Find method declarations
            sourceFile.identifiers.addAll
            (
                compilationUnit
                    .findAll(MethodDeclaration.class)
                    .stream()
                    .map(methodDeclaration -> new Identifier(methodDeclaration.getNameAsString(), Identifier.Type.METHOD))
                    .collect(Collectors.toList())
            );

            // Find field declarations
            sourceFile.identifiers.addAll
            (
                compilationUnit
                    .findAll(FieldDeclaration.class)
                    .stream()
                    .map(FieldDeclaration::getVariables)
                    .flatMap(Collection::stream)
                    .map(variableDeclaration -> new Identifier(variableDeclaration.getNameAsString(), Identifier.Type.FIELD))
                    .collect(Collectors.toList())
            );

            // Find constructor parameters
            sourceFile.identifiers.addAll
            (
                compilationUnit.findAll(ConstructorDeclaration.class)
                    .stream()
                    .map(CallableDeclaration::getParameters)
                    .flatMap(Collection::stream)
                    .map(x -> new Identifier(x.getNameAsString(), Identifier.Type.PARAMETER))
                    .collect(Collectors.toList())
            );

            // Method parameters
            sourceFile.identifiers.addAll
            (
                compilationUnit.findAll(MethodDeclaration.class)
                    .stream()
                    .map(CallableDeclaration::getParameters)
                    .flatMap(Collection::stream)
                    .map(x -> new Identifier(x.getNameAsString(), Identifier.Type.PARAMETER))
                    .collect(Collectors.toList())
            );

            // Local variables found in method bodies
            sourceFile.identifiers.addAll
            (
                compilationUnit.findAll(MethodDeclaration.class)
                    .stream()
                    .map(MethodDeclaration::getBody)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(x -> x.findAll(VariableDeclarator.class))
                    .flatMap(Collection::stream)
                    .map(variableDeclaration -> new Identifier(variableDeclaration.getNameAsString(), Identifier.Type.LOCAL_VARIABLE))
                    .collect(Collectors.toList())
            );

            sourceFile.parsedSuccessfully = true;
            repository.sourceFiles.add(sourceFile);
        }

        return repository;
    }

    private Optional<Long> tryToFindLinesOfCodeOf(String filePath)
    {
        Long linesOfCode = 0L;
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            while (reader.readLine() != null) ++linesOfCode;
        }
        catch (Throwable th)
        {
           Log.info("Could not read LOC of file " + filePath);
           return Optional.empty();
        }

        return Optional.of(linesOfCode);
    }
}
