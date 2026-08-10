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

import java.security.Principal;
import java.util.Set;

/**
 * 已认证 Keycloak 用户的安全账户视图 SPI。
 *
 * <p>封装 {@link Principal} 与角色集合，供适配器向容器安全上下文注册用户身份。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface KeycloakAccount {
    /** 返回已认证用户的主体。 */
    Principal getPrincipal();

    /** 返回用户拥有的角色名称集合。 */
    Set<String> getRoles();
}
