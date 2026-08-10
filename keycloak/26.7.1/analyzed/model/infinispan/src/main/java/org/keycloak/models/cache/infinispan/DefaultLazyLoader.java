/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models.cache.infinispan;

import java.util.function.Function;
import java.util.function.Supplier;

import org.keycloak.models.KeycloakSession;

import static org.keycloak.authorization.fgap.AdminPermissionsSchema.runWithoutAuthorization;

/**
 * LazyLoader 的默认实现：数据仅加载一次，线程安全。
 * <p>
 * 缓存数据用于 CachedRealm 等可在 Keycloak 实例内多线程共享的实体；
 * 在 FGAP 启用时于无授权上下文下加载，避免缓存部分结果。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class DefaultLazyLoader<S, D> implements LazyLoader<S, D> {

    /** 从源对象转换为目标数据的函数。 */
    private final Function<S, D> loader;
    /** 源对象为 null 时的回退供应器。 */
    private final Supplier<D> fallback;
    /** 已加载并缓存的数据（volatile + 双重检查锁）。 */
    private volatile D data;

    /** 构造懒加载器。 */
    public DefaultLazyLoader(Function<S, D> loader, Supplier<D> fallback) {
        this.loader = loader;
        this.fallback = fallback;
    }

    /** 线程安全地加载并缓存数据，仅首次访问时触发 loader。 */
    @Override
    public D get(KeycloakSession session, Supplier<S> sourceSupplier) {
        if (data == null) {
            synchronized (this) {
                if (data == null) {
                    runWithoutAuthorization(session, () -> {
                        // 确保 FGAP 启用时缓存不包含部分授权过滤结果
                        S source = sourceSupplier.get();
                        data = source == null ? fallback.get() : loader.apply(source);
                    });
                }
            }
        }
        return data;
    }
}
