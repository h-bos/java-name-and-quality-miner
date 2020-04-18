package io.hbp.com;

import com.github.javaparser.printer.lexicalpreservation.PhantomNodeLogic;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typeinference.TypeInferenceCache;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import net.sourceforge.pmd.*;
import net.sourceforge.pmd.util.datasource.DataSource;
import net.sourceforge.pmd.util.datasource.FileDataSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ViolationsParser
{
    public void parseAndAddViolationsTo(Repository repository)
    {
        JavaParserFacade.clearInstances();
        PhantomNodeLogic.cleanUpCache();

        PMDConfiguration pmdConfiguration = new PMDConfiguration();
        RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.createFactory(pmdConfiguration);
        InMemoryRenderer inMemoryRenderer = new InMemoryRenderer();

        pmdConfiguration.setRuleSets("pmd-non-naming-rules.xml");
        pmdConfiguration.setThreads(Runtime.getRuntime().availableProcessors() / 2);
        pmdConfiguration.setMinimumPriority(RulePriority.LOW);
        pmdConfiguration.setShowSuppressedViolations(false);
        pmdConfiguration.setAnalysisCacheLocation("cache/violations-" + repository.id + ".cache");

        // Find all successfully parsed source files.
        List<SourceFile> successfullyParsedSourceFiles = repository.sourceFiles
                .stream()
                .filter(sourceFile -> sourceFile.parsedSuccessfully)
                .collect(Collectors.toList());

        List<List<SourceFile>> sourceFileBatches = new ArrayList<>();

        int batchSize = 1000;
        int numberOfBatches = (int) Math.ceil(successfullyParsedSourceFiles.size() / (double) batchSize);
        for (int i = 0; i < numberOfBatches; i++)
        {
            sourceFileBatches.add(new ArrayList<>
            (
                successfullyParsedSourceFiles.subList(i * batchSize, Math.min((i + 1) * batchSize, successfullyParsedSourceFiles.size()))
            ));
        }

        for (List<SourceFile> sourceFileBatch : sourceFileBatches)
        {
            List<DataSource> sources = sourceFileBatch
                .stream()
                .map(sourceFile -> new FileDataSource(sourceFile.filePath.toFile()))
                .collect(Collectors.toList());

            try
            {
                inMemoryRenderer.start();
            }
            catch (IOException e)
            {
                Log.error(e);
            }

            RuleContext ruleContext = new RuleContext();

            try
            {
                PMD.processFiles(pmdConfiguration, ruleSetFactory, sources, ruleContext, List.of(inMemoryRenderer));
            }
            catch (Error e)
            {
                // PMD has throws Errors when they should be throwing some type of parsing error that is put into the report.
                // That is why we catch Errors here and invalidate the batch of sources that were included in the run where
                // the Error was thrown and invalidate them.
                for (SourceFile sourceFile : sourceFileBatch)
                {
                    sourceFile.parsedSuccessfully = false;
                }
            }

            inMemoryRenderer.end();

            File repos = new File("");
            String absolutePathRootPath = repos.getAbsolutePath() + "\\";

            inMemoryRenderer.ruleViolations.forEachRemaining(ruleViolation -> {
                String relativeFileName = ruleViolation.getFilename().replace(absolutePathRootPath, "");
                SourceFile sourceFile = sourceFileBatch
                        .stream()
                        .filter(file -> file.fileName.equals(relativeFileName))
                        .collect(Collectors.toList())
                        .get(0);
                sourceFile.violations.add(new Violation(ruleViolation));
            });

            inMemoryRenderer.processingErrors.forEachRemaining(processingError -> {
                String relativeFileName = processingError.getFile().replace(absolutePathRootPath, "");
                SourceFile sourceFile = sourceFileBatch
                        .stream()
                        .filter(file -> file.fileName.equals(relativeFileName))
                        .collect(Collectors.toList())
                        .get(0);
                sourceFile.parsedSuccessfully = false;
            });
        }
    }
}
