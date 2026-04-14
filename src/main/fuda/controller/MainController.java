package fuda.controller;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;
import java.util.regex.*;

/**
 * MainController – Hệ thống tự động quét và chạy bài làm React JS sinh viên.
 * Hỗ trợ format: MônHọc_KỳHọc_MSSV (Ví dụ: FER202_PE_SPR26_HE123456)
 */
public class MainController {

    private static final Pattern FOLDER_PATTERN = Pattern.compile(
            "^([A-Z]+\\d+)_(PE|FE|ME)_((?:SPR|SUM|FAL|SP|SU|FA)\\d{2})_([A-Z0-9]+)$",
            Pattern.CASE_INSENSITIVE);

    private final Queue<File> studentQueue = new LinkedList<>();
    private Process currentProcess = null;
    private String currentMSSV = "";
    private int totalFiles = 0;
    private int processedCount = 0;

    /**
     * Nạp danh sách bài làm từ thư mục mẹ.
     *
     * @return true nếu tìm thấy ít nhất 1 bài hợp lệ.
     */
    public boolean loadFromPath(String path) {
        File dir = cleanPath(path);
        System.out.println("\n🔍 Đang quét: " + dir.getAbsolutePath());

        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("❌ Thư mục không tồn tại. Vui lòng kiểm tra lại!");
            return false;
        }

        File[] subDirs = dir.listFiles(File::isDirectory);
        if (subDirs == null || subDirs.length == 0) {
            System.err.println("❌ Không tìm thấy thư mục con nào.");
            return false;
        }

        studentQueue.clear();
        Arrays.stream(subDirs)
                .sorted(Comparator.comparing(File::getName))
                .filter(this::isValidProject)
                .forEach(studentQueue::add);

        totalFiles = studentQueue.size();
        processedCount = 0;

        System.out.printf("✅ Tìm thấy %d bài nộp hợp lệ.%n", totalFiles);
        return totalFiles > 0;
    }

    /**
     * Dừng bài hiện tại và chạy bài kế tiếp.
     */
    public boolean runNext() throws IOException, InterruptedException {
        stopSession();

        File project = studentQueue.poll();
        if (project == null) {
            System.out.println("\n🎉 HOÀN THÀNH: Đã chạy hết danh sách bài nộp!");
            return false;
        }

        processedCount++;
        currentMSSV = parseMSSV(project.getName());

        printHeader(String.format("[%d/%d] Sinh viên: %s", processedCount, totalFiles, currentMSSV));

        int port = 5173; // Luôn ưu tiên 5173 sau khi đã stopSession() và delay
        if (!isPortAvailable(port))
            port = findFreePort(5174);

        if (runCommand(project, "npm", "install").waitFor() == 0) {
            currentProcess = runCommand(project, "npm", "run", "dev", "--", "--port", String.valueOf(port), "--host");

            System.out.println("⏳ Đang khởi động Dev Server...");
            Thread.sleep(3000); // Chờ server lên hẳn

            if (currentProcess.isAlive()) {
                String url = "http://localhost:" + port;
                System.out.println("🚀 Đang chạy tại: " + url);
                openLink(url);
            } else {
                System.err.println("❌ Không thể khởi động Dev Server.");
            }
        } else {
            System.err.println("❌ Lỗi khi chạy 'npm install'.");
        }

        return true;
    }

    public void stopSession() {
        if (currentProcess == null || !currentProcess.isAlive())
            return;

        try {
            System.out.print("🛑 Đang đóng phiên làm việc... ");
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("taskkill", "/F", "/T", "/PID", String.valueOf(currentProcess.pid())).start()
                        .waitFor();
            } else {
                currentProcess.destroyForcibly();
            }
            Thread.sleep(800); // Delay ngắn giải phóng port
            System.out.println("Xong.");
        } catch (Exception e) {
            currentProcess.destroyForcibly();
        }
    }

    // --- Helper Methods ---

    private boolean isValidProject(File dir) {
        boolean match = FOLDER_PATTERN.matcher(dir.getName()).matches();
        boolean hasPackage = new File(dir, "package.json").exists();
        if (!match)
            System.out.println("  ⚠️ Bỏ qua (Sai tên): " + dir.getName());
        else if (!hasPackage)
            System.out.println("  ⚠️ Bỏ qua (Thiếu package.json): " + dir.getName());
        return match && hasPackage;
    }

    private String parseMSSV(String name) {
        Matcher m = FOLDER_PATTERN.matcher(name);
        return m.find() ? m.group(4).toUpperCase() : name;
    }

    private Process runCommand(File dir, String... args) throws IOException {
        List<String> cmd = new ArrayList<>();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            cmd.add("cmd");
            cmd.add("/c");
        }
        cmd.addAll(Arrays.asList(args));

        return new ProcessBuilder(cmd)
                .directory(dir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();
    }

    private File cleanPath(String path) {
        String p = path.replaceAll("^\"|\"$", "").trim()
                .replaceAll("[^\\x20-\\x7e]", "");
        File f = new File(p);
        return f.isAbsolute() ? f : f.getAbsoluteFile();
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket s = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private int findFreePort(int start) {
        for (int p = start; p < start + 50; p++)
            if (isPortAvailable(p))
                return p;
        return start;
    }

    private void openLink(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win"))
                Runtime.getRuntime().exec("cmd /c start " + url);
            else if (os.contains("mac"))
                Runtime.getRuntime().exec("open " + url);
            else
                Runtime.getRuntime().exec("xdg-open " + url);
        } catch (IOException ignored) {
        }
    }

    private void printHeader(String title) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  " + title);
        System.out.println("=".repeat(50));
    }

    // --- Main Entry ---

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        MainController app = new MainController();

        System.out.println("=== FUDA AUTOGRADER v1.1 ===");

        while (true) {
            System.out.print("👉 Nhập đường dẫn folder (hoặc kéo folder vào đây): ");
            if (app.loadFromPath(sc.nextLine()))
                break;
        }

        while (app.runNext()) {
            System.out.println("\nPRESS [ENTER] TO CONTINUE TO NEXT STUDENT...");
            sc.nextLine();
        }

        sc.close();
    }
}
