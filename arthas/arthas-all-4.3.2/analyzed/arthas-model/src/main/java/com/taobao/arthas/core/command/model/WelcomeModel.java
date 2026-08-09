package com.taobao.arthas.core.command.model;

/**
 * 客户端连接 Arthas 服务端后返回的欢迎信息模型。
 * <p>
 * 携带进程 PID、启动时间、版本号、文档链接及目标 JVM 主类名等元数据，
 * 对应命令结果类型 {@code welcome}。
 *
 * @author gongdewei 2020/4/20
 */
public class WelcomeModel extends ResultModel {

    /** 目标 JVM 进程 ID。 */
    private String pid;
    /** Agent 启动或会话建立时间字符串。 */
    private String time;
    /** 当前 Arthas 版本号。 */
    private String version;
    /** 官方文档/Wiki 链接。 */
    private String wiki;
    /** 教程或快速入门链接。 */
    private String tutorials;
    /** 目标 JVM 主类全限定名。 */
    private String mainClass;

    public WelcomeModel() {
    }

    /** 固定返回 {@code welcome}，标识欢迎消息类型。 */
    @Override
    public String getType() {
        return "welcome";
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getWiki() {
        return wiki;
    }

    public void setWiki(String wiki) {
        this.wiki = wiki;
    }

    public String getTutorials() {
        return tutorials;
    }

    public void setTutorials(String tutorials) {
        this.tutorials = tutorials;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }
}
