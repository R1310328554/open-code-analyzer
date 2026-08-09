package com.taobao.arthas.core.command.model;

/**
 * base64 命令的结构化结果模型，携带编解码后的文本内容。
 * <p>
 * type 固定为 "base64"，供 JSON/Web 控制台识别结果类型。
 *
 * @author hengyunabc 2021-01-05
 */
public class Base64Model extends ResultModel {

    /** Base64 编解码结果字符串 */
    private String content;

    public Base64Model() {
    }

    public Base64Model(String content) {
        this.content = content;
    }

    /** 返回结果类型标识 "base64" */
    @Override
    public String getType() {
        return "base64";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
