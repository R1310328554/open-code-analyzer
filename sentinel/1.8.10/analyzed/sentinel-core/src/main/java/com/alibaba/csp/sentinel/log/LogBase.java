/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.log;


import java.io.File;
import java.util.Properties;
import java.util.logging.Level;

import static com.alibaba.csp.sentinel.util.ConfigUtil.addSeparator;

/**
 * <p>Sentinel 日志基础配置类。</p>
 *
 * <p>
 * 默认日志根目录为 {@code ${user.home}/logs/csp/}，可通过 {@link #LOG_DIR}
 * 属性覆盖。默认日志文件名不含 pid；若同一机器上运行同一服务的多个实例，
 * 可通过将 {@link #LOG_NAME_USE_PID} 配置为 {@code true} 在文件名中区分进程。
 * </p>
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
public class LogBase {

    public static final String LOG_DIR = "csp.sentinel.log.dir";
    public static final String LOG_NAME_USE_PID = "csp.sentinel.log.use.pid";
    public static final String LOG_OUTPUT_TYPE = "csp.sentinel.log.output.type";
    public static final String LOG_CHARSET = "csp.sentinel.log.charset";
    public static final String LOG_LEVEL = "csp.sentinel.log.level";

    /**
     * 将业务日志（如 RecordLog、CommandCenterLog）输出到文件。
     */
    public static final String LOG_OUTPUT_TYPE_FILE = "file";
    /**
     * 将业务日志（如 RecordLog、CommandCenterLog）输出到控制台。
     */
    public static final String LOG_OUTPUT_TYPE_CONSOLE = "console";
    public static final String LOG_CHARSET_UTF8 = "utf-8";

    private static final String DIR_NAME = "logs" + File.separator + "csp";
    private static final String USER_HOME = "user.home";
    private static final Level LOG_DEFAULT_LEVEL = Level.INFO;


    private static boolean logNameUsePid;
    private static String logOutputType;
    private static String logBaseDir;
    private static String logCharSet;
    private static Level logLevel;

    static {
        try {
            initializeDefault();
            loadProperties();
        } catch (Throwable t) {
            System.err.println("[LogBase] FATAL ERROR when initializing logging config");
            t.printStackTrace();
        }
    }

    private static void initializeDefault() {
        logNameUsePid = false;
        logOutputType = LOG_OUTPUT_TYPE_FILE;
        logBaseDir = addSeparator(System.getProperty(USER_HOME)) + DIR_NAME + File.separator;
        logCharSet = LOG_CHARSET_UTF8;
        logLevel = LOG_DEFAULT_LEVEL;
    }

    private static void loadProperties() {
        Properties properties = LogConfigLoader.getProperties();

        logOutputType = properties.get(LOG_OUTPUT_TYPE) == null ? logOutputType : properties.getProperty(LOG_OUTPUT_TYPE);
        if (!LOG_OUTPUT_TYPE_FILE.equalsIgnoreCase(logOutputType) && !LOG_OUTPUT_TYPE_CONSOLE.equalsIgnoreCase(logOutputType)) {
            logOutputType = LOG_OUTPUT_TYPE_FILE;
        }
        System.out.println("INFO: Sentinel log output type is: " + logOutputType);

        logCharSet = properties.getProperty(LOG_CHARSET) == null ? logCharSet : properties.getProperty(LOG_CHARSET);
        System.out.println("INFO: Sentinel log charset is: " + logCharSet);


        logBaseDir = properties.getProperty(LOG_DIR) == null ? logBaseDir : properties.getProperty(LOG_DIR);
        logBaseDir = addSeparator(logBaseDir);
        File dir = new File(logBaseDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("ERROR: create Sentinel log base directory error: " + logBaseDir);
            }
        }
        System.out.println("INFO: Sentinel log base directory is: " + logBaseDir);

        String usePid = properties.getProperty(LOG_NAME_USE_PID);
        logNameUsePid = "true".equalsIgnoreCase(usePid);
        System.out.println("INFO: Sentinel log name use pid is: " + logNameUsePid);

        // 加载日志级别
        String logLevelString = properties.getProperty(LOG_LEVEL);
        if (logLevelString != null && (logLevelString = logLevelString.trim()).length() > 0) {
            try {
                logLevel = Level.parse(logLevelString);
            } catch (IllegalArgumentException e) {
                System.out.println("Log level : " + logLevel + " is invalid. Use default : " + LOG_DEFAULT_LEVEL.toString());
            }
        }
        System.out.println("INFO: Sentinel log level is: " + logLevel);
    }


    /**
     * 日志文件名是否应包含 pid，由 {@link #LOG_NAME_USE_PID} 系统属性控制。
     *
     * @return 若文件名应包含 pid 则返回 true，否则 false
     */
    public static boolean isLogNameUsePid() {
        return logNameUsePid;
    }

    /**
     * 获取日志文件根目录路径，保证以 {@link File#separator} 结尾。
     *
     * @return 日志文件根目录路径
     */
    public static String getLogBaseDir() {
        return logBaseDir;
    }

    /**
     * 获取日志输出类型。
     *
     * @return 日志输出类型，默认为 "file"
     */
    public static String getLogOutputType() {
        return logOutputType;
    }

    /**
     * 获取日志文件字符集。
     *
     * @return 日志文件字符集，默认为 "utf-8"
     */
    public static String getLogCharset() {
        return logCharSet;
    }

    public static Level getLogLevel() {
        return logLevel;
    }
}
