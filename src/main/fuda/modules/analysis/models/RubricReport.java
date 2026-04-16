package fuda.modules.analysis.models;

import java.util.List;

public record RubricReport(
    String assignmentId,
    List<String> sourceRoots,
    List<String> ignoreDirs,
    List<CriteriaReport> criteria
) {}
