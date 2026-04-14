package fuda.modules.analysis.config;

import java.util.List;

public record CriteriaConfig(
    String id,
    RuleType type,
    int weight,
    String passMessage,
    String failMessage,
    List<String> paths,
    String pattern,
    Integer minMatches
) {}
