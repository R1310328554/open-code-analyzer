package org.keycloak.testframework.github;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.keycloak.testframework.config.Config;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * GitHub Actions 工作流中的测试报告生成器。
 * <p>
 * 将失败用例与慢测试写入 {@code GITHUB_STEP_SUMMARY} Markdown 文件，便于 CI 概览。
 */
public class GitHubActionReport {

    private static final String GITHUB_STEP_SUMMARY = System.getenv("GITHUB_STEP_SUMMARY");
    private static final String GITHUB_SERVER_URL = System.getenv("GITHUB_SERVER_URL");
    private static final String GITHUB_REPOSITORY = System.getenv("GITHUB_REPOSITORY");
    private static final String GITHUB_SHA = System.getenv("GITHUB_SHA");
    private static final String GIT_ROOT = findGitRoot();

    /** 是否启用报告（需环境变量与配置同时满足）。 */
    private final boolean enabled;
    /** GitHub Step Summary 输出文件。 */
    private final File gitHubStepSummary;

    /** 测试类慢执行阈值（毫秒）。 */
    private final long slowTestClassTimeout;
    /** 单个测试方法慢执行阈值（毫秒）。 */
    private final long slowTestTimeout;

    /** 当前测试类开始时间戳。 */
    private long testClassStartedAt;
    /** 当前测试方法开始时间戳。 */
    private long testStartedAt;

    /** 收集的失败用例记录。 */
    private List<Failure> failures = new LinkedList<>();
    /** 收集的慢测试记录。 */
    private List<Slow> slowTests = new LinkedList<>();

    /** 从环境变量与 {@link Config} 读取阈值并初始化。 */
    public GitHubActionReport() {
        this.gitHubStepSummary = GITHUB_STEP_SUMMARY != null ? new File(GITHUB_STEP_SUMMARY) : null;
        this.enabled = Config.get("kc.test.github.enabled", true, Boolean.class) && gitHubStepSummary != null;
        this.slowTestClassTimeout = TimeUnit.SECONDS.toMillis(Config.get("kc.test.github.slow.class", 120L, Long.class));
        this.slowTestTimeout = TimeUnit.SECONDS.toMillis(Config.get("kc.test.github.slow.method", 30L, Long.class));
    }

    /** 测试类开始时记录时间戳。 */
    public void onClassStart() {
        if (enabled) {
            testClassStartedAt = System.currentTimeMillis();
        }
    }

    /** 测试类成功结束时检查是否超过类级慢阈值。 */
    public void onClassSuccess(ExtensionContext context) {
        if (enabled) {
            if (slowTestClassTimeout >= -1) {
                long executionTime = System.currentTimeMillis() - testClassStartedAt;
                if (executionTime > slowTestClassTimeout) {
                    Class<?> testClass = context.getRequiredTestClass();
                    String file = findJavaClass(testClass);
                    String link = getLink(file, -1);
                    slowTests.add(new Slow(context.getRequiredTestClass().getName(), null, executionTime, link));
                }
            }
        }
    }

    /** 测试类失败时记录失败信息。 */
    public void onClassError(ExtensionContext context) {
        if (enabled) {
            onError(context, false);
        }
    }

    /** 测试方法开始时记录时间戳。 */
    public void onMethodStart() {
        if (enabled && slowTestTimeout >= -1) {
            testStartedAt = System.currentTimeMillis();
        }
    }

    /** 测试方法成功结束时检查是否超过方法级慢阈值。 */
    public void onMethodSuccess(ExtensionContext context) {
        if (enabled) {
            if (slowTestTimeout >= -1) {
                long executionTime = System.currentTimeMillis() - testStartedAt;
                if (executionTime > slowTestTimeout) {
                    Class<?> testClass = context.getRequiredTestClass();
                    String file = findJavaClass(testClass);
                    String link = getLink(file, -1);
                    slowTests.add(new Slow(context.getRequiredTestClass().getName(), context.getRequiredTestMethod().getName(), executionTime, link));
                }
            }
        }
    }

    /** 测试方法失败时记录失败信息。 */
    public void onMethodFailed(ExtensionContext context) {
        if (enabled) {
            onError(context, true);
        }
    }

    /** 将失败与慢测试表格追加写入 Step Summary 文件。 */
    public void printSummary() {
        if (enabled && (!failures.isEmpty() || !slowTests.isEmpty())) {
            try {
                PrintWriter printWriter = new PrintWriter(new FileWriter(gitHubStepSummary, true));

                if (!failures.isEmpty()) {
                    printWriter.println("## :x: Failed tests");
                    printWriter.println("| Test class | Test method | Line | Failure |");
                    printWriter.println("| ---------- | ----------- | ---- | ------- |");

                    failures.stream().sorted(Comparator.comparing(Failure::className)).forEach(f ->
                            printWriter.println("| " + createLink(f.className(), f.link()) + " | " + (f.methodName() != null ? f.methodName() : "") + " | " + (f.line() >= 0 ? f.line() : "") + " | `" + f.message() + "` |")
                    );
                }

                if (!slowTests.isEmpty()) {
                    printWriter.println("## :hourglass: Slow tests detected");
                    printWriter.println("| Test class | Test method | Execution time (s) |");
                    printWriter.println("| ---------- | ----------- | -------------- |");

                    slowTests.stream().sorted(Comparator.comparing(Slow::executionTime).reversed()).forEach(s ->
                            printWriter.println("| " + createLink(s.className(), s.link()) + " | " + (s.methodName() != null ? s.methodName() : "") + " | " + (s.executionTime() / 1000) + " |")
                    );
                }

                printWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** 提取失败消息、源码行号并生成 GitHub 链接。 */
    private void onError(ExtensionContext context, boolean method) {
        Optional<Throwable> executionException = context.getExecutionException();
        if (executionException.isPresent()) {
            Class<?> testClass = context.getRequiredTestClass();
            String file = findJavaClass(testClass);

            Method testMethod = method ? context.getRequiredTestMethod() : null;
            Throwable throwable = executionException.get();
            String message = throwable.getMessage();
            int line = findLine(testClass, testMethod, throwable);

            String link = getLink(file, line);

            failures.add(new Failure(testClass.getName(), testMethod != null ? testMethod.getName() : null, message, link, line));
        }
    }

    /** 将测试类映射为仓库内 Java 源文件相对路径。 */
    private String findJavaClass(Class<?> testClass) {
        if (GIT_ROOT == null) {
            return null;
        }

        String classFile = testClass.getResource("/" + testClass.getName().replace('.', '/') + ".class").getFile();
        return classFile.replace(GIT_ROOT + "/", "").replace("target/test-classes", "src/test/java").replace(".class", ".java");
    }

    /** 构造指向 GitHub blob 指定行号的链接。 */
    private String getLink(String file, int line) {
        if (file == null) {
            return null;
        }
        String link = GITHUB_SERVER_URL + "/" + GITHUB_REPOSITORY + "/blob/" + GITHUB_SHA + "/" + file;
        if (line >= 0) {
            link += "#L" + line;
        }
        return link;
    }

    /** 自当前工作目录向上查找 {@code .git} 根目录。 */
    private static String findGitRoot() {
        File file = new File(System.getProperty("user.dir"));
        while (file != null && file.isDirectory()) {
            if (new File(file, ".git").isDirectory()) {
                return file.getAbsolutePath();
            }
            file = file.getParentFile();
        }
        return null;
    }

    /** 从异常堆栈中定位测试类/方法的源码行号。 */
    private int findLine(Class<?> testClass, Method testMethod, Throwable throwable) {
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            if (stackTraceElement.getClassName().equals(testClass.getName()) && (testMethod == null || stackTraceElement.getMethodName().equals(testMethod.getName()))) {
                return stackTraceElement.getLineNumber();
            }
        }
        return -1;
    }

    /** 生成 Markdown 链接，无链接时返回纯文本。 */
    private String createLink(String text, String link) {
        if (link == null) {
            return text;
        }
        return "[" + text + "]" + "(" + link + ")";
    }

    /** 慢测试记录。 */
    private record Slow(String className, String methodName, long executionTime, String link) {}

    /** 失败用例记录。 */
    private record Failure(String className, String methodName, String message, String link, int line) {}

}
