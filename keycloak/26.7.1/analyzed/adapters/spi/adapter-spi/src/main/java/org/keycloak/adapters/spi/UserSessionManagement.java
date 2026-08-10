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

package org.keycloak.adapters.spi;

import java.util.List;

/**
 * 用户会话管理 SPI，供适配器实现登出与 HTTP 会话清理。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserSessionManagement {

    /** 登出所有已关联的用户会话。 */
    void logoutAll();

    /**
     * 按 HTTP 会话 ID 列表登出指定会话。
     *
     * @param ids HTTP 会话标识符列表
     */
    void logoutHttpSessions(List<String> ids);
}
