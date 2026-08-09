package com.taobao.arthas.core.command.model;

/**
 * {@code version} 命令的结构化结果：Arthas 客户端/Agent 版本号字符串。
 * <p>
 * 由 {@link com.taobao.arthas.core.command.basic1000.VersionCommand} 填充，
 * Web Console 与 Telnet 均可据此展示当前工具版本。
 */
public class VersionModel extends ResultModel {

    /** Arthas 版本号，如 4.3.2 */
    private String version;

    /** 结果类型标识，固定为 {@code version} */
    @Override
    public String getType() {
        return "version";
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
