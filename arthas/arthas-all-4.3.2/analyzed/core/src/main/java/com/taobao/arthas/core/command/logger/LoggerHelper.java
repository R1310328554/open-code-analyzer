package com.taobao.arthas.core.command.logger;

/**
 * logger 命令 JSON 输出的字段名常量接口。
 * <p>
 * 各 Log4j/Logback/Log4j2 Helper 组装 Map 时统一使用这些键，
 * 便于 {@link LoggerCommand} 与前端/Web 控制台解析结构化结果。
 *
 * @author hengyunabc 2019-09-06
 */
public interface LoggerHelper {
    /** logger/appender 实现类的 Class 对象键名 */
    public static final String clazz = "class";
    /** 所属 ClassLoader 描述字符串 */
    public static final String classLoader = "classLoader";
    /** ClassLoader 哈希，与 sc -d 输出一致 */
    public static final String classLoaderHash = "classLoaderHash";
    /** 类所在 jar/目录 URL */
    public static final String codeSource = "codeSource";

    // logger 级别相关
    /** 显式配置的 level */
    public static final String level = "level";
    /** 继承父 logger 后的有效级别 */
    public static final String effectiveLevel = "effectiveLevel";

    // 仅 Log4j2：关联的 Configuration 对象
    public static final String config = "config";

    /** 是否向父 logger 传递日志（boolean） */
    public static final String additivity = "additivity";
    /** appender 信息列表 */
    public static final String appenders = "appenders";

    // appender 字段
    /** logger 或 appender 名称 */
    public static final String name = "name";
    /** 文件 appender 的路径 */
    public static final String file = "file";
    /** 异步 appender 队列满时是否阻塞 */
    public static final String blocking = "blocking";
    /** 异步 appender 引用的子 appender 名列表 */
    public static final String appenderRef = "appenderRef";
    /** 控制台 appender 输出目标（System.out/err） */
    public static final String target = "target";

}
