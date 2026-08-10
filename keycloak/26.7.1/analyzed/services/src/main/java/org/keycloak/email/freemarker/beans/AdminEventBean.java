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

package org.keycloak.email.freemarker.beans;

import java.util.Date;

import org.keycloak.events.admin.AdminEvent;

/**
 * 管理事件 FreeMarker Bean：将 {@link AdminEvent} 暴露为模板可读属性。
 * <p>供管理事件通知邮件模板渲染操作类型、客户端、IP 与资源路径等信息。</p>
 *
 * @author <a href="mailto:giriraj.sharma27@gmail.com">Giriraj Sharma</a>
 */
public class AdminEventBean {

    private AdminEvent adminEvent;

    /** @param adminEvent 待包装的管理事件 */
    public AdminEventBean(AdminEvent adminEvent) {
        this.adminEvent = adminEvent;
    }

    /** @return 事件发生时间 */
    public Date getDate() {
        return new Date(adminEvent.getTime());
    }

    /** @return 操作类型（小写字符串） */
    public String getOperationType() {
        return adminEvent.getOperationType().toString().toLowerCase();
    }

    /** @return 触发事件的客户端 ID */
    public String getClient() {
        return adminEvent.getAuthDetails().getClientId();
    }

    /**
     * 注意：反向代理未提供有效地址时返回值可能不是真实 IP。
     *
     * @return 客户端 IP 地址
     */
    public String getIpAddress() {
        return adminEvent.getAuthDetails().getIpAddress();
    }

    /** @return 受影响的 Admin REST 资源路径 */
    public String getResourcePath() {
        return adminEvent.getResourcePath();
    }
}
