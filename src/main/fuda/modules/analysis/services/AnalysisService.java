package fuda.modules.analysis.services;

import fuda.modules.analysis.models.CriteriaReport;
import fuda.modules.analysis.models.RubricReport;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface AnalysisService {

    List<CriteriaReport> findPassedCriteria(Path projectRoot, RubricReport rubric) throws IOException;

    List<CriteriaReport> findFailedCriteria(Path projectRoot, RubricReport rubric) throws IOException;

    int calculateEarnedWeight(Path projectRoot, RubricReport rubric) throws IOException;

    boolean checkCriteria(Path projectRoot, RubricReport rubric, CriteriaReport criteria) throws IOException;
}
