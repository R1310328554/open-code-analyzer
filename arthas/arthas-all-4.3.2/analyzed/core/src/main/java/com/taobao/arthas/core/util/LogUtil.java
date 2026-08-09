package com.taobao.arthas.core.util;

import java.io.File;
import java.util.Iterator;

import com.alibaba.arthas.deps.ch.qos.logback.classic.LoggerContext;
import com.alibaba.arthas.deps.ch.qos.logback.classic.joran.JoranConfigurator;
import com.alibaba.arthas.deps.ch.qos.logback.classic.spi.ILoggingEvent;
import com.alibaba.arthas.deps.ch.qos.logback.core.Appender;
import com.alibaba.arthas.deps.ch.qos.logback.core.rolling.RollingFileAppender;
import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.common.AnsiLog;
import com.taobao.arthas.core.env.ArthasEnvironment;

/**
 * Arthas 自身 Logback 日志初始化与路径解析工具。
 * <p>
 * 从 {@link ArthasEnvironment} 读取 logback.xml 位置、日志文件名/目录等配置，
 * 并暴露 arthas.log 实际路径及缓存目录供 dump 等命令使用。
 *
 * @author hengyunabc
 */
public class LogUtil {

    /** 配置项：logback.xml 配置文件路径 */
    public static final String LOGGING_CONFIG_PROPERTY = "arthas.logging.config";
    /** 占位符解析：默认 {@code ${arthas.home}/logback.xml} */
    public static final String LOGGING_CONFIG = "${arthas.logging.config:${arthas.home}/logback.xml}";

    /**
     * 配置项：日志文件名（可为绝对路径或相对当前目录）。
     */
    public static final String FILE_NAME_PROPERTY = "arthas.logging.file.name";
    /** Logback 上下文属性键：日志文件路径 */
    public static final String ARTHAS_LOG_FILE = "ARTHAS_LOG_FILE";

    /**
     * 配置项：日志文件所在目录。
     */
    public static final String FILE_PATH_PROPERTY = "arthas.logging.file.path";
    /** Logback 上下文属性键：日志目录 */
    public static final String ARTHAS_LOG_PATH = "ARTHAS_LOG_PATH";

    /** 解析到的 arthas.log 绝对路径缓存 */
    private static String logFile = "";

    /**
     * 初始化 Arthas 内置 Logback 日志系统。
     * <pre>
     * 1. 从 arthas.logging.config（或 arthas.home 下 logback.xml）加载配置
     * 2. 可通过 arthas.logging.file.name / arthas.logging.file.path 覆盖日志文件位置
     * </pre>
     *
     * @param env Arthas 环境配置
     * @return 配置成功后的 LoggerContext；配置缺失或失败时返回 null
     */
    public static LoggerContext initLogger(ArthasEnvironment env) {
        String loggingConfig = env.resolvePlaceholders(LOGGING_CONFIG);
        if (loggingConfig == null || loggingConfig.trim().isEmpty()) {
            return null;
        }
        AnsiLog.debug("arthas logging file: " + loggingConfig);
        File configFile = new File(loggingConfig);
        if (!configFile.isFile()) {
            AnsiLog.error("can not find arthas logging config: " + loggingConfig);
            return null;
        }

        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.reset();

            String fileName = env.getProperty(FILE_NAME_PROPERTY);
            if (fileName != null) {
                loggerContext.putProperty(ARTHAS_LOG_FILE, fileName);
            }
            String filePath = env.getProperty(FILE_PATH_PROPERTY);
            if (filePath != null) {
                loggerContext.putProperty(ARTHAS_LOG_PATH, filePath);
            }

            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerContext);
            configurator.doConfigure(configFile.toURI().toURL()); // load logback xml file

            // 查找名为 ARTHAS 的 RollingFileAppender，记录实际日志文件路径
            Iterator<Appender<ILoggingEvent>> appenders = loggerContext.getLogger("root").iteratorForAppenders();

            while (appenders.hasNext()) {
                Appender<ILoggingEvent> appender = appenders.next();
                if (appender instanceof RollingFileAppender) {
                    RollingFileAppender fileAppender = (RollingFileAppender) appender;
                    if ("ARTHAS".equalsIgnoreCase(fileAppender.getName())) {
                        logFile = new File(fileAppender.getFile()).getCanonicalPath();
                    }
                }
            }

            return loggerContext;
        } catch (Throwable e) {
            AnsiLog.error("try to load arthas logging config file error: " + configFile, e);
        }
        return null;
    }

    /**
     * @return 当前 arthas 日志文件的绝对路径；未初始化时默认 {@code "arthas.log"}
     */
    public static String loggingFile() {
        if (logFile == null || logFile.trim().isEmpty()) {
            return "arthas.log";
        }
        return logFile;
    }

    /**
     * @return 日志文件所在目录；无法解析时返回当前工作目录绝对路径
     */
    public static String loggingDir() {
        if (logFile != null && !logFile.isEmpty()) {
            String parent = new File(logFile).getParent();
            if (parent != null) {
                return parent;
            }
        }
        return new File("").getAbsolutePath();
    }

    /**
     * 返回 Arthas 临时缓存目录（与 logs 目录同级下的 {@code arthas-cache}）。
     *
     * @return 缓存目录绝对路径（不存在时会尝试 mkdirs）
     */
    public static String cacheDir() {
        File logsDir = new File(loggingDir()).getParentFile();
        if (logsDir.exists()) {
            File arthasCacheDir = new File(logsDir, "arthas-cache");
            arthasCacheDir.mkdirs();
            return arthasCacheDir.getAbsolutePath();
        } else {
            File arthasCacheDir = new File("arthas-cache");
            arthasCacheDir.mkdirs();
            return arthasCacheDir.getAbsolutePath();
        }
    }

    /** @return 命令结果专用 Logger（category {@code result}） */
    public static Logger getResultLogger() {
        return LoggerFactory.getLogger("result");
    }
}
