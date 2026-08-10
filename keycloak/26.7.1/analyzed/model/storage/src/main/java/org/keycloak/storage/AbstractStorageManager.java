/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.storage;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.common.util.reflections.Types;
import org.keycloak.component.ComponentFactory;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.utils.ServicesUtils;

import org.jboss.logging.Logger;

/**
 * 存储 Provider 管理抽象基类：按 realm 查找、实例化并调用外部存储 Provider。
 * <p>
 * 提供超时保护的多 Provider 聚合查询，以及按能力接口（capability interface）筛选实例。
 *
 * @param <ProviderType> 用于查找工厂并创建 Provider 实例的类型
 * @param <StorageProviderModelType> 存储 Provider 配置模型类型，须继承
 *                                  {@link CacheableStorageProviderModel}（含 {@code isEnabled()}）及
 *                                  {@link org.keycloak.component.PrioritizedComponentModel}（支持优先级排序）
 */
public abstract class AbstractStorageManager<ProviderType extends Provider,
        StorageProviderModelType extends CacheableStorageProviderModel> {

    private static final Logger LOG = Logger.getLogger(AbstractStorageManager.class);

    /**
     * 从外部存储获取模型的超时时间（毫秒）；默认 3000，可通过配置覆盖。
     */
    private static final Long STORAGE_PROVIDER_DEFAULT_TIMEOUT = 3000L;
    /** 当前 Keycloak 会话。 */
    protected final KeycloakSession session;
    /** Provider 接口类型。 */
    private final Class<ProviderType> providerTypeClass;
    /** Provider 工厂类型。 */
    private final Class<? extends ProviderFactory> factoryTypeClass;
    /** 将 ComponentModel 转换为存储 Provider 模型的函数。 */
    private final Function<ComponentModel, StorageProviderModelType> toStorageProviderModelTypeFunction;
    /** 配置作用域名称（如 {@code user}）。 */
    private final String configScope;
    /** 缓存解析后的存储 Provider 超时值。 */
    private Long storageProviderTimeout;

    /** 构造存储管理器。 */
    public AbstractStorageManager(KeycloakSession session, Class<? extends ProviderFactory> factoryTypeClass, Class<ProviderType> providerTypeClass, Function<ComponentModel, StorageProviderModelType> toStorageProviderModelTypeFunction, String configScope) {
        this.session = session;
        this.providerTypeClass = providerTypeClass;
        this.factoryTypeClass = factoryTypeClass;
        this.toStorageProviderModelTypeFunction = toStorageProviderModelTypeFunction;
        this.configScope = configScope;
    }

    /** 获取存储 Provider 调用超时（毫秒），懒加载自配置。 */
    protected Long getStorageProviderTimeout() {
        if (storageProviderTimeout == null) {
            storageProviderTimeout = Config.scope(configScope).getLong("storageProviderTimeout", STORAGE_PROVIDER_DEFAULT_TIMEOUT);
        }
        return storageProviderTimeout;
    }

    /**
     * 按 providerId 返回可创建 {@code CreatedProviderType} 实例的工厂。
     *
     * @param providerId 工厂标识
     * @return 实现 {@code ComponentFactory<CreatedProviderType, ProviderType>} 的工厂
     */
    protected <T extends ProviderType> ComponentFactory<T, ProviderType> getStorageProviderFactory(String providerId) {
        return (ComponentFactory<T, ProviderType>) session.getKeycloakSessionFactory()
                .getProviderFactory(providerTypeClass, providerId);
    }

    /**
     * 返回 realm 内实现指定能力接口的全部已启用存储 Provider 流。
     *
     * @param realm realm
     * @param capabilityInterface 能力接口类，如 {@code GroupLookupProvider} 或 {@code UserQueryProvider}
     * @return 已启用且匹配 {@code getProviderTypeClass()} 的 Provider 流
     */
    protected <T> Stream<T> getEnabledStorageProviders(RealmModel realm, Class<T> capabilityInterface) {
        return getStorageProviderModels(realm, providerTypeClass)
                .map(toStorageProviderModelTypeFunction)
                .filter(StorageProviderModelType::isEnabled)
                .sorted(StorageProviderModelType.comparator)
                .map(storageProviderModelType -> getStorageProviderInstance(storageProviderModelType, capabilityInterface, false))
                .filter(Objects::nonNull);
    }

    /**
     * 对所有已启用且实现能力接口的 StorageProvider 应用 {@code applyFunction} 并 flatMap 合并结果。
     * <p>
     * 每个 StorageProvider 须在限定时间内响应，超时则返回空流。
     *
     * @param realm realm
     * @param capabilityInterface 能力接口类
     * @param applyFunction 应用于各 Provider 的函数
     * @param <R> applyFunction 返回元素类型
     * @return 所有 Provider 结果的合并流
     */
    protected <R, T> Stream<R> flatMapEnabledStorageProvidersWithTimeout(RealmModel realm, Class<T> capabilityInterface, Function<T, ? extends Stream<R>> applyFunction) {
        return getEnabledStorageProviders(realm, capabilityInterface)
                .flatMap(ServicesUtils.timeBound(session, getStorageProviderTimeout(), applyFunction));
    }

    /**
     * 对所有已启用 StorageProvider 应用 {@code applyFunction} 并 map 为结果流。
     * <p>
     * 每个 StorageProvider 须在限定时间内响应，超时则跳过（返回 null 并过滤）。
     *
     * @param realm realm
     * @param capabilityInterface 能力接口类
     * @param applyFunction 应用于各 Provider 的函数
     * @param <R> applyFunction 返回类型
     * @return 各 Provider 结果的流
     */
    protected <R, T> Stream<R> mapEnabledStorageProvidersWithTimeout(RealmModel realm, Class<T> capabilityInterface, Function<T, R> applyFunction) {
        return getEnabledStorageProviders(realm, capabilityInterface)
                .map(ServicesUtils.timeBoundOne(session, getStorageProviderTimeout(), applyFunction))
                .filter(Objects::nonNull);
    }

    /**
     * 对所有已启用 StorageProvider 依次调用 {@code consumer}。
     * <p>
     * 每个 Provider 调用受超时限制。
     *
     * @param realm realm
     * @param capabilityInterface 能力接口类
     * @param consumer 应用于各 Provider 的消费者
     */
    protected <T> void consumeEnabledStorageProvidersWithTimeout(RealmModel realm, Class<T> capabilityInterface, Consumer<T> consumer) {
        getEnabledStorageProviders(realm, capabilityInterface)
                .forEachOrdered(ServicesUtils.consumeWithTimeBound(session, getStorageProviderTimeout(), consumer));
    }


    /** 按 providerId 获取实现能力接口的 Provider 实例（不含已禁用）。 */
    protected <T> T getStorageProviderInstance(RealmModel realm, String providerId, Class<T> capabilityInterface) {
        return getStorageProviderInstance(realm, providerId, capabilityInterface, false);
    }

    /**
     * 返回 realm 内指定 providerId 的 Provider 实例；若未实现能力接口则返回 null。
     *
     * @param realm realm
     * @param providerId 数据库/存储中的 ComponentModel ID
     * @param capabilityInterface 能力接口类
     * @return Provider 实例，或 null
     */
    protected <T> T getStorageProviderInstance(RealmModel realm, String providerId, Class<T> capabilityInterface, boolean includeDisabled) {
        if (providerId == null || capabilityInterface == null) return null;
        return getStorageProviderInstance(getStorageProviderModel(realm, providerId), capabilityInterface, includeDisabled);
    }

    /**
     * 返回 realm 与 providerId 对应的 StorageProvider 配置模型。
     *
     * @param realm Realm
     * @param providerId Provider ID
     * @return StorageProviderModelType 实例，或组件不存在时 null
     */
    protected StorageProviderModelType getStorageProviderModel(RealmModel realm, String providerId) {
        ComponentModel componentModel = realm.getComponent(providerId);
        if (componentModel == null) {
            return null;
        }
        return toStorageProviderModelTypeFunction.apply(componentModel);
    }

    /**
     * 按配置模型返回 Provider 实例（默认不含已禁用）；未实现能力接口时返回 null。
     *
     * @param model 自存储获取的 StorageProviderModel
     * @param capabilityInterface 能力接口类
     * @param <T> 所需能力接口类型
     * @return 类型 T 的实例，或 null
     */
    protected <T> T getStorageProviderInstance(StorageProviderModelType model, Class<T> capabilityInterface) {
        return getStorageProviderInstance(model, capabilityInterface, false);
    }

    /**
     * 按配置模型返回 Provider 实例；{@code includeDisabled} 为 true 时包含已禁用 Provider。
     *
     * @param model 自存储获取的 StorageProviderModel
     * @param capabilityInterface 能力接口类
     * @param includeDisabled 为 true 时亦返回已禁用的 Provider
     * @return 类型 T 的实例，或 null
     */
    protected <T> T getStorageProviderInstance(StorageProviderModelType model, Class<T> capabilityInterface, boolean includeDisabled) {
        if (model == null || (!model.isEnabled() && !includeDisabled) || capabilityInterface == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        ProviderType instance = (ProviderType) session.getAttribute(model.getId());
        if (instance != null && capabilityInterface.isAssignableFrom(instance.getClass())) return capabilityInterface.cast(instance);

        ComponentFactory<? extends ProviderType, ProviderType> factory = getStorageProviderFactory(model.getProviderId());
        if (factory == null) {
            LOG.warnv("Configured StorageProvider {0} of provider id {1} does not exist", model.getName(), model.getProviderId());
            return null;
        }
        if (!Types.supports(capabilityInterface, factory, factoryTypeClass)) {
            return null;
        }

        instance = factory.create(session, model);
        if (instance == null) {
            throw new IllegalStateException("StorageProviderFactory (of type " + factory.getClass().getName() + ") produced a null instance");
        }
        session.enlistForClose(instance);
        session.setAttribute(model.getId(), instance);
        return capabilityInterface.cast(instance);
    }

    /**
     * 返回 realm 内指定 storageType 的全部 ComponentModel 流。
     *
     * @param realm Realm
     * @param storageType Provider 类型
     * @return ComponentModel 流
     */
    public static Stream<ComponentModel> getStorageProviderModels(RealmModel realm, Class<? extends Provider> storageType) {
        return realm.getStorageProviders(storageType);
    }
}
