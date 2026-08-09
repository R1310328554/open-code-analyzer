package com.taobao.arthas.core.shell;

import com.taobao.arthas.core.util.ArthasBanner;

import java.lang.instrument.Instrumentation;

/**
 * {@link ShellServer} 的配置项：欢迎语、会话超时、连接超时与 JVM 信息。
 * <p>
 * 提供流式 setter；构造时使用 DEFAULT_* 常量作为默认值。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ShellServerOptions {

    /** 默认会话清理（Reaper）扫描间隔：60 秒 */

    public static final long DEFAULT_REAPER_INTERVAL = 60 * 1000; // 60 seconds

    /** 默认 Shell 会话空闲超时：3 小时无访问则过期 */

    public static final long DEFAULT_SESSION_TIMEOUT = 3 * 60 * 60 * 1000; // 3 hours

    /** 终端服务器等待客户端完成连接握手的最长时间：6 秒 */

    public static final long DEFAULT_CONNECTION_TIMEOUT = 6000; // 6 seconds

    public static final String DEFAULT_WELCOME_MESSAGE = ArthasBanner.welcome();

    public static final String DEFAULT_INPUTRC = "com/taobao/arthas/core/shell/term/readline/inputrc";

    private String welcomeMessage;
    private long sessionTimeout;
    private long reaperInterval;
    private long connectionTimeout;
    private long pid;
    private Instrumentation instrumentation;

    /** 使用欢迎语、会话/连接超时与 Reaper 间隔的默认值初始化 */
    public ShellServerOptions() {
        welcomeMessage = DEFAULT_WELCOME_MESSAGE;
        sessionTimeout = DEFAULT_SESSION_TIMEOUT;
        connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        reaperInterval = DEFAULT_REAPER_INTERVAL;
    }

    /**
     * @return the shell welcome message
     */
    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    /**
     * Set the shell welcome message, i.e the message displayed in the user console when he connects to the shell.
     *
     * @param welcomeMessage the welcome message
     * @return a reference to this, so the API can be used fluently
     */
    public ShellServerOptions setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
        return this;
    }

    /**
     * @return the session timeout
     */
    public long getSessionTimeout() {
        return sessionTimeout;
    }

    /**
     * Set the session timeout.
     *
     * @param sessionTimeout the new session timeout
     * @return a reference to this, so the API can be used fluently
     */
    public ShellServerOptions setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    /**
     * @return the reaper interval
     */
    public long getReaperInterval() {
        return reaperInterval;
    }

    /**
     * 设置会话驱逐扫描间隔（毫秒）。
     *
     * @param reaperInterval the new repeat interval
     * @return a reference to this, so the API can be used fluently
     */
    public ShellServerOptions setReaperInterval(long reaperInterval) {
        this.reaperInterval = reaperInterval;
        return this;
    }

    /** 设置目标 JVM 进程 PID，供欢迎语与诊断命令展示 */
    public ShellServerOptions setPid(long pid) {
        this.pid = pid;
        return this;
    }

    /** 注入 {@link Instrumentation}，供 Shell 内增强类命令使用 */
    public ShellServerOptions setInstrumentation(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
        return this;
    }

    public long getPid() {
        return pid;
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}
