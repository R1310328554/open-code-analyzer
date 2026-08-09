package com.taobao.arthas.core.command.model;

/**
 * HTTP 风格的状态码结果模型，用于向客户端返回操作成功或失败及说明消息。
 */
public class StatusModel extends ResultModel {

    /** HTTP 或业务状态码。 */
    private int statusCode;
    /** 状态附加说明消息。 */
    private String message;

    public StatusModel(int statusCode) {
        this.statusCode = statusCode;
    }

    public StatusModel(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }


    public String getMessage() {
        return message;
    }

    @Override
    public String getType() {
        return "status";
    }

}
