package fuda.modules.analysis.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fuda.modules.analysis.models.RubricReport;

import java.io.IOException;
import java.nio.file.Path;

public class JsonRubricLoader {
    private final ObjectMapper mapper;

    public JsonRubricLoader() {
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public RubricReport load(Path jsonPath) throws IOException {
        return mapper.readValue(jsonPath.toFile(), RubricReport.class);
    }
}
