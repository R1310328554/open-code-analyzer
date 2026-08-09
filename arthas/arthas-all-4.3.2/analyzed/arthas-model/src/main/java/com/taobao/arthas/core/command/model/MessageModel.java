package com.taobao.arthas.core.command.model;

/**
 * 通用文本消息结果模型，用于向客户端推送简单字符串消息。
 *
 * @author gongdewei 2020/4/2
 */
public class MessageModel extends ResultModel {
    /** 消息正文内容。 */
    private String message;

    public MessageModel() {
    }

    public MessageModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String getType() {
        return "message";
    }
}
