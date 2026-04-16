package fuda.modules.analysis.services;

import fuda.modules.analysis.models.CriteriaReport;
import fuda.modules.analysis.models.RubricReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

public class AnalysisServiceImpl implements AnalysisService {

    @Override
    public List<CriteriaReport> findPassedCriteria(Path projectRoot, RubricReport rubric) throws IOException {
        validateProjectRoot(projectRoot);

        List<CriteriaReport> passed = new ArrayList<>();
        for (CriteriaReport criteria : getCriteria(rubric)) {
            if (checkCriteria(projectRoot, rubric, criteria)) {
                passed.add(criteria);
            }
        }
        return passed;
    }

    @Override
    public List<CriteriaReport> findFailedCriteria(Path projectRoot, RubricReport rubric) throws IOException {
        validateProjectRoot(projectRoot);

        List<CriteriaReport> failed = new ArrayList<>();
        for (CriteriaReport criteria : getCriteria(rubric)) {
            if (!checkCriteria(projectRoot, rubric, criteria)) {
                failed.add(criteria);
            }
        }
        return failed;
    }

    @Override
    public boolean checkCriteria(Path projectRoot, RubricReport rubric, CriteriaReport criteria) throws IOException {
        validateProjectRoot(projectRoot);
        if (criteria == null || criteria.type() == null) {
            return false;
        }

        return switch (criteria.type()) {
            case FILE_EXISTS -> evaluateFileExists(projectRoot, criteria);
            case REACT_SOURCE_EXISTS -> evaluateReactSourceExists(projectRoot, rubric, criteria);
            case REGEX_IN_SOURCE -> evaluateRegexInSource(projectRoot, rubric, criteria);
        };
    }

    @Override
    public int calculateEarnedWeight(Path projectRoot, RubricReport rubric) throws IOException {
        validateProjectRoot(projectRoot);

        int earnedWeight = 0;
        for (CriteriaReport criteria : getCriteria(rubric)) {
            if (checkCriteria(projectRoot, rubric, criteria)) {
                earnedWeight += Math.max(criteria.weight(), 0);
            }
        }
        return earnedWeight;
    }

    private List<CriteriaReport> getCriteria(RubricReport rubric) {
        if (rubric == null || rubric.criteria() == null) {
            return List.of();
        }
        return rubric.criteria();
    }

    private void validateProjectRoot(Path projectRoot) throws IOException {
        if (projectRoot == null || !Files.exists(projectRoot) || !Files.isDirectory(projectRoot)) {
            throw new IOException("Invalid project root: " + projectRoot);
        }
    }

    private boolean evaluateFileExists(Path projectRoot, CriteriaReport criteria) {
        List<String> paths = criteria.paths() == null ? List.of() : criteria.paths();
        if (paths.isEmpty()) {
            return false;
        }

        for (String relativePath : paths) {
            if (relativePath == null || relativePath.isBlank()) {
                return false;
            }

            Path resolvedPath = projectRoot.resolve(relativePath).normalize();
            if (!Files.exists(resolvedPath)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateReactSourceExists(Path projectRoot, RubricReport rubric, CriteriaReport criteria) {
        List<Path> sourceFiles = collectSourceFiles(projectRoot, rubric, criteria);
        int minMatches = criteria.minMatches() == null ? 1 : criteria.minMatches();
        return sourceFiles.size() >= Math.max(minMatches, 1);
    }

    private boolean evaluateRegexInSource(Path projectRoot, RubricReport rubric, CriteriaReport criteria) {
        String regex = criteria.pattern();

        if (regex == null || regex.isBlank()) {
            return false;
        }

        Pattern pattern;
        try {
            pattern = Pattern.compile(regex, Pattern.MULTILINE);
        } catch (PatternSyntaxException ex) {
            return false;
        }

        List<Path> sourceFiles = collectSourceFiles(projectRoot, rubric, criteria);
        int totalMatches = 0;

        for (Path file : sourceFiles) {
            try {
                String content = Files.readString(file);
                var matcher = pattern.matcher(content);
                while (matcher.find()) {
                    totalMatches++;
                }
            } catch (IOException ignored) {
                // Continue scanning other files if one file is unreadable.
            }
        }

        int minMatches = criteria.minMatches() == null ? 1 : criteria.minMatches();
        return totalMatches >= Math.max(minMatches, 1);
    }

    private List<Path> collectSourceFiles(Path projectRoot, RubricReport rubric, CriteriaReport criteria) {
        List<String> sourceRoots = resolveSourceRoots(rubric, criteria);
        Set<String> ignoreDirs = normalizeIgnoreDirs(rubric);

        List<Path> sourceFiles = new ArrayList<>();
        for (String root : sourceRoots) {
            if (root == null || root.isBlank()) continue;

            Path sourceRoot = projectRoot.resolve(root).normalize();
            if (!Files.exists(sourceRoot) || !Files.isDirectory(sourceRoot)) continue;

            try (Stream<Path> stream = Files.walk(sourceRoot)) {
                List<Path> found = stream
                        .filter(Files::isRegularFile)
                        .filter(this::isSourceFile)
                        .filter(path -> !isIgnored(path, ignoreDirs))
                        .sorted()
                        .toList();
                sourceFiles.addAll(found);
            } catch (IOException ignored) {
                // Ignore unreadable source roots and keep scanning others.
            }
        }

        return sourceFiles;
    }

    private List<String> resolveSourceRoots(RubricReport rubric, CriteriaReport criteria) {
        if (criteria != null && criteria.paths() != null && !criteria.paths().isEmpty()) {
            return criteria.paths();
        }
        if (rubric != null && rubric.sourceRoots() != null && !rubric.sourceRoots().isEmpty()) {
            return rubric.sourceRoots();
        }
        return List.of("src");
    }

    private Set<String> normalizeIgnoreDirs(RubricReport rubric) {
        List<String> configured = rubric == null || rubric.ignoreDirs() == null
                ? List.of("node_modules", ".git", "dist", "build", "coverage", ".next")
                : rubric.ignoreDirs();

        Set<String> normalized = new HashSet<>();
        for (String dir : configured) {
            if (dir != null && !dir.isBlank()) {
                normalized.add(dir.toLowerCase(Locale.ROOT));
            }
        }
        return normalized;
    }

    private boolean isSourceFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".js") || fileName.endsWith(".jsx");
    }

    private boolean isIgnored(Path path, Set<String> ignoreDirs) {
        for (Path part : path) {
            String segment = part.toString().toLowerCase(Locale.ROOT);
            if (ignoreDirs.contains(segment)) {
                return true;
            }
        }
        return false;
    }
}
