package com.taobao.arthas.compiler;

/*-
 * #%L
 * compiler
 * %%
 * Copyright (C) 2017 - 2018 SkaLogs
 * %%
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
 * #L%
 */

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.*;

/**
 * 动态编译失败时抛出的运行时异常，附带 javac 产生的诊断信息列表。
 */
public class DynamicCompilerException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    /** javac 编译过程中收集的诊断条目（含行号与错误消息） */
    private List<Diagnostic<? extends JavaFileObject>> diagnostics;

    /**
     * @param message 异常摘要信息
     * @param diagnostics 编译诊断列表
     */
    public DynamicCompilerException(String message, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        super(message);
        this.diagnostics = diagnostics;
    }

    /**
     * @param cause 原始异常
     * @param diagnostics 编译诊断列表
     */
    public DynamicCompilerException(Throwable cause, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        super(cause);
        this.diagnostics = diagnostics;
    }

    /** 将诊断信息转换为行号与消息的结构化列表 */
    private List<Map<String, Object>> getErrorList() {
        List<Map<String, Object>> messages = new ArrayList<Map<String, Object>>();
        if (diagnostics != null) {
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
                Map<String, Object> message = new HashMap<String, Object>(2);
                message.put("line", diagnostic.getLineNumber());
                message.put("message", diagnostic.getMessage(Locale.US));
                messages.add(message);
            }

        }
        return messages;
    }

    /** 将结构化错误列表格式化为可读的多行字符串 */
    private String getErrors() {
        StringBuilder errors = new StringBuilder();

        for (Map<String, Object> message : getErrorList()) {
            for (Map.Entry<String, Object> entry : message.entrySet()) {
                Object value = entry.getValue();
                if (value != null && !value.toString().isEmpty()) {
                    errors.append(entry.getKey());
                    errors.append(": ");
                    errors.append(value);
                }
                errors.append(" , ");
            }

            errors.append("\n");
        }

        return errors.toString();

    }

    /** 在原始消息后追加格式化的编译错误详情 */
    @Override
    public String getMessage() {
        return super.getMessage() + "\n" + getErrors();
    }

}
