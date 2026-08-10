package org.keycloak.testframework.events;

import java.time.Instant;
import java.util.Date;

/**
 * 结构化 syslog 日志条目模型。
 * <p>
 * 解析 Keycloak 测试框架使用的 syslog 行格式（RFC5424 风格头部 + 消息体）。
 */
public class SysLog {

    /** 头部与消息体之间的分隔符（含 BOM）。 */
    private static final String SEPARATOR = " - \uFEFF";

    /** 日志时间戳。 */
    private Date timestamp;
    /** 发送主机名。 */
    private String hostname;
    /** 应用名称。 */
    private String appName;
    /** 日志 category（logger 名）。 */
    private String category;
    /** 日志消息正文。 */
    private String message;

    /** 仅由 {@link #parse(String)} 构造。 */
    private SysLog() {
    }

    /**
     * 从原始 syslog 行解析结构化条目。
     *
     * @param logEntry 单行 syslog 文本
     * @return 解析后的 {@link SysLog}
     */
    public static SysLog parse(String logEntry) {
        int i = logEntry.indexOf(SEPARATOR);

        String[] header = logEntry.substring(0, i).split(" ");

        SysLog sysLog = new SysLog();
        sysLog.timestamp = Date.from(Instant.parse(header[1]));
        sysLog.hostname = header[2];
        sysLog.appName = header[3];
        sysLog.category = header[5];
        sysLog.message = logEntry.substring(i + SEPARATOR.length());
        return sysLog;
    }

    /** @return 日志时间戳 */
    public Date getTimestamp() {
        return timestamp;
    }

    /** @return 主机名 */
    public String getHostname() {
        return hostname;
    }

    /** @return 应用名 */
    public String getAppName() {
        return appName;
    }

    /** @return 日志 category */
    public String getCategory() {
        return category;
    }

    /** @return 消息正文 */
    public String getMessage() {
        return message;
    }
}
