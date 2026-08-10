/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.forms.login;

/**
 * 登录表单消息的展示类型枚举。
 * <p>由 {@link LoginFormsProvider#setMessage(MessageType, String, Object...)} 指定， 影响主题模板中的 CSS 样式与图标。</p>
 *
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public enum MessageType {

    SUCCESS, WARNING, INFO, ERROR

}
