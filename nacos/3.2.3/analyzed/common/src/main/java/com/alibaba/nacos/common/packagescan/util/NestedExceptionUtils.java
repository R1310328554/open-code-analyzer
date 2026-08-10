/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.packagescan.util;

/**
 * Copy from https://github.com/spring-projects/spring-framework.git, with less modifications
 * 嵌套异常工具类：为无法共享基类的异常类型提供根因解析与消息拼接，供 {@link NestedIoException} 等使用。
 * Helper class for implementing exception classes which are capable of
 * holding nested exceptions. Necessary because we can't share a base
 * class among different exception types.
 *
 * <p>Mainly for use within the framework.
 *
 * @author Juergen Hoeller
 * @see NestedIoException
 * @since 2.0
 */
public abstract class NestedExceptionUtils {

    /**
     * Build a message for the given base message and root cause.
     *
     * @param message the base message
     * @param cause   the root cause
     * @return the full exception message
      * <p>嵌套异常工具；详见类级说明。</p>
     */

    /** 拼接基础消息与 nested exception 根因描述 */
    public static String buildMessage(String message, Throwable cause) {
        if (cause == null) {
            return message;
        }
        StringBuilder sb = new StringBuilder(64);
        if (message != null) {
            sb.append(message).append("; ");
        }
        sb.append("nested exception is ").append(cause);
        return sb.toString();
    }

    /**
     * Retrieve the innermost cause of the given exception, if any.
     *
     * @param original the original exception to introspect
     * @return the innermost exception, or {@code null} if none
     * @since 4.3.9
      * <p>嵌套异常工具；详见类级说明。</p>
     */

    /** 沿 cause 链追溯最内层根因，无嵌套时返回 null */
    public static Throwable getRootCause(Throwable original) {
        if (original == null) {
            return null;
        }
        Throwable rootCause = null;
        Throwable cause = original.getCause();
        while (cause != null && cause != rootCause) {
            rootCause = cause;
            cause = cause.getCause();
        }
        return rootCause;
    }

    /**
     * Retrieve the most specific cause of the given exception, that is,
     * either the innermost cause (root cause) or the exception itself.
     *
     * <p>Differs from {@link #getRootCause} in that it falls back
     * to the original exception if there is no root cause.
     *
     * @param original the original exception to introspect
     * @return the most specific cause (never {@code null})
     * @since 4.3.9
      * <p>嵌套异常工具；详见类级说明。</p>
     */
    /** 返回根因；若无嵌套则返回原始异常本身 */
    public static Throwable getMostSpecificCause(Throwable original) {
        Throwable rootCause = getRootCause(original);
        return (rootCause != null ? rootCause : original);
    }

}
