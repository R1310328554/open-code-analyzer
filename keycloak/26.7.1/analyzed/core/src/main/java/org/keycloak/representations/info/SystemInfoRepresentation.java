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

package org.keycloak.representations.info;


import java.util.Date;
import java.util.Locale;

/**
 * Keycloak 服务器运行环境与 JVM/OS 信息的 REST 表示。
 */
public class SystemInfoRepresentation {

    /** Keycloak 服务器版本号。 */
    private String version;
    /** 当前服务器时间（字符串形式）。 */
    private String serverTime;
    /** 可读格式的运行时长。 */
    private String uptime;
    /** 运行时长（毫秒）。 */
    private Long uptimeMillis;
    /** Java 版本。 */
    private String javaVersion;
    /** Java 供应商。 */
    private String javaVendor;
    /** Java 虚拟机名称。 */
    private String javaVm;
    /** Java 虚拟机版本。 */
    private String javaVmVersion;
    /** Java 运行时名称。 */
    private String javaRuntime;
    /** JAVA_HOME 路径。 */
    private String javaHome;
    /** 操作系统名称。 */
    private String osName;
    /** 操作系统架构。 */
    private String osArchitecture;
    /** 操作系统版本。 */
    private String osVersion;
    /** 默认文件编码。 */
    private String fileEncoding;
    /** 运行进程的用户名。 */
    private String userName;
    /** 进程工作目录。 */
    private String userDir;
    /** 用户时区 ID。 */
    private String userTimezone;
    /** 用户区域设置（language_country）。 */
    private String userLocale;

    /**
     * 根据服务器启动时间与版本号填充系统信息快照。
     *
     * @param serverStartupTime 服务器启动时间戳（毫秒）
     * @param serverVersion     Keycloak 版本字符串
     * @return 填充完毕的系统信息对象
     */
    public static SystemInfoRepresentation create(long serverStartupTime, String serverVersion) {
        SystemInfoRepresentation rep = new SystemInfoRepresentation();
        rep.version = serverVersion;
        rep.serverTime = new Date().toString();
        rep.uptimeMillis = System.currentTimeMillis() - serverStartupTime;
        rep.uptime = formatUptime(rep.uptimeMillis);
        rep.javaVersion = System.getProperty("java.version");
        rep.javaVendor = System.getProperty("java.vendor");
        rep.javaVm = System.getProperty("java.vm.name");
        rep.javaVmVersion = System.getProperty("java.vm.version");
        rep.javaRuntime = System.getProperty("java.runtime.name");
        rep.javaHome = System.getProperty("java.home");
        rep.osName = System.getProperty("os.name");
        rep.osArchitecture = System.getProperty("os.arch");
        rep.osVersion = System.getProperty("os.version");
        rep.fileEncoding = System.getProperty("file.encoding");
        rep.userName = System.getProperty("user.name");
        rep.userDir = System.getProperty("user.dir");
        rep.userTimezone = System.getProperty("user.timezone");
        if (System.getProperty("user.country") != null && System.getProperty("user.language") != null) {
            rep.userLocale = (new Locale(System.getProperty("user.language"), System.getProperty("user.country")).toString());
        }
        return rep;
    }

    /** @return 服务器版本 */
    public String getVersion() {
        return version;
    }

    /** @param version 服务器版本 */
    public void setVersion(String version) {
        this.version = version;
    }

    /** @return 服务器当前时间 */
    public String getServerTime() {
        return serverTime;
    }

    /** @param serverTime 服务器当前时间 */
    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }

    /** @return 可读运行时长 */
    public String getUptime() {
        return uptime;
    }

    /** @param uptime 可读运行时长 */
    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    /** @return 运行时长（毫秒） */
    public Long getUptimeMillis() {
        return uptimeMillis;
    }

    /** @param uptimeMillis 运行时长（毫秒） */
    public void setUptimeMillis(Long uptimeMillis) {
        this.uptimeMillis = uptimeMillis;
    }

    /** @return Java 版本 */
    public String getJavaVersion() {
        return javaVersion;
    }

    /** @param javaVersion Java 版本 */
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    /** @return Java 供应商 */
    public String getJavaVendor() {
        return javaVendor;
    }

    /** @param javaVendor Java 供应商 */
    public void setJavaVendor(String javaVendor) {
        this.javaVendor = javaVendor;
    }

    /** @return Java 虚拟机名称 */
    public String getJavaVm() {
        return javaVm;
    }

    /** @param javaVm Java 虚拟机名称 */
    public void setJavaVm(String javaVm) {
        this.javaVm = javaVm;
    }

    /** @return Java 虚拟机版本 */
    public String getJavaVmVersion() {
        return javaVmVersion;
    }

    /** @param javaVmVersion Java 虚拟机版本 */
    public void setJavaVmVersion(String javaVmVersion) {
        this.javaVmVersion = javaVmVersion;
    }

    /** @return Java 运行时名称 */
    public String getJavaRuntime() {
        return javaRuntime;
    }

    /** @param javaRuntime Java 运行时名称 */
    public void setJavaRuntime(String javaRuntime) {
        this.javaRuntime = javaRuntime;
    }

    /** @return JAVA_HOME */
    public String getJavaHome() {
        return javaHome;
    }

    /** @param javaHome JAVA_HOME */
    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    /** @return 操作系统名称 */
    public String getOsName() {
        return osName;
    }

    /** @param osName 操作系统名称 */
    public void setOsName(String osName) {
        this.osName = osName;
    }

    /** @return 操作系统架构 */
    public String getOsArchitecture() {
        return osArchitecture;
    }

    /** @param osArchitecture 操作系统架构 */
    public void setOsArchitecture(String osArchitecture) {
        this.osArchitecture = osArchitecture;
    }

    /** @return 操作系统版本 */
    public String getOsVersion() {
        return osVersion;
    }

    /** @param osVersion 操作系统版本 */
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    /** @return 文件编码 */
    public String getFileEncoding() {
        return fileEncoding;
    }

    /** @param fileEncoding 文件编码 */
    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    /** @return 进程用户名 */
    public String getUserName() {
        return userName;
    }

    /** @param userName 进程用户名 */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** @return 工作目录 */
    public String getUserDir() {
        return userDir;
    }

    /** @param userDir 工作目录 */
    public void setUserDir(String userDir) {
        this.userDir = userDir;
    }

    /** @return 用户时区 */
    public String getUserTimezone() {
        return userTimezone;
    }

    /** @param userTimezone 用户时区 */
    public void setUserTimezone(String userTimezone) {
        this.userTimezone = userTimezone;
    }

    /** @return 用户区域设置 */
    public String getUserLocale() {
        return userLocale;
    }

    /** @param userLocale 用户区域设置 */
    public void setUserLocale(String userLocale) {
        this.userLocale = userLocale;
    }

    /**
     * 将毫秒时长格式化为「天、小时、分钟、秒」英文可读字符串。
     *
     * @param uptime 运行时长（毫秒）
     * @return 格式化后的时长描述
     */
    private static String formatUptime(long uptime) {
        long diffInSeconds = uptime / 1000;
        long diff[] = new long[]{0, 0, 0, 0}; // sec
        diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds); // min
        diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds; // hours
        diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds; // days
        diff[0] = (diffInSeconds = (diffInSeconds / 24));

        return String.format(
                "%d day%s, %d hour%s, %d minute%s, %d second%s",
                diff[0],
                diff[0] != 1 ? "s" : "",
                diff[1],
                diff[1] != 1 ? "s" : "",
                diff[2],
                diff[2] != 1 ? "s" : "",
                diff[3],
                diff[3] != 1 ? "s" : "");
    }

}
