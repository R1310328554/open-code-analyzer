package com.taobao.arthas.core.command.logger;

import java.lang.reflect.Field;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * Log4j2 日志框架辅助类：在目标 ClassLoader 内动态查询 logger 配置并修改级别。
 * <p>
 * 由 {@link LoggerCommand} 通过 ASM 重命名后注入到应用 ClassLoader 执行；
 * 静态块检测当前 ClassLoader 是否真正加载了 log4j-core，避免误判上游依赖。
 *
 * @author hengyunabc 2019-09-20
 */
public class Log4j2Helper {
    /** 当前 ClassLoader 是否可用 Log4j2（与 Arthas 同层加载 log4j-core） */
    private static boolean Log4j2 = false;
    /** 反射读取 LoggerConfig 内部 config 字段，用于输出配置对象信息 */
    private static Field configField = null;

    static {
        try {
            Class<?> loggerClass = Log4j2Helper.class.getClassLoader().loadClass("org.apache.logging.log4j.Logger");
            // 这里可能会加载到其它上游ClassLoader的log4j2，因此需要判断是否当前classloader
            if (loggerClass.getClassLoader().equals(Log4j2Helper.class.getClassLoader())) {
                Log4j2 = true;
            }

            try {
                configField = LoggerConfig.class.getDeclaredField("config");
                configField.setAccessible(true);
            } catch (Throwable e) {
                // ignore
            }
        } catch (Throwable t) {
        }
    }

    /** 判断字符串非 null 且非空 */
    public static boolean hasLength(String str) {
        return (str != null && !str.isEmpty());
    }

    /** 按名称获取 LoggerConfig，空名或 ROOT 统一映射为根 logger */
    private static LoggerConfig getLoggerConfig(String name) {
        if (!hasLength(name) || LoggerConfig.ROOT.equalsIgnoreCase(name)) {
            name = LogManager.ROOT_LOGGER_NAME;
        }
        return getLoggerContext().getConfiguration().getLoggers().get(name);
    }

    /** 获取当前 JVM 绑定的 Log4j2 LoggerContext */
    private static LoggerContext getLoggerContext() {
        return (LoggerContext) LogManager.getContext(false);
    }

    /**
     * 更新指定 logger 的级别；不存在则新建 LoggerConfig。
     * @return true 成功，null 表示未启用 Log4j2，null level 表示级别名非法
     */
    public static Boolean updateLevel(String loggerName, String logLevel) {
        if (Log4j2) {
            Level level = Level.getLevel(logLevel.toUpperCase());
            if (level == null) {
                return null;
            }
            LoggerConfig loggerConfig = getLoggerConfig(loggerName);
            if (loggerConfig == null) {
                loggerConfig = new LoggerConfig(loggerName, level, true);
                getLoggerContext().getConfiguration().addLogger(loggerName, loggerConfig);
            } else {
                loggerConfig.setLevel(level);
            }
            getLoggerContext().updateLoggers();
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * 收集 logger 信息映射，外层 key 为 logger 名，内层为 {@link LoggerHelper} 字段键值。
     * @param name 指定 logger 名，null/空则枚举全部
     * @param includeNoAppender 是否包含无 appender 的 logger
     */
    public static Map<String, Map<String, Object>> getLoggers(String name, boolean includeNoAppender) {
        Map<String, Map<String, Object>> loggerInfoMap = new HashMap<String, Map<String, Object>>();
        if (!Log4j2) {
            return loggerInfoMap;
        }

        Configuration configuration = getLoggerContext().getConfiguration();

        if (name != null && !name.trim().isEmpty()) {
            LoggerConfig loggerConfig = configuration.getLoggerConfig(name);
            if (loggerConfig == null) {
                return loggerInfoMap;
            }
            // 排掉非root时，获取到root的logger config
            if (!name.equalsIgnoreCase(LoggerConfig.ROOT) && isEmpty(loggerConfig.getName())) {
                return loggerInfoMap;
            }
            loggerInfoMap.put(name, doGetLoggerInfo(loggerConfig));
        } else {
            // 获取所有logger时，如果没有appender则忽略
            Map<String, LoggerConfig> loggers = configuration.getLoggers();
            if (loggers != null) {
                for (Entry<String, LoggerConfig> entry : loggers.entrySet()) {
                    LoggerConfig loggerConfig = entry.getValue();
                    if (!includeNoAppender) {
                        if (!loggerConfig.getAppenders().isEmpty()) {
                            loggerInfoMap.put(entry.getKey(), doGetLoggerInfo(entry.getValue()));
                        }
                    } else {
                        loggerInfoMap.put(entry.getKey(), doGetLoggerInfo(entry.getValue()));
                    }
                }
            }
        }

        return loggerInfoMap;
    }

    /** 反射读取 LoggerConfig 私有 config 字段，失败返回 null */
    private static Object getConfigField(LoggerConfig loggerConfig) {
        try {
            if (configField != null) {
                return configField.get(loggerConfig);
            }
        } catch (Throwable e) {
            // ignore
        }
        return null;
    }

    /** 将单个 LoggerConfig 转为 JSON 友好的 Map（级别、追加器、类来源等） */
    private static Map<String, Object> doGetLoggerInfo(LoggerConfig loggerConfig) {
        Map<String, Object> info = new HashMap<String, Object>();

        String name = loggerConfig.getName();
        if (name == null || name.trim().isEmpty()) {
            name = LoggerConfig.ROOT;
        }

        info.put(LoggerHelper.name, name);
        info.put(LoggerHelper.clazz, loggerConfig.getClass());
        CodeSource codeSource = loggerConfig.getClass().getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            info.put(LoggerHelper.codeSource, codeSource.getLocation());
        }
        Object config = getConfigField(loggerConfig);
        if (config != null) {
            info.put(LoggerHelper.config, config);
        }

        info.put(LoggerHelper.additivity, loggerConfig.isAdditive());

        Level level = loggerConfig.getLevel();
        if (level != null) {
            info.put(LoggerHelper.level, level.toString());
        }

        List<Map<String, Object>> result = doGetLoggerAppenders(loggerConfig);
        info.put(LoggerHelper.appenders, result);
        return info;
    }

    /** 提取 logger 绑定的 appender 列表，区分 File/Console/Async 类型 */
    private static List<Map<String, Object>> doGetLoggerAppenders(LoggerConfig loggerConfig) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        Map<String, Appender> appenders = loggerConfig.getAppenders();

        for (Entry<String, Appender> entry : appenders.entrySet()) {
            Map<String, Object> info = new HashMap<String, Object>();
            Appender appender = entry.getValue();
            info.put(LoggerHelper.name, appender.getName());
            info.put(LoggerHelper.clazz, appender.getClass());

            result.add(info);
            if (appender instanceof FileAppender) {
                info.put(LoggerHelper.file, ((FileAppender) appender).getFileName());
            } else if (appender instanceof ConsoleAppender) {
                info.put(LoggerHelper.target, ((ConsoleAppender) appender).getTarget());
            } else if (appender instanceof AsyncAppender) {

                AsyncAppender asyncAppender = ((AsyncAppender) appender);
                String[] appenderRefStrings = asyncAppender.getAppenderRefStrings();

                info.put(LoggerHelper.blocking, asyncAppender.isBlocking());
                info.put(LoggerHelper.appenderRef, Arrays.asList(appenderRefStrings));
            }
        }
        return result;
    }

    /** null 或空串判定 */
    private static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }
}
