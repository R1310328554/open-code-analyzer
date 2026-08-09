/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block;

/**
 * Sentinel 阻断异常的抽象基类，表示因流控、熔断或系统保护而被拦截。
 *
 * @author youji.zj
 */
public abstract class BlockException extends Exception {

    private static final int MAX_SEARCH_DEPTH = 10;

    public static final String BLOCK_EXCEPTION_FLAG = "SentinelBlockException";
    public static final String BLOCK_EXCEPTION_MSG_PREFIX = "SentinelBlockException: ";

    /**
     * <p>无堆栈的占位 {@link RuntimeException}，消息为 {@link #BLOCK_EXCEPTION_FLAG}，
     * 用于快速抛出阻断信号。
     * </p>
     * <p>
     * 请使用 {@link #isBlockException(Throwable)} 判断异常是否为 Sentinel 阻断异常。
     * </p>
     */
    public static RuntimeException THROW_OUT_EXCEPTION = new RuntimeException(BLOCK_EXCEPTION_FLAG);

    public static StackTraceElement[] sentinelStackTrace = new StackTraceElement[] {
        new StackTraceElement(BlockException.class.getName(), "block", "BlockException", 0)
    };

    static {
        THROW_OUT_EXCEPTION.setStackTrace(sentinelStackTrace);
    }

    protected AbstractRule rule;
    private String ruleLimitApp;

    public BlockException(String ruleLimitApp) {
        super();
        this.ruleLimitApp = ruleLimitApp;
    }

    public BlockException(String ruleLimitApp, AbstractRule rule) {
        super();
        this.ruleLimitApp = ruleLimitApp;
        this.rule = rule;
    }

    public BlockException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlockException(String ruleLimitApp, String message) {
        super(message);
        this.ruleLimitApp = ruleLimitApp;
    }

    public BlockException(String ruleLimitApp, String message, AbstractRule rule) {
        super(message);
        this.ruleLimitApp = ruleLimitApp;
        this.rule = rule;
    }

    /** 不填充堆栈，降低阻断异常的开销。 */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    public String getRuleLimitApp() {
        return ruleLimitApp;
    }

    public void setRuleLimitApp(String ruleLimitApp) {
        this.ruleLimitApp = ruleLimitApp;
    }

    /** 转换为带 Sentinel 前缀的运行时异常。 */
    public RuntimeException toRuntimeException() {
        RuntimeException t = new RuntimeException(BLOCK_EXCEPTION_MSG_PREFIX + getClass().getSimpleName());
        t.setStackTrace(sentinelStackTrace);
        return t;
    }

    /**
     * 判断异常是否为 Sentinel 阻断异常。满足以下任一条件即为阻断异常：
     * <ul>
     * <li>异常或其（子）cause 为 {@link BlockException}；或</li>
     * <li>异常或其子 cause 的消息以 {@link #BLOCK_EXCEPTION_FLAG} 为前缀。</li>
     * </ul>
     *
     * @param t 待检查的异常
     * @return 若为 Sentinel 阻断异常则返回 true
     */
    public static boolean isBlockException(Throwable t) {
        if (null == t) {
            return false;
        }

        int counter = 0;
        Throwable cause = t;
        while (cause != null && counter++ < MAX_SEARCH_DEPTH) {
            if (cause instanceof BlockException) {
                return true;
            }
            if (cause.getMessage() != null && cause.getMessage().startsWith(BLOCK_EXCEPTION_FLAG)) {
                return true;
            }
            cause = cause.getCause();
        }

        return false;
    }

    /** 获取触发阻断的规则（若有）。 */
    public AbstractRule getRule() {
        return rule;
    }
}
