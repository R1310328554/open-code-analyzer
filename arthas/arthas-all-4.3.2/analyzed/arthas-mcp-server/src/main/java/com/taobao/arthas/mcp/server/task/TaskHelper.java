/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema.TaskStatus;

/**
 * Task 状态判定与状态机转移校验的工具类。
 *
 * @author Yeaury
 */
public final class TaskHelper {

    private TaskHelper() {}

    /** 判断是否为终态：COMPLETED、FAILED 或 CANCELLED。 */
    public static boolean isTerminal(TaskStatus status) {
        if (status == null) {
            return false;
        }
        return status == TaskStatus.COMPLETED
            || status == TaskStatus.FAILED
            || status == TaskStatus.CANCELLED;
    }

    /**
     * 校验从 {@code from} 到 {@code to} 的状态转移是否合法。
     * <p>
     * 终态不可再转移；WORKING 可转至任意状态；
     * INPUT_REQUIRED 仅可回到 WORKING 或进入终态。
     */
    public static boolean isValidTransition(TaskStatus from, TaskStatus to) {
        if (from == null || to == null) {
            return false;
        }
        if (isTerminal(from)) {
            return false;
        }
        if (from == TaskStatus.WORKING) {
            return true;
        }
        if (from == TaskStatus.INPUT_REQUIRED) {
            return to == TaskStatus.WORKING || isTerminal(to);
        }
        return false;
    }
}
