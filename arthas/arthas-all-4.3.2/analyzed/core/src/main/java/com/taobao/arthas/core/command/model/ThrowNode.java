package com.taobao.arthas.core.command.model;

/**
 * trace 调用树中的异常节点：记录某次方法调用链上抛出的异常摘要，类型为 {@code "throw"}。
 * <p>
 * 由 {@link TraceTree#end(Throwable, int)} 挂到当前 {@link MethodNode} 下；
 * 仅保存异常类名、message 与抛出点行号，不含完整堆栈（完整栈见 stack 命令）。
 *
 * @author gongdewei 2020/7/21
 */
public class ThrowNode extends TraceNode {
    /** 异常全限定类名 */
    private String exception;
    /** {@link Throwable#getMessage()}，可能为 null */
    private String message;
    /** 抛出位置源码行号（字节码行号映射） */
    private int lineNumber;

    public ThrowNode() {
        super("throw");
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
