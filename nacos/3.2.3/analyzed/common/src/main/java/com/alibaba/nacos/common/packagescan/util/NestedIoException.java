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

import java.io.IOException;


/**
 * Copy from https://github.com/spring-projects/spring-framework.git, with less modifications
 * 支持嵌套根因的 {@link IOException} 子类：在 Java 6 之前标准 IO 异常无根因暴露，本类通过 {@link NestedExceptionUtils} 增强 {@link #getMessage()}。
 * Subclass of {@link IOException} that properly handles a root cause,
 * exposing the root cause just like NestedChecked/RuntimeException does.
 *
 * <p>Proper root cause handling has not been added to standard IOException before
 * Java 6, which is why we need to do it ourselves for Java 5 compatibility purposes.
 *
 * <p>The similarity between this class and the NestedChecked/RuntimeException
 * class is unavoidable, as this class needs to derive from IOException.
 *
 * @author Juergen Hoeller
 * @see #getMessage
 * @since 2.0
 */
@SuppressWarnings("serial")
public class NestedIoException extends IOException {

    static {
        // 静态块预加载 NestedExceptionUtils，避免 OSGi 下 getMessage 类加载死锁
        // Eagerly load the NestedExceptionUtils class to avoid classloader deadlock
        // issues on OSGi when calling getMessage(). Reported by Don Brown; SPR-5607.
        NestedExceptionUtils.class.getName();
    }

    /**
     * Construct a {@code NestedIOException} with the specified detail message.
     *
     * @param msg the detail message
      * <p>嵌套 IO 异常；详见类级说明。</p>
     */
    public NestedIoException(String msg) {
        super(msg);
    }

    /**
     * Construct a {@code NestedIOException} with the specified detail message
     * and nested exception.
     *
     * @param msg   the detail message
     * @param cause the nested exception
      * <p>嵌套 IO 异常；详见类级说明。</p>
     */
    public NestedIoException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Return the detail message, including the message from the nested exception
     * if there is one.
      * <p>嵌套 IO 异常；详见类级说明。</p>
     */
    /** 返回含 nested exception 详情的完整消息 */
    @Override
    public String getMessage() {
        return NestedExceptionUtils.buildMessage(super.getMessage(), getCause());
    }

}
