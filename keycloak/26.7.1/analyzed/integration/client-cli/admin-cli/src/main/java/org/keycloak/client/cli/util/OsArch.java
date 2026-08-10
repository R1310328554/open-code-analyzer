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
 * 操作系统与 CPU 架构的规范化描述。
 * <p>
 * 用于 CLI 脚本示例中的路径分隔符、环境变量占位符及旧版 Windows 标记。
 *
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class OsArch {

    /** 规范化 OS 标识（如 {@code linux}、{@code win32}）。 */
    private String os;
    /** 规范化架构标识（如 {@code x86_64}、{@code i686}）。 */
    private String arch;
    /** 是否为旧版 Windows（2003/2008/Vista）。 */
    private boolean legacy;

    /** 非旧版平台构造器。 */
    public OsArch(String os, String arch) {
        this(os, arch, false);
    }

    /**
     * 完整构造器。
     *
     * @param os 操作系统标识
     * @param arch CPU 架构
     * @param legacy 是否旧版 Windows
     */
    public OsArch(String os, String arch, boolean legacy) {
        this.os = os;
        this.arch = arch;
        this.legacy = legacy;
    }

    /** 返回 OS 标识。 */
    public String os() {
        return os;
    }

    /** 返回架构标识。 */
    public String arch() {
        return arch;
    }

    /** 是否为旧版 Windows 平台。 */
    public boolean isLegacy() {
        return legacy;
    }

    /** 判断是否为 Windows（{@code win32}）。 */
    public boolean isWindows() {
        return "win32".equals(os);
    }

    /**
     * 按平台返回环境变量占位符语法。
     *
     * @param var 变量名
     * @return Windows 为 {@code %VAR%}，Unix 为 {@code $VAR}
     */
    public String envVar(String var) {
        if (isWindows()) {
            return "%" + var + "%";
        } else {
            return "$" + var;
        }
    }

    /**
     * 按平台规范化路径字符串（分隔符与 {@code ~} 展开）。
     *
     * @param path 原始路径
     * @return 平台适配后的路径
     */
    public String path(String path) {
        if (isWindows()) {
            path = path.replaceAll("/", "\\\\");
            if (path.startsWith("~")) {
                path =  "%HOMEPATH%" + path.substring(1);
            }
        }
        return path;
    }
}
