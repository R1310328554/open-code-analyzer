package org.keycloak.testframework.events;

/**
 * 接收 {@link SysLogServer} 解析出的 syslog 条目的回调接口。
 */
public interface SysLogListener {

    /**
     * 收到新 syslog 条目时调用。
     *
     * @param sysLog 解析后的日志条目
     */
    void onLog(SysLog sysLog);

}
