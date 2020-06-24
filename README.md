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

(Need randomly sampled repositories? Check out: https://github.com/hb-p/ghtorrent-sampler)

#### Generate CSV files with identifier/quality measurments 
```
java -jar java-name-and-quality-miner.jar 
```
