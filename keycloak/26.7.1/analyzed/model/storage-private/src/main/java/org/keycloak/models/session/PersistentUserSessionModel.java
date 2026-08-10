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

package org.keycloak.models.session;

/**
 * 持久化用户会话的数据传输模型：存储会话元数据及 JSON 序列化的扩展字段。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface PersistentUserSessionModel {

    /** 用户会话 ID。 */
    String getUserSessionId();

    void setUserSessionId(String userSessionId);

    /** 会话创建时间（秒）。 */
    int getStarted();

    void setStarted(int started);

    /** 最近一次会话刷新时间（秒）。 */
    int getLastSessionRefresh();

    void setLastSessionRefresh(int lastSessionRefresh);

    /** 是否为离线会话。 */
    boolean isOffline();

    void setOffline(boolean offline);

    /** JSON 序列化的会话扩展数据。 */
    String getData();

    void setData(String data);

    void setRealmId(String realmId);

    void setUserId(String userId);

    void setBrokerSessionId(String brokerSessionId);

    /** 是否勾选“记住我”。 */
    boolean isRememberMe();

    void setRememberMe(boolean rememberMe);

}
