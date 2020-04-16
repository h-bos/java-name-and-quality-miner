package io.hbp.com;

import java.io.File;
import java.util.List;

public class Main
{
    static final String REPOSITORIES_OUTPUT = "repositories.csv";
    static final String SOURCE_FILES_OUTPUT = "source-files.csv";
    static final String IDENTIFIERS_OUTPUT  = "identifiers.csv";
    static final String LOG_FILE            = "log.txt";
    static final String REPOSITORIES_FOLDER = "repositories/";

    public static void main(String[] args)
    {
        CsvWriter csvWriter = new CsvWriter();
        csvWriter.clearFile(REPOSITORIES_OUTPUT);
        csvWriter.clearFile(SOURCE_FILES_OUTPUT);
        csvWriter.clearFile(IDENTIFIERS_OUTPUT);
        csvWriter.clearFile(LOG_FILE);

        File[] repositoryRootDirectories = new File(REPOSITORIES_FOLDER).listFiles(File::isDirectory);
        assert repositoryRootDirectories != null;

        IdentifierParser identifierParser = new IdentifierParser();
        ViolationsParser violationsParser = new ViolationsParser();

        int numberOfRepositoriesChecked = 0;

        { // Add headers to CSVs based on first project's records headers.
            Log.info("Parsing repository " + ++numberOfRepositoriesChecked + " out of " + repositoryRootDirectories.length);
            Repository repository = identifierParser.parseRepository(repositoryRootDirectories[0].toPath());
            violationsParser.parseAndAddViolationsTo(repository);

            csvWriter.appendHeaders(REPOSITORIES_OUTPUT, repository.repositoryRecord());
            csvWriter.appendRecords(REPOSITORIES_OUTPUT, List.of(repository.repositoryRecord()));

            csvWriter.appendHeaders(SOURCE_FILES_OUTPUT, repository.sourceFileRecords().get(0));
            csvWriter.appendRecords(SOURCE_FILES_OUTPUT, repository.sourceFileRecords());

            csvWriter.appendHeaders(IDENTIFIERS_OUTPUT, repository.identifierRecords().get(0));
            csvWriter.appendRecords(IDENTIFIERS_OUTPUT, repository.identifierRecords());
        }

        for (int fileIndex = 1; fileIndex < repositoryRootDirectories.length; fileIndex++)
        {
            Log.info("Parsing repository " + ++numberOfRepositoriesChecked + " out of " + repositoryRootDirectories.length);
            Repository repository = identifierParser.parseRepository(repositoryRootDirectories[fileIndex].toPath());
            violationsParser.parseAndAddViolationsTo(repository);
            csvWriter.appendRecords(REPOSITORIES_OUTPUT, List.of(repository.repositoryRecord()));
            csvWriter.appendRecords(SOURCE_FILES_OUTPUT, repository.sourceFileRecords());
            csvWriter.appendRecords(IDENTIFIERS_OUTPUT, repository.identifierRecords());
        }

        Log.writeToFile();
    }
}
