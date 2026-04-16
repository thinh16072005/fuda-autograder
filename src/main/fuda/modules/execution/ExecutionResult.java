package fuda.modules.execution;

import java.util.List;

public class ExecutionResult {
    private boolean success;
    private int score;
    private List<String> details;
    private TestResult tests;
    private String error;
    private long durationMs;

    public static class TestResult {
        public boolean create;
        public boolean read;
        public boolean detail;
        public Boolean update;
        public boolean delete;
    }

    // Getters & Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }

    public TestResult getTests() { return tests; }
    public void setTests(TestResult tests) { this.tests = tests; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}