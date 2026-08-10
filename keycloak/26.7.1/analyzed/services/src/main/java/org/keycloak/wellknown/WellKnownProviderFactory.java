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

package org.keycloak.wellknown;

import org.keycloak.provider.ProviderFactory;

/**
 * {@link WellKnownProvider} 的 {@link ProviderFactory} 接口。
 * <p>定义 URL 别名、优先级及是否可通过服务器元数据端点暴露等扩展点。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface WellKnownProviderFactory extends ProviderFactory<WellKnownProvider> {

    /**
     * 用作 well-known 提供者 URL 后缀的别名。
     * <p>例如别名为 {@code openid-configuration} 时，端点可能为 {@code https://myhost/auth/realms/myrealm/.well-known/openid-configuration}。多个工厂使用相同别名时，优先级最低（数值最小）者生效。</p>
     *
     * @see #getPriority()
     */
    default String getAlias() {
        return getId();
    }

    /**
     * 返回较低优先级，使自定义 {@code openid-configuration} 实现优先于 Keycloak 内置 {@link org.keycloak.protocol.oidc.OIDCWellKnownProviderFactory}。
     */
    default int getPriority() {
        return 1;
    }

    /**
     * 控制 {@link WellKnownProvider} 是否可通过服务器元数据端点访问。
     * <p>返回 true 时，提供者可通过 {@code /.well-known/{alias}/realms/{realm}} 暴露，其中 {@code {alias}} 为 {@link #getAlias()} 返回值。默认实现返回 false。</p>
     *
     * @return 若可通过 ServerMetadataResource 暴露则为 true
     */
    default boolean isAvailableViaServerMetadata() {
        return false;
    }
}
