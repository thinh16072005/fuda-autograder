# fuda-autograder Analysis Layer

## Overview

Autograder system for evaluating React assignments against configurable rubric criteria in JSON format. The analysis layer reads source code directly from project directories and evaluates against three rule types: `FILE_EXISTS`, `REACT_SOURCE_EXISTS`, and `REGEX_IN_SOURCE`.

## Components

### Models
- **`RubricReport`**: Container for rubric metadata and criteria list
- **`CriteriaReport`**: Single criterion with rule type, weight, paths, pattern, minMatches
- **`RuleType`**: Enum defining evaluation rules (FILE_EXISTS, REACT_SOURCE_EXISTS, REGEX_IN_SOURCE)

### Services
- **`AnalysisService`**: Interface for rubric evaluation
- **`AnalysisServiceImpl`**: Implements criterion checking and pass/fail/weight aggregation

### Config
- **`JsonRubricLoader`**: Loads rubric from JSON file using Jackson

### Controller
- **`AnalysisController`**: Orchestrates rubric loading and service analysis with input validation

## Usage Example

```java
Path projectRoot = Paths.get("/path/to/react-project");
Path rubricJson = Paths.get("/path/to/rubric.json");

AnalysisController controller = new AnalysisController();
AnalysisController.AnalysisResult result = controller.analyze(projectRoot, rubricJson);

System.out.println("Score: " + result.scorePercentage() + "%");
System.out.println("Earned: " + result.earnedWeight() + "/" + result.totalWeight());
System.out.println("Passed: " + result.passedCriteria().size());
System.out.println("Failed: " + result.failedCriteria().size());
```

## Rubric JSON Format

```json
{
  "assignmentId": "react-101",
  "sourceRoots": ["src"],
  "ignoreDirs": ["node_modules", ".git"],
  "criteria": [
    {
      "id": "has-package-json",
      "type": "FILE_EXISTS",
      "weight": 10,
      "passMessage": "package.json found",
      "failMessage": "package.json missing",
      "paths": ["package.json"],
      "pattern": null,
      "minMatches": null
    },
    {
      "id": "uses-react",
      "type": "REACT_SOURCE_EXISTS",
      "weight": 20,
      "passMessage": "React imports detected",
      "failMessage": "No React imports found",
      "paths": ["src"],
      "pattern": null,
      "minMatches": null
    },
    {
      "id": "uses-hooks",
      "type": "REGEX_IN_SOURCE",
      "weight": 15,
      "passMessage": "React Hooks found",
      "failMessage": "No React Hooks detected",
      "paths": ["src"],
      "pattern": "use(State|Effect|Memo)\\s*\\(",
      "minMatches": 1
    }
  ]
}
```

## Rule Type Details

### FILE_EXISTS
- Checks if all paths in `criteria.paths` exist
- Paths are resolved relative to `projectRoot`
- Fails if any path does not exist

### REACT_SOURCE_EXISTS
- Scans source files under `criteria.paths` (defaults to `projectRoot` if empty)
- Ignores: `node_modules`, `.git`, `dist`, `build`, etc.
- Passes if any file:
  - Ends with `.jsx`, `.js` OR
  - Contains React import/require or React Hook usage (useState, useEffect, etc.)

### REGEX_IN_SOURCE
- Compiles `criteria.pattern` regex (MultiLine mode)
- Scans all source files under `criteria.paths`
- Counts total matches across all files
- Passes if total matches >= `criteria.minMatches` (defaults to 1)
- Returns false on regex syntax error

## Test Running

```bash
mvn test
```

## Module Structure

```
src/main/fuda/
├── controller/
│   └── AnalysisController.java
└── modules/analysis/
    ├── config/
    │   └── JsonRubricLoader.java
    ├── models/
    │   ├── CriteriaReport.java
    │   ├── RubricReport.java
    │   └── RuleType.java
    └── services/
        ├── AnalysisService.java
        └── AnalysisServiceImpl.java

src/test/java/fuda/
├── controller/
│   └── AnalysisControllerTest.java
├── modules/analysis/services/
│   └── AnalysisServiceImplTest.java
└── AppTest.java
```
