package com.taobao.arthas.core.shell.term.impl.http.api;

/**
 * HTTP API 业务异常，携带可读错误信息返回客户端。
 * <p>
 * 由 {@code HttpApiHandler} 捕获并序列化为 JSON 错误响应。
 *
 * @author gongdewei 2020-03-19
 */
public class ApiException extends Exception {

    /** @param message 返回给客户端的错误描述 */
    public ApiException(String message) {
        super(message);
    }

    /** @param message 错误描述；@param cause 根因异常 */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
