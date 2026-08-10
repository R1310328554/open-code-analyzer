/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.theme.beans;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.forms.login.MessageType;

/**
 * 按表单字段聚合消息的 Bean。
 * <p>以 {@code messagesPerField} 键存入 FreeMarker 上下文，支持按字段查询错误/警告及条件输出 CSS 类名。</p>
 *
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class MessagesPerFieldBean {

    private Map<String, MessageBean> messagesPerField = new HashMap<String, MessageBean>();

    /** 向指定字段追加消息；field 为 null 时使用 global。 */
    public void addMessage(String field, String messageText, MessageType messageType) {
        if (messageText == null || messageText.trim().isEmpty())
            return;
        if (field == null)
            field = "global";

        MessageBean fm = messagesPerField.get(field);
        if (fm == null) {
            messagesPerField.put(field, new MessageBean(messageText, messageType));
        } else {
            fm.appendSummaryLine(messageText);
        }
    }

    /**
     * 检查指定字段是否已有消息。
     *
     * @param field 字段名
     * @return 存在则 true
     */
    public boolean exists(String field) {
        return messagesPerField.containsKey(field);
    }

    /**
     * 检查给定字段中是否存在错误类型消息。
     *
     * @param fields 待检查的字段名
     * @return 任一字段有错误则 true
     */
    public boolean existsError(String... fields) {
        for (String field : fields) {
            if (exists(field) && messagesPerField.get(field).isError())
                return true;
        }
        return false;
    }

    /**
     * 返回给定字段中首个错误消息的文本。
     *
     * @param fields 待检查的字段名
     * @return 消息文本，无错误时返回空字符串
     */
    public String getFirstError(String... fields) {
        for (String field : fields) {
            if (existsError(field)) {
                return get(field);
            }
        }
        return "";
    }

    /**
     * 获取指定字段的消息摘要。
     *
     * @param fieldName 字段名
     * @return 消息文本，不存在时返回空字符串
     */
    public String get(String fieldName) {
        MessageBean mb = messagesPerField.get(fieldName);
        if (mb != null) {
            return mb.getSummary();
        } else {
            return "";
        }
    }

    /**
     * 若字段存在消息则返回给定文本（用于条件输出 CSS 类等）。
     *
     * @param fieldName 待检查字段名
     * @param text 存在消息时要输出的文本
     * @return 有消息时返回 text，否则空字符串
     */
    public String printIfExists(String fieldName, String text) {
        if (exists(fieldName))
            return text;
        else
            return "";
    }

}
