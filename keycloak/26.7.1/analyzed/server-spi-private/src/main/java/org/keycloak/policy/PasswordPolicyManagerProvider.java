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

package org.keycloak.policy;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

/**
 * 密码策略管理器提供者接口：在 realm 上下文中聚合校验密码。
 * <p>实现类（如 {@link DefaultPasswordPolicyManagerProvider}）按 realm 已启用的各 {@link PasswordPolicyProvider} 顺序执行校验。</p>
 *
 * @author <a href="mailto:roelof.naude@epiuse.com">Roelof Naude</a>
 */
public interface PasswordPolicyManagerProvider extends Provider {

    /** 在指定 realm 与用户上下文中校验密码。 */
    PolicyError validate(RealmModel realm, UserModel user, String password);
    /** 使用当前会话 realm 校验密码（无用户模型）。 */
    PolicyError validate(String user, String password);

}
