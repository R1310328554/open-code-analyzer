package com.taobao.arthas.common;

/**
 * 反射操作失败时抛出的运行时异常，保留原始 {@link Throwable} 供排查。
 */
public class ReflectException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    /** 被包装的根因 */
    private Throwable cause;

    public ReflectException(Throwable cause) {
        super(cause != null ? cause.getClass().getName() + "-->" + cause.getMessage() : "");
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }
}
