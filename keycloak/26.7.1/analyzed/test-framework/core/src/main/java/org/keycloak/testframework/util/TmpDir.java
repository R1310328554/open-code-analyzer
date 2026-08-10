package org.keycloak.testframework.util;

import java.io.File;

/**
 * 解析测试框架使用的临时目录。
 * <p>
 * Maven 可能覆盖 {@code java.io.tmpdir}；本工具优先使用 {@code /tmp} 或 Windows {@code TEMP}。
 */
public class TmpDir {

    // Maven 会覆盖 java.io.tmpdir，此处显式解析操作系统临时目录
    /**
     * 返回可用的临时目录。
     * <p>
     * 依次尝试 {@code /tmp}、环境变量 {@code TEMP}、{@code java.io.tmpdir}。
     *
     * @return 存在且为目录的路径
     */
    public static File resolveTmpDir() {
        File tmpDir = new File("/tmp");
        if (tmpDir.isDirectory()) {
            return tmpDir;
        }
        tmpDir = new File(System.getenv("TEMP"));
        if (tmpDir.isDirectory()) {
            return tmpDir;
        }
        return new File(System.getProperty("java.io.tmpdir"));
    }

}
