/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oid4vc.issuance.keybinding;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oid4vc.OID4VCEnvironmentProviderFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link ProofValidator} 的 Provider 工厂接口。
 * <p>继承 {@link OID4VCEnvironmentProviderFactory}，便于按 OID4VC 环境配置实例化 proof 校验器。</p>
 */
public interface ProofValidatorFactory extends ProviderFactory<ProofValidator>, OID4VCEnvironmentProviderFactory {

    // 多数具体工厂无需实现下列生命周期方法，故提供空默认实现以简化子类。

    /** {@inheritDoc} 默认无初始化逻辑。 */
    @Override
    default void init(Config.Scope config) {
    }

    /** {@inheritDoc} 默认无 postInit 逻辑。 */
    @Override
    default void postInit(KeycloakSessionFactory factory) {
    }

    /** {@inheritDoc} 默认无资源需释放。 */
    @Override
    default void close() {
    }
}
