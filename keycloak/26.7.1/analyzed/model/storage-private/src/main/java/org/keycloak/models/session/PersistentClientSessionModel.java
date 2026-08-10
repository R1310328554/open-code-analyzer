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
 * 持久化客户端会话的数据传输模型：关联用户会话、客户端及 JSON 序列化负载。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface PersistentClientSessionModel {

    /** 所属用户会话 ID。 */
    String getUserSessionId();

    void setUserSessionId(String userSessionId);

    /** 客户端内部 UUID。 */
    String getClientId();

    void setClientId(String clientId);

    /** 会话最后活动时间戳（秒）。 */
    int getTimestamp();

    void setTimestamp(int timestamp);

    /** JSON 序列化的会话扩展数据。 */
    String getData();

    void setData(String data);
}
