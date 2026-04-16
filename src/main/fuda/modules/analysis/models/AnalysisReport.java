package fuda.modules.analysis.models;

import java.util.List;

public record AnalysisReport(
        String assignmentId,
        String projectRoot,
        String rubricPath,
        int totalWeight,
        int earnedWeight,
        List<CriteriaReport> passedCriteria,
        List<CriteriaReport> failedCriteria,
        double scorePercentage
) {
    /**
     * Return detailed report with all pass/fail messages.
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Analysis Report ===\n");
        sb.append(String.format("Assignment: %s\n", assignmentId));
        sb.append(String.format("Project Root: %s\n", projectRoot));
        sb.append(String.format("Score: %d / %d (%.2f%%)\n\n", earnedWeight, totalWeight, scorePercentage));

        if (!passedCriteria.isEmpty()) {
            sb.append("✓ PASSED CRITERIA:\n");
            for (CriteriaReport criteria : passedCriteria) {
                sb.append(String.format("  [%s] %s (+%d points)\n", criteria.id(), criteria.passMessage(), criteria.weight()));
            }
            sb.append("\n");
        }

        if (!failedCriteria.isEmpty()) {
            sb.append("✗ FAILED CRITERIA:\n");
            for (CriteriaReport criteria : failedCriteria) {
                sb.append(String.format("  [%s] %s (-%d points)\n", criteria.id(), criteria.failMessage(), criteria.weight()));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}