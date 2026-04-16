package fuda.controller;

import fuda.modules.analysis.config.JsonRubricLoader;
import fuda.modules.analysis.models.AnalysisReport;
import fuda.modules.analysis.models.CriteriaReport;
import fuda.modules.analysis.models.RubricReport;
import fuda.modules.analysis.services.AnalysisService;
import fuda.modules.analysis.services.AnalysisServiceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AnalysisController {

    private final AnalysisService analysisService;
    private final JsonRubricLoader rubricLoader;

    public AnalysisController() {
        this.analysisService = new AnalysisServiceImpl();
        this.rubricLoader = new JsonRubricLoader();
    }

    public AnalysisReport analyze(Path projectRoot, Path rubricJsonPath) throws IOException {
        validateInputs(projectRoot, rubricJsonPath);

        RubricReport rubric = rubricLoader.load(rubricJsonPath);
        if (rubric == null || rubric.criteria() == null || rubric.criteria().isEmpty()) {
            throw new IOException("Rubric is empty or invalid");
        }

        List<CriteriaReport> passedCriteria = analysisService.findPassedCriteria(projectRoot, rubric);
        List<CriteriaReport> failedCriteria = analysisService.findFailedCriteria(projectRoot, rubric);
        int earnedWeight = analysisService.calculateEarnedWeight(projectRoot, rubric);

        int totalWeight = rubric.criteria().stream()
                .mapToInt(CriteriaReport::weight)
                .sum();

        return new AnalysisReport(
                rubric.assignmentId(),
                projectRoot.toString(),
                rubricJsonPath.toString(),
                totalWeight,
                earnedWeight,
                passedCriteria,
                failedCriteria,
                calculatePercentage(earnedWeight, totalWeight)
        );
    }

    private void validateInputs(Path projectRoot, Path rubricJsonPath) throws IOException {
        if (projectRoot == null || !Files.exists(projectRoot) || !Files.isDirectory(projectRoot)) {
            throw new IOException("Invalid project root: " + projectRoot);
        }
        if (rubricJsonPath == null || !Files.exists(rubricJsonPath)) {
            throw new IOException("Rubric JSON file not found: " + rubricJsonPath);
        }
    }

    private double calculatePercentage(int earned, int total) {
        if (total == 0) {
            return 0.0;
        }
        return (earned * 100.0) / total;
    }
}
