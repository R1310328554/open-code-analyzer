import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 测试日志裁剪工具：成功通过的测试仅保留摘要行，失败测试保留完整输出。
 * <p>
 * 为 GitHub Actions 等 CI 环境缩减 Maven Surefire 日志体积；由 st 创建于 2017-07-03。
 *
 * Created to shrink down the output for GitHub Actions.
 *
 * Created by st on 03/07/17.
 */
public class LogTrimmer {

    /** 匹配 Surefire “Running …” 行，可选 {@code [INFO] } 前缀。 */
    private static Pattern TEST_START_PATTERN = Pattern.compile("(\\[INFO\\] )?Running (.*)");
    /** 正则捕获组：测试类全名。 */
    private static int TEST_NAME_GROUP = 2;

    /**
     * 从标准输入逐行读取 Maven 测试输出并按规则裁剪后写到标准输出。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            String testRunning = null;
            String line = null;
            Matcher testMatcher = null;
            StringBuilder testText = new StringBuilder();

            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (testRunning == null) {
                    // 尚未进入某个测试的输出块：识别 Running 行或原样前缀输出
                    testMatcher = TEST_START_PATTERN.matcher(line);
                    if (testMatcher.find()) {
                        testRunning = testMatcher.group(TEST_NAME_GROUP);
                        System.out.println(line);
                    } else {
                        System.out.println("-- " + line);
                    }
                } else {
                    // 正在缓冲某个测试的中间输出
                    if (line.contains("Tests run:")) {
                        // 汇总行：若存在失败或错误则刷出此前缓冲的详细日志
                        if (!(line.contains("Failures: 0") && line.contains("Errors: 0"))) {
                            System.out.println("--------- " + testRunning + " output start ---------");
                            System.out.println(testText.toString());
                            System.out.println("--------- " + testRunning + " output end  ---------");
                        }
                        System.out.println(line);
                        testRunning = null;
                        testText = new StringBuilder();
                    } else {
                        // 累积单行输出，前缀短类名便于定位
                        testText.append(testRunning.substring(testRunning.lastIndexOf('.') + 1) + " ++ " + line);
                        testText.append("\n");
                    }
                }
            }
        }
    }
}
