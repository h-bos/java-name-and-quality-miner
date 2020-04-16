package io.hbp.com;

import java.io.File;

public class Main
{
    static final String SOURCE_FILES_OUTPUT = "source-files.csv";
    static final String IDENTIFIERS_OUTPUT  = "identifiers.csv";
    static final String LOG_FILE            = "log.txt";
    static final String REPOSITORIES_FOLDER = "repositories/";

    public static void main(String[] args)
    {
        CsvWriter csvWriter = new CsvWriter();
        csvWriter.clearRecords(SOURCE_FILES_OUTPUT);
        csvWriter.clearRecords(IDENTIFIERS_OUTPUT);
        csvWriter.clearRecords(LOG_FILE);

        File[] repositoryRootDirectories = new File(REPOSITORIES_FOLDER).listFiles(File::isDirectory);
        assert repositoryRootDirectories != null;

        IdentifierParser identifierParser = new IdentifierParser();
        ViolationsParser violationsParser = new ViolationsParser();

        int numberOfRepositoriesChecked = 0;

        {
            Log.info("Parsing repository " + ++numberOfRepositoriesChecked + " out of " + repositoryRootDirectories.length);
            Repository repository = identifierParser.parseRepository(repositoryRootDirectories[0].toPath());
            violationsParser.parseAndAddViolationsTo(repository);
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
            csvWriter.appendRecords(SOURCE_FILES_OUTPUT, repository.sourceFileRecords());
            csvWriter.appendRecords(IDENTIFIERS_OUTPUT, repository.identifierRecords());
        }

        Log.writeToFile();
    }
}
