package com.taobao.arthas.core.shell.command;

/**
 * 命令执行结束时的退出状态封装。
 * <p>
 * 包含状态码与可选错误消息；{@link #success()} 表示正常结束（码 0），
 * {@link #failure(int, String)} 用于非零失败场景。
 */
public class ExitStatus {

    /** 成功状态单例（statusCode = 0） */
    public static final ExitStatus SUCCESS_STATUS = new ExitStatus(0);

    /**
     * 返回成功状态。
     *
     * @return 共享的 SUCCESS_STATUS 实例
     */
    public static ExitStatus success() {
        return SUCCESS_STATUS;
    }

    /**
     * 构造失败状态。
     *
     * @param statusCode 非零退出码
     * @param message 失败说明，可为 null
     * @return 新的 ExitStatus
     * @throws IllegalArgumentException statusCode 为 0 时抛出
     */
    public static ExitStatus failure(int statusCode, String message) {
        if (statusCode == 0) {
            throw new IllegalArgumentException("failure status code cannot be 0");
        }
        return new ExitStatus(statusCode, message);
    }

    /**
     * 判断给定状态是否表示失败。
     *
     * @param exitStatus 待检查状态，null 视为非失败
     * @return true 当 exitStatus 非 null 且 statusCode ≠ 0
     */
    public static boolean isFailed(ExitStatus exitStatus) {
        return exitStatus != null && exitStatus.getStatusCode() != 0;
    }


    /** 进程退出码 */
    private int statusCode;
    /** 可选的错误描述 */
    private String message;

    private ExitStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    private ExitStatus(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    /** @return 退出状态码 */
    public int getStatusCode() {
        return statusCode;
    }

    /** @return 失败时的说明消息，成功时可能为 null */
    public String getMessage() {
        return message;
    }

}
