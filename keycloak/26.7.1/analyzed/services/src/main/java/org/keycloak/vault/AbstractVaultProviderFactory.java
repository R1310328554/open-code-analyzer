/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.vault;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;

import org.jboss.logging.Logger;

/**
 * 支持配置密钥解析器的 {@link VaultProviderFactory} 抽象基类。
 * <p>在 {@link #init(Config.Scope)} 中读取 {@code keyResolvers} 属性（逗号分隔），验证并实例化 {@link VaultKeyResolver} 列表，供子类在 {@link #create(KeycloakSession)} 中传给 Vault 提供者。</p>
 * <p>可用解析器：</p>
 * <ul>
 *     <li>{@code KEY_ONLY}：仅使用密钥名，忽略领域；</li>
 *     <li>{@code REALM_UNDERSCORE_KEY}：领域与密钥以下划线连接，内部下划线双写转义；</li>
 *     <li>{@code REALM_FILESEPARATOR_KEY}：以平台文件分隔符连接，便于按目录分组；</li>
 *     <li>{@code FACTORY_PROVIDED}：由工厂 {@link #getFactoryResolver()} 自定义格式。</li>
 * </ul>
 * <p>未配置时默认 {@code REALM_UNDERSCORE_KEY}；全部解析器无效时抛出 {@link VaultConfigurationException}。子类 {@link #init(Config.Scope)} 须调用 {@code super.init(config)}。</p>
 * <p><b>注意</b>：使用 {@code FACTORY_PROVIDED} 时须同时覆盖 {@link #getId()} 以独立配置。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public abstract class AbstractVaultProviderFactory implements VaultProviderFactory {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    /** 密钥解析器列表的配置属性名。 */
    protected static final String KEY_RESOLVERS = "keyResolvers";

    /** 初始化后可用的密钥解析器列表。 */
    protected List<VaultKeyResolver> keyResolvers = new LinkedList<>();

    /** 从配置加载 keyResolvers，无效时回退默认或抛异常。 */
    @Override
    public void init(Config.Scope config) {
        String resolverNames = config.get(KEY_RESOLVERS);
        if (resolverNames != null) {
            for (String resolverName : resolverNames.split(",")) {
                VaultKeyResolver resolver = this.getVaultKeyResolver(resolverName);
                if (resolver != null) {
                    this.keyResolvers.add(resolver);
                }
            }
            if (this.keyResolvers.isEmpty()) {
                throw new VaultConfigurationException("Unable to initialize factory - all provided key resolvers are invalid");
            }
        }
        // 未配置解析器时使用默认 REALM_UNDERSCORE_KEY
        if (this.keyResolvers.isEmpty()) {
            logger.debugf("Key resolver is undefined - using %s by default", AvailableResolvers.REALM_UNDERSCORE_KEY.name());
            this.keyResolvers.add(AvailableResolvers.REALM_UNDERSCORE_KEY.getVaultKeyResolver());
        }
    }

    /**
     * 返回工厂自定义的 {@link VaultKeyResolver}（{@code FACTORY_PROVIDED} 时使用）。
     * <p>默认抛出 {@link UnsupportedOperationException}。</p>
     *
     * @return 工厂提供的解析器
     */
    protected VaultKeyResolver getFactoryResolver() {
        throw new UnsupportedOperationException("getFactoryResolver not implemented by factory " + getClass().getName());
    }

    /**
     * 从 {@link KeycloakSession} 获取当前领域名称。
     *
     * @param session Keycloak 会话
     * @return 领域名称
     */
    protected String getRealmName(KeycloakSession session) {
        return session.getContext().getRealm().getName();
    }

    /**
     * 按名称获取 {@link VaultKeyResolver} 实例。
     *
     * @param resolverName 解析器名称
     * @return 对应解析器，无效名称返回 {@code null}
     */
    private VaultKeyResolver getVaultKeyResolver(final String resolverName) {
        try {
            AvailableResolvers value = AvailableResolvers.valueOf(resolverName.trim().toUpperCase());
            return value == AvailableResolvers.FACTORY_PROVIDED ? this.getFactoryResolver() : value.getVaultKeyResolver();
        }
        catch(Exception e) {
            logger.debugf(e,"Invalid key resolver: %s - skipping", resolverName);
            return null;
        }
    }

    /** 可用 {@link VaultKeyResolver} 枚举，配置名须与枚举成员一致。 */
    protected enum AvailableResolvers {

        /** 忽略领域，所有领域共享同一密钥（密钥中下划线双写转义）。 */
        KEY_ONLY((realm, key) -> key.replaceAll("_", "__")),

        /** 领域名与密钥以下划线连接，内部下划线双写转义（默认解析器）。 */
        REALM_UNDERSCORE_KEY((realm, key) -> realm.replaceAll("_", "__") + "_" + key.replaceAll("_", "__")),

        /** 以平台文件分隔符连接领域与密钥，便于按目录结构分组。 */
        REALM_FILESEPARATOR_KEY((realm, key) -> realm + File.separator + key),

        /** 密钥格式由工厂 {@code getFactoryResolver} 自定义；枚举静态上下文无法引用工厂实例。 */
        FACTORY_PROVIDED(null);

        /** 解析器函数式实现。 */
        private VaultKeyResolver resolver;

        AvailableResolvers(final VaultKeyResolver resolver) {
            this.resolver = resolver;
        }

        /** @return 关联的 {@link VaultKeyResolver} */
        VaultKeyResolver getVaultKeyResolver() {
            return this.resolver;
        }
    }
}
