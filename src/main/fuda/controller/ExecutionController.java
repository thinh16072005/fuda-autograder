package fuda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fuda.modules.execution.ExecutionResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ExecutionController {

    private static final String SCRIPT_PATH = "executor/runner/playwrightRunner.js";

    public static ExecutionResult execute(String url) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "node",
                    SCRIPT_PATH,
                    url
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();

            // đọc output
            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                output = reader.lines().collect(Collectors.joining("\n"));
            }

            process.waitFor();

            // debug nếu cần
            System.out.println("\n📦 RAW RESULT:");
            System.out.println(output);

            // parse JSON
            ObjectMapper mapper = new ObjectMapper();
            ExecutionResult result = mapper.readValue(output, ExecutionResult.class);

            printPretty(result);

            return result;

        } catch (Exception e) {
            ExecutionResult fail = new ExecutionResult();
            fail.setSuccess(false);
            fail.setScore(0);
            fail.setError(e.getMessage());
            return fail;
        }
    }

    // --- Pretty log (rất hữu ích khi chấm) ---
    private static void printPretty(ExecutionResult r) {
        System.out.println("\n===== RESULT =====");
        System.out.println("Score: " + r.getScore());
        System.out.println("Success: " + r.isSuccess());

        if (r.getTests() != null) {
            System.out.println("---- Tests ----");
            System.out.println("Create: " + r.getTests().create);
            System.out.println("Read: " + r.getTests().read);
            System.out.println("Detail: " + r.getTests().detail);
            System.out.println("Update: " + r.getTests().update);
            System.out.println("Delete: " + r.getTests().delete);
        }

        if (r.getDetails() != null) {
            System.out.println("---- Details ----");
            r.getDetails().forEach(System.out::println);
        }

        if (r.getError() != null) {
            System.out.println("❌ Error: " + r.getError());
        }

        System.out.println("=================\n");
    }
}