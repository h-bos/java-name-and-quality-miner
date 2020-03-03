# java-name-and-quality-miner

**java-name-and-quality-miner** is a tool that maps identifier characteristics (casing, length, etc.), maps them to 
violations/metrics reports, and outputs the map as a CSV file for further analysis.

## Requirements

* Java 13+
* PMD
* Windows (Linux PRs are welcome) 

## Get Started

### Build
```
mvn package
```

### Run

#### Gather Repositories

Place all repositories that you want to analyze in the `repositories` folder.

(Need repository samples? Check out: https://github.com/hb-p/ghtorrent-sampler)

#### Generate PMD Report of All Repositories

**Windows**
```
pmd.bat -d repositories\ -l java -t {nbrOfThreads} -f csv -R pmd-non-naming-rules.xml -shortnames > pmd-report.csv
```

#### Generate Report of Identifier Characteristics and Internal Code Quality Attributes
```
java -jar ghtorrent-sampler.jar 
```
