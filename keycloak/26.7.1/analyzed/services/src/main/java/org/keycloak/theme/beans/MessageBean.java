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

import org.keycloak.forms.login.MessageType;

/**
 * 登录页单条消息 Bean。
 * <p>封装消息摘要文本与 {@link org.keycloak.forms.login.MessageType} 类型，供模板按 success/warning/error 样式渲染。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class MessageBean {

    private String summary;

    private MessageType type;

    /** 构造带类型标签的消息。 */
    public MessageBean(String message, MessageType type) {
        this.summary = message;
        this.type = type;
    }

    /** 返回消息摘要文本。 */
    public String getSummary() {
        return summary;
    }

    /** 追加一行摘要（以空格分隔）；null 时忽略。 */
    public void appendSummaryLine(String newLine) {
        if (newLine == null)
            return;
        if (summary == null)
            summary = newLine;
        else
            summary = summary + " " + newLine;
    }

    /** 返回小写类型名（success/warning/error）。 */
    public String getType() {
        return this.type.toString().toLowerCase();
    }

    /** 是否为成功类型消息。 */
    public boolean isSuccess() {
        return MessageType.SUCCESS.equals(this.type);
    }

    /** 是否为警告类型消息。 */
    public boolean isWarning() {
        return MessageType.WARNING.equals(this.type);
    }

    /** 是否为错误类型消息。 */
    public boolean isError() {
        return MessageType.ERROR.equals(this.type);
    }

}
