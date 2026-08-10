/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.workflow;

import java.util.Objects;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_AFTER;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_PRIORITY;

/**
 * 工作流中的单个步骤，封装提供者 ID、组件配置与执行优先级。
 * <p>可从 {@link ComponentModel} 构造，支持按优先级排序。</p>
 */
public class WorkflowStep implements Comparable<WorkflowStep> {

    private KeycloakSession session;
    private String id;
    private final String providerId;
    private MultivaluedHashMap<String, String> config;

    /** 以提供者 ID 与配置构造步骤（无持久化 ID）。 */
    public WorkflowStep(String providerId, MultivaluedHashMap<String, String> config) {
        this.providerId = providerId;
        this.config = config;
    }

    /** 从领域组件模型加载步骤定义。 */
    public WorkflowStep(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.id = model.getId();
        this.providerId = model.getProviderId();
        this.config = model.getConfig();
    }

    public String getId() {
        return id;
    }

    public String getProviderId() {
        return providerId;
    }

    public WorkflowStep setConfig(String key, String value) {
        if (config == null) {
            config = new MultivaluedHashMap<>();
        }
        this.config.putSingle(key, value);
        return this;
    }

    public MultivaluedHashMap<String, String> getConfig() {
        if (config == null) {
            return new MultivaluedHashMap<>();
        }
        return config;
    }

    /** 设置步骤执行优先级（数值越小越优先）。 */
    public void setPriority(int priority) {
        setConfig(CONFIG_PRIORITY, String.valueOf(priority));
    }

    public int getPriority() {
        String value = getConfig().getFirst(CONFIG_PRIORITY);
        if (value == null) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return Integer.MAX_VALUE;
        }
    }

    /** 设置本步骤须在前置步骤 ID 之后执行。 */
    public void setAfter(String after) {
        setConfig(CONFIG_AFTER, after);
    }

    public String getAfter() {
        return getConfig().getFirst(CONFIG_AFTER);
    }

    /** 从步骤提供者获取通知邮件主题，无则返回 null。 */
    public String getNotificationSubject() {
        if (session != null) {
            WorkflowStepProvider provider = Workflows.getStepProvider(session, this);
            if (provider != null) {
                return provider.getNotificationSubject();
            }
        }
        return null;
    }

    /** 从步骤提供者获取通知邮件正文，无则返回 null。 */
    public String getNotificationMessage() {
        if (session != null) {
            WorkflowStepProvider provider = Workflows.getStepProvider(session, this);
            if (provider != null) {
                return provider.getNotificationMessage();
            }
        }
        return null;
    }

    @Override
    public int compareTo(WorkflowStep other) {
        return Integer.compare(this.getPriority(), other.getPriority());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WorkflowStep that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
