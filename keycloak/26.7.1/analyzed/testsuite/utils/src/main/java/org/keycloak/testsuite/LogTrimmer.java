package org.keycloak.testsuite;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 精简测试输出的工具类：成功用例仅保留摘要，失败用例输出完整日志。
 * 最初为缩短 Travis CI 日志而创建。
 *
 * Created by st on 03/07/17.
 */
public class LogTrimmer {

    /** 匹配 Maven Surefire 测试开始行的正则。 */
    private static Pattern TEST_START_PATTERN = Pattern.compile("(\\[INFO\\] )?Running (.*)");
    /** 测试类名捕获组索引。 */
    private static int TEST_NAME_GROUP = 2;

    /** 从标准输入读取日志并过滤输出。 */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            String testRunning = null;
            String line = null;
            Matcher testMatcher = null;
            StringBuilder testText = new StringBuilder();

            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (testRunning == null) {
                    testMatcher = TEST_START_PATTERN.matcher(line);
                    if (testMatcher.find()) {
                        testRunning = testMatcher.group(TEST_NAME_GROUP);
                        System.out.println(line);
                    } else {
                        System.out.println("-- " + line);
                    }
                } else {
                    if (line.contains("Tests run:")) {
                        if (!(line.contains("Failures: 0") && line.contains("Errors: 0"))) {
                            System.out.println("--------- " + testRunning + " output start ---------");
                            System.out.println(testText.toString());
                            System.out.println("--------- " + testRunning + " output end  ---------");
                        }
                        System.out.println(line);
                        testRunning = null;
                        testText = new StringBuilder();
                    } else {
                        testText.append(testRunning.substring(testRunning.lastIndexOf('.') + 1) + " ++ " + line);
                        testText.append("\n");
                    }
                }
            }
        }
    }
}
