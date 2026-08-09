package com.taobao.arthas.core.command.logger;

import java.lang.reflect.Field;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.ILoggerFactory;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.pattern.PatternLayoutBase;

/**
 * Logback 日志框架辅助类：通过 SLF4J 获取 {@link LoggerContext} 并读写 logger 级别。
 * <p>
 * 静态初始化确认 logback-classic 与 Arthas 同 ClassLoader 加载；
 * 预反射 PatternLayoutBase、ThrowableProxyConverter 字段供后续诊断扩展。
 *
 * @author hengyunabc 2019-09-06
 */
public class LogbackHelper {

    /** 当前环境是否可用 Logback */
    private static boolean Logback = false;
    /** 预留反射字段：布局头节点、异常栈长度选项 */
    private static Field headField, lengthOptionField;
    /** 缓存的 SLF4J LoggerFactory，须为 LoggerContext 实例 */
    private static ILoggerFactory loggerFactoryInstance;

    static {
        try {
            Class<?> loggerClass = LogbackHelper.class.getClassLoader().loadClass("ch.qos.logback.classic.Logger");
            // 这里可能会加载到应用中依赖的logback，因此需要判断classloader
            if (loggerClass.getClassLoader().equals(LogbackHelper.class.getClassLoader())) {
                ILoggerFactory loggerFactory = org.slf4j.LoggerFactory.getILoggerFactory();

                if (loggerFactory instanceof LoggerContext) {
                    loggerFactoryInstance = loggerFactory;

                    headField = PatternLayoutBase.class.getDeclaredField("head");
                    headField.setAccessible(true);

                    lengthOptionField = ThrowableProxyConverter.class.getDeclaredField("lengthOption");
                    lengthOptionField.setAccessible(true);

                    Logback = true;
                }
            }
        } catch (Throwable t) {
            // ignore
        }
    }

    /** 在 LoggerContext 中查找 logger 并设置级别，未找到返回 false */
    public static Boolean updateLevel(String name, String level) {
        if (Logback) {
            try {
                Level l = Level.toLevel(level, Level.ERROR);
                LoggerContext loggerContext = (LoggerContext) loggerFactoryInstance;

                Logger logger = loggerContext.exists(name);
                if (logger != null) {
                    logger.setLevel(l);
                    return true;
                }
            } catch (Throwable t) {
                // ignore
            }
            return false;
        }
        return null;
    }

    /** 查询单个或全部 logback logger，LinkedHashMap 保持遍历顺序 */
    public static Map<String, Map<String, Object>> getLoggers(String name, boolean includeNoAppender) {
        Map<String, Map<String, Object>> loggerInfoMap = new LinkedHashMap<String, Map<String, Object>>();

        if (Logback) {
            LoggerContext loggerContext = (LoggerContext) loggerFactoryInstance;
            if (name != null && !name.trim().isEmpty()) {
                Logger logger = loggerContext.exists(name);
                if (logger != null) {
                    loggerInfoMap.put(name, doGetLoggerInfo(logger));
                }
            } else {
                // 获取所有logger时，如果没有appender则忽略
                List<Logger> loggers = loggerContext.getLoggerList();
                for (Logger logger : loggers) {
                    Map<String, Object> info = doGetLoggerInfo(logger);

                    if (!includeNoAppender) {
                        List<?> appenders = (List<?>) info.get(LoggerHelper.appenders);
                        if (appenders != null && !appenders.isEmpty()) {
                            loggerInfoMap.put(logger.getName(), info);
                        }
                    } else {
                        loggerInfoMap.put(logger.getName(), info);
                    }

                }
            }
        }

        return loggerInfoMap;
    }

    /** 提取 logback Logger 的名称、级别、追加器及类加载来源 */
    private static Map<String, Object> doGetLoggerInfo(Logger logger) {
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        info.put(LoggerHelper.name, logger.getName());
        info.put(LoggerHelper.clazz, logger.getClass());
        CodeSource codeSource = logger.getClass().getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            info.put(LoggerHelper.codeSource, codeSource.getLocation());
        }
        info.put(LoggerHelper.additivity, logger.isAdditive());

        Level level = logger.getLevel(), effectiveLevel = logger.getEffectiveLevel();
        if (level != null) {
            info.put(LoggerHelper.level, level.toString());
        }
        if (effectiveLevel != null) {
            info.put(LoggerHelper.effectiveLevel, effectiveLevel.toString());
        }

        List<Map<String, Object>> result = doGetLoggerAppenders(logger.iteratorForAppenders());
        info.put(LoggerHelper.appenders, result);
        return info;
    }

    /** 遍历 appender 迭代器，AsyncAppender 展开 ref 列表并记录 blocking 状态 */
    @SuppressWarnings("rawtypes")
    private static List<Map<String, Object>> doGetLoggerAppenders(Iterator<Appender<ILoggingEvent>> appenders) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        while (appenders.hasNext()) {
            Map<String, Object> info = new LinkedHashMap<String, Object>();
            Appender<ILoggingEvent> appender = appenders.next();
            info.put(LoggerHelper.name, appender.getName());
            info.put(LoggerHelper.clazz, appender.getClass());
            if (appender instanceof FileAppender) {
                info.put(LoggerHelper.file, ((FileAppender) appender).getFile());
            } else if (appender instanceof AsyncAppender) {
                AsyncAppender aa = (AsyncAppender) appender;
                Iterator<Appender<ILoggingEvent>> iter = aa.iteratorForAppenders();
                List<Map<String, Object>> asyncs = doGetLoggerAppenders(iter);

                // 异步appender所 ref的 appender，参考： https://logback.qos.ch/manual/appenders.html
                List<String> appenderRef = new ArrayList<String>();
                for (Map<String, Object> a : asyncs) {
                    appenderRef.add((String) a.get(LoggerHelper.name));
                    result.add(a);
                }
                info.put(LoggerHelper.appenderRef, appenderRef);
                info.put(LoggerHelper.blocking, !aa.isNeverBlock());
            } else if (appender instanceof ConsoleAppender) {
                info.put(LoggerHelper.target, ((ConsoleAppender) appender).getTarget());
            }
            result.add(info);
        }

        return result;
    }
}
