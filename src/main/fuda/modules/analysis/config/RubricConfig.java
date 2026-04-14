package fuda.modules.analysis.config;

import java.util.List;

public record RubricConfig(
    String assignmentId,
    List<String> sourceRoots,
    List<String> ignoreDirs,
    List<CriteriaConfig> criteria
) {}
