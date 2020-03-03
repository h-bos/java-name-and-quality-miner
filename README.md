# java-name-and-quality-miner

## Requirements

* Java 13+
* PMD

## Get Started

### Build
```
mvn package
```

### Run

#### Generate PMD Report of All Repositories

**Windows**
```
pmd.bat -d repo\ -language java -t 8 -f csv -R pmd-non-naming-rules.xml > result.csv
```
**Linux**
```
./run.sh pmd -d repo/ -language java -t 8 -f csv -R pmd-non-naming-rules.xml > result.csv 
```

#### Generate Report of Identifier Characteristics and Internal Code Quality
```
java -jar ghtorrent-sampler.jar report.csv
```

#### Resulting Report Format
```
|avg_identifier_length|...|nbr_of_violations|
```

