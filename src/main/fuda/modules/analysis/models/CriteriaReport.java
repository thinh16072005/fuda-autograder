package fuda.modules.analysis.models;

import java.util.List;

public record CriteriaReport(
    String id,
    RuleType type,
    int weight,
    String passMessage,
    String failMessage,
    List<String> paths,
    String pattern,
    Integer minMatches
) {}
