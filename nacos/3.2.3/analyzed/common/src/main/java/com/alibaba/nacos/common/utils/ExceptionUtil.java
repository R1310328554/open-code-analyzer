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

package com.alibaba.nacos.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 异常处理工具：拼接 cause 链消息、获取根 cause、将堆栈转为 UTF-8 字符串；
 * {@link #NONE_EXCEPTION} 表示「无异常」占位常量。
 * Common methods for exception.
 *
 * @author nkorange
 * @since 1.2.0
 */
public class ExceptionUtil {
    
    private ExceptionUtil() {
    }
    
    /** 表示无异常发生的占位 RuntimeException（空消息） */

    public static final Exception NONE_EXCEPTION = new RuntimeException("");
    
    /** 沿 cause 链拼接 {@code caused: message;} 直至消息为空 */
    public static String getAllExceptionMsg(Throwable e) {
        Throwable cause = e;
        StringBuilder strBuilder = new StringBuilder();
        
        while (cause != null && !StringUtils.isEmpty(cause.getMessage())) {
            strBuilder.append("caused: ").append(cause.getMessage()).append(';');
            cause = cause.getCause();
        }
        
        return strBuilder.toString();
    }
    
    /** 返回最内层 cause，无 cause 时返回 t 自身 */
    public static Throwable getCause(final Throwable t) {
        final Throwable cause = t.getCause();
        if (Objects.isNull(cause)) {
            return t;
        }
        return cause;
    }
    
    /** 将完整堆栈打印到内存并以 UTF-8 字符串返回；t 为 null 时返回空串 */
    public static String getStackTrace(final Throwable t) {
        if (t == null) {
            return "";
        }
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            final PrintStream ps = new PrintStream(out, false, StandardCharsets.UTF_8.name());
            t.printStackTrace(ps);
            ps.flush();
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            // UTF-8 在 JVM 中始终可用，不应到达此分支
            throw new IllegalStateException(e);
        }
    }
}
