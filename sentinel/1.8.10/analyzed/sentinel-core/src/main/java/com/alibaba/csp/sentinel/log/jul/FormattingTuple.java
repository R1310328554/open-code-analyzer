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

// Copyright notice: This code was copied from SLF4J which licensed under the MIT License.
package com.alibaba.csp.sentinel.log.jul;

/**
 * 保存 {@link MessageFormatter} 格式化后的结果（消息、参数数组、异常）。
 *
 * @author Joern Huxhorn
 */
public class FormattingTuple {

    /** 表示空消息的占位实例。 */
    static public FormattingTuple NULL = new FormattingTuple(null);

    private String message;
    private Throwable throwable;
    private Object[] argArray;

    public FormattingTuple(String message) {
        this(message, null, null);
    }

    public FormattingTuple(String message, Object[] argArray, Throwable throwable) {
        this.message = message;
        this.throwable = throwable;
        this.argArray = argArray;
    }

    /** 获取格式化后的消息文本。 */
    public String getMessage() {
        return message;
    }

    /** 获取格式化时使用的参数数组。 */
    public Object[] getArgArray() {
        return argArray;
    }

    /** 获取关联的异常（若有）。 */
    public Throwable getThrowable() {
        return throwable;
    }

}
