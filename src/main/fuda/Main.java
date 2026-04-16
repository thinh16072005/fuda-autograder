package fuda;

import fuda.controller.AnalysisController;
import fuda.modules.analysis.models.AnalysisReport;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        String path = "D:/FUDAautograder/FUDA.Autograder/sample-input/SE001";

        Path projectRoot = Path.of(path);
        Path rubricJsonPath = Path.of("src/main/fuda/rubric.json");
        AnalysisController controller = new AnalysisController();
        AnalysisReport result = controller.analyze(projectRoot, rubricJsonPath);

        // Print detailed report with pass/fail messages
        System.out.println(result.toDetailedString());
    }
}

