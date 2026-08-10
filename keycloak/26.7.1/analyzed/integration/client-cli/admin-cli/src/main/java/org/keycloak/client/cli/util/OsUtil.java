/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.client.cli.util;

/**
 * 运行时操作系统检测与 CLI 展示常量。
 * <p>
 * 启动时解析 {@code os.name}/{@code os.arch}，映射为 Keycloak 脚本包所用的
 * {@link OsArch} 标识，并提供平台相关的提示符与换行符。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class OsUtil {

    /** 当前 JVM 对应的规范化 OS/架构。 */
    public static final OsArch OS_ARCH = determineOSAndArch();

    /** 交互式示例命令行提示符。 */
    public static final String PROMPT = OS_ARCH.isWindows() ? "c:\\>" : "$";

    /** 平台换行符（Windows 为 CRLF，其余为 LF）。 */
    public static final String EOL = OS_ARCH.isWindows() ? "\r\n" : "\n";


    /**
     * 检测并规范化当前操作系统与 CPU 架构。
     * <p>
     * 支持 Linux、Windows、macOS、Solaris、FreeBSD、OpenBSD；未知平台抛出异常。
     *
     * @return 规范化 {@link OsArch} 实例
     */
    public static OsArch determineOSAndArch() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch");

        if (arch.equals("amd64")) {
            arch = "x86_64";
        }

        if (os.startsWith("linux")) {
            if (arch.equals("x86") || arch.equals("i386") || arch.equals("i586")) {
                arch = "i686";
            }
            return new OsArch("linux", arch);
        } else if (os.startsWith("windows")) {
            if (arch.equals("x86")) {
                arch = "i386";
            }
            if (os.indexOf("2008") != -1 || os.indexOf("2003") != -1 || os.indexOf("vista") != -1) {
                return new OsArch("win32", arch, true);
            } else {
                return new OsArch("win32", arch);
            }
        } else if (os.startsWith("sunos")) {
            return new OsArch("sunos5", "x86_64");
        } else if (os.startsWith("mac os x")) {
            return new OsArch("osx", "x86_64");
        } else if (os.startsWith("freebsd")) {
            return new OsArch("freebsd", arch);
        } else if (os.startsWith("openbsd")) {
            return new OsArch("openbsd", arch);
        }

        // 不支持的平台
        throw new RuntimeException("Could not determine OS and architecture for this operating system: " + os);
    }
}
