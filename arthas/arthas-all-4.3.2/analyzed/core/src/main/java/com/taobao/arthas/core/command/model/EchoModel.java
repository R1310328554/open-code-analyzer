package com.taobao.arthas.core.command.model;

/**
 * {@code echo} 命令的结果模型，原样回显用户输入或脚本变量展开后的文本。
 * <p>
 * 类型标识为 {@code "echo"}，内容字段不参与结构化解析，仅作终端/隧道透传。
 *
 * @author gongdewei 2020/5/11
 */
public class EchoModel extends ResultModel {

    /** 回显的正文内容 */
    private String content;

    public EchoModel() {
    }

    public EchoModel(String content) {
        this.content = content;
    }

    @Override
    public String getType() {
        return "echo";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
