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

package org.keycloak.representations.idm;

import java.util.List;

/**
 * 域用户事件与管理事件监听配置的 REST 表示。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RealmEventsConfigRepresentation {
    /** 是否启用用户事件记录。 */
    protected boolean eventsEnabled;
    /** 用户事件保留时长（秒）。 */
    protected Long eventsExpiration;
    /** 已注册的事件监听器 SPI ID 列表。 */
    protected List<String> eventsListeners;
    /** 启用的用户事件类型列表。 */
    protected List<String> enabledEventTypes;
    
    /** 是否启用管理事件记录。 */
    protected Boolean adminEventsEnabled;
    /** 管理事件是否包含详细变更内容。 */
    protected Boolean adminEventsDetailsEnabled;

    public boolean isEventsEnabled() {
        return eventsEnabled;
    }

    public void setEventsEnabled(boolean eventsEnabled) {
        this.eventsEnabled = eventsEnabled;
    }

    public Long getEventsExpiration() {
        return eventsExpiration;
    }

    public void setEventsExpiration(Long eventsExpiration) {
        this.eventsExpiration = eventsExpiration;
    }

    public List<String> getEventsListeners() {
        return eventsListeners;
    }

    public void setEventsListeners(List<String> eventsListeners) {
        this.eventsListeners = eventsListeners;
    }

    public List<String> getEnabledEventTypes() {
        return enabledEventTypes;
    }

    public void setEnabledEventTypes(List<String> enabledEventTypes) {
        this.enabledEventTypes = enabledEventTypes;
    }

    public Boolean isAdminEventsEnabled() {
        return adminEventsEnabled;
    }

    public void setAdminEventsEnabled(Boolean adminEventsEnabled) {
        this.adminEventsEnabled = adminEventsEnabled;
    }

    public Boolean isAdminEventsDetailsEnabled() {
        return adminEventsDetailsEnabled;
    }

    public void setAdminEventsDetailsEnabled(Boolean adminEventsDetailsEnabled) {
        this.adminEventsDetailsEnabled = adminEventsDetailsEnabled;
    }
    
}
