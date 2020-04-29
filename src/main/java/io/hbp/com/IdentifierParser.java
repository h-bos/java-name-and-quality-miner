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
                    sourceFile.addIdentifiers(List.of(new Identifier(classOrInterfaceDeclaration.getNameAsString())), IdentifierGroup.EntityType.INTERFACE);
                }
                else
                {
                    sourceFile.addIdentifiers(List.of(new Identifier(classOrInterfaceDeclaration.getNameAsString())), IdentifierGroup.EntityType.CLASS);
                }
            }

            // Find enum declarations
            sourceFile.addIdentifiers(
            (
                compilationUnit
                    .findAll(EnumDeclaration.class)
                    .stream()
                    .map(enumDeclaration -> new Identifier(enumDeclaration.getNameAsString()))
                    .collect(Collectors.toList())
            ), IdentifierGroup.EntityType.ENUM);

            // Find enum constant declarations
            sourceFile.addIdentifiers(
            (
                compilationUnit
                    .findAll(EnumConstantDeclaration.class)
                    .stream()
                    .map(enumDeclaration -> new Identifier(enumDeclaration.getNameAsString()))
                    .collect(Collectors.toList())
            ), IdentifierGroup.EntityType.ENUM_CONSTANT);

            // Find method declarations
            sourceFile.addIdentifiers(
            (
                compilationUnit
                    .findAll(MethodDeclaration.class)
                    .stream()
                    .map(methodDeclaration -> new Identifier(methodDeclaration.getNameAsString()))
                    .collect(Collectors.toList())
            ), IdentifierGroup.EntityType.METHOD);

            // Find field declarations
            sourceFile.addIdentifiers(
            (
                compilationUnit
                    .findAll(FieldDeclaration.class)
                    .stream()
                    .map(FieldDeclaration::getVariables)
                    .flatMap(Collection::stream)
                    .map(variableDeclaration -> new Identifier(variableDeclaration.getNameAsString()))
                    .collect(Collectors.toList())
            ), IdentifierGroup.EntityType.FIELD);

            // Find constructor parameters
            sourceFile.addIdentifiers(
            (
                compilationUnit.findAll(ConstructorDeclaration.class)
                    .stream()
                    .map(CallableDeclaration::getParameters)
                    .flatMap(Collection::stream)
                    .map(x -> new Identifier(x.getNameAsString()))
                    .collect(Collectors.toList())
            ), IdentifierGroup.EntityType.PARAMETER);

            // Method parameters
            sourceFile.addIdentifiers(
            (
                compilationUnit.findAll(MethodDeclaration.class)
                    .stream()
                    .map(CallableDeclaration::getParameters)
                    .flatMap(Collection::stream)
                    .map(x -> new Identifier(x.getNameAsString()))
                    .collect(Collectors.toList())
            ), IdentifierGroup.EntityType.PARAMETER);

            // Local variables found in method bodies
            sourceFile.addIdentifiers(
            (
                compilationUnit.findAll(MethodDeclaration.class)
                    .stream()
                    .map(MethodDeclaration::getBody)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(x -> x.findAll(VariableDeclarator.class))
                    .flatMap(Collection::stream)
                    .map(variableDeclaration -> new Identifier(variableDeclaration.getNameAsString()))
                    .collect(Collectors.toList())
            ), IdentifierGroup.EntityType.LOCAL_VARIABLE);

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
