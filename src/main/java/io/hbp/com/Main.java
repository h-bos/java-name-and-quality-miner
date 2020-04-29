package io.hbp.com;

import java.io.File;
import java.util.List;

public class Main
{
    static final String REPOSITORIES_OUTPUT = "repositories-2.csv";
    static final String SOURCE_FILES_OUTPUT = "source-files-2.csv";
    static final String IDENTIFIERS_OUTPUT  = "identifier-groups-2.csv";
    static final String LOG_FILE            = "log.txt-2";
    static final String REPOSITORIES_FOLDER = "repositories/";

    public static void main(String[] args)
    {
        CsvWriter.clearFile(REPOSITORIES_OUTPUT);
        CsvWriter.clearFile(SOURCE_FILES_OUTPUT);
        CsvWriter.clearFile(IDENTIFIERS_OUTPUT);
        CsvWriter.clearFile(LOG_FILE);

        File[] repositoryRootDirectories = new File(REPOSITORIES_FOLDER).listFiles(File::isDirectory);
        assert repositoryRootDirectories != null;

        IdentifierParser identifierParser = new IdentifierParser();
        ViolationsParser violationsParser = new ViolationsParser();

        int numberOfRepositoriesChecked = 0;

        { // Add headers to CSVs based on first project's records headers.
            Log.info("Parsing repository " + ++numberOfRepositoriesChecked + " out of " + repositoryRootDirectories.length);
            Repository repository = identifierParser.parseRepository(repositoryRootDirectories[0].toPath());
            violationsParser.parseAndAddViolationsTo(repository);

            CsvWriter.appendHeaders(REPOSITORIES_OUTPUT, CsvWriter.repositoryHeaders);
            CsvWriter.appendRecords(REPOSITORIES_OUTPUT, List.of(repository.repositoryRecord()));

            CsvWriter.appendHeaders(SOURCE_FILES_OUTPUT, CsvWriter.sourceFileHeaders);
            CsvWriter.appendRecords(SOURCE_FILES_OUTPUT, repository.sourceFileRecords());

            CsvWriter.appendHeaders(IDENTIFIERS_OUTPUT, CsvWriter.identifierHeaders);
            CsvWriter.appendRecords(IDENTIFIERS_OUTPUT, repository.identifierRecords());
        }

        for (int fileIndex = 1; fileIndex < repositoryRootDirectories.length; fileIndex++)
        {
            Log.info("Parsing repository " + ++numberOfRepositoriesChecked + " out of " + repositoryRootDirectories.length);
            Repository repository = identifierParser.parseRepository(repositoryRootDirectories[fileIndex].toPath());
            violationsParser.parseAndAddViolationsTo(repository);
            CsvWriter.appendRecords(REPOSITORIES_OUTPUT, List.of(repository.repositoryRecord()));
            CsvWriter.appendRecords(SOURCE_FILES_OUTPUT, repository.sourceFileRecords());
            CsvWriter.appendRecords(IDENTIFIERS_OUTPUT, repository.identifierRecords());
        }

        Log.writeToFile();
    }
}
