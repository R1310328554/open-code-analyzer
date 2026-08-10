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
package org.keycloak.models;

/**
 * 离线用户会话模型扩展接口，可直接获取用户 ID 与登录用户名。
 * <p>扩展 {@link UserSessionModel}，供离线令牌持久化层使用。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface OfflineUserSessionModel extends UserSessionModel {
    /** 离线会话关联的用户 ID。 */
    public String getUserId();

    /** 设置离线会话记录的用户登录名。 */
    void setLoginUsername(String loginUsername);
}
