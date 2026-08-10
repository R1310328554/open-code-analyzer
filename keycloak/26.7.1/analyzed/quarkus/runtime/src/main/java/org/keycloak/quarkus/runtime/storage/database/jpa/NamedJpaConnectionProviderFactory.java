/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.storage.database.jpa;

import java.util.function.Supplier;

import jakarta.persistence.EntityManagerFactory;

/**
 * 按持久化单元名称解析 {@link EntityManagerFactory} 的 JPA 连接工厂。
 * <p>用于多数据源场景：{@link #unitName} 同时作为工厂 ID 与 CDI 持久化单元名。</p>
 */
public final class NamedJpaConnectionProviderFactory extends AbstractJpaConnectionProviderFactory {

    /** 目标 JPA 持久化单元名称。 */
    private String unitName;

    /** 按 {@link #unitName} 从 CDI 解析 {@link EntityManagerFactory}，未找到则抛出异常。 */
    @Override
    protected EntityManagerFactory getEntityManagerFactory() {
        return getEntityManagerFactory(unitName).orElseThrow(new Supplier<IllegalStateException>() {
            @Override
            public IllegalStateException get() {
                return new IllegalStateException("Could not resolve named EntityManagerFactory [" + unitName + "]");
            }
        });
    }

    /** 返回配置的持久化单元名称。 */
    public String getUnitName() {
        return unitName;
    }

    /** 设置持久化单元名称（同时影响 {@link #getId()}）。 */
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    /** 工厂 ID 与持久化单元名一致。 */
    @Override
    public String getId() {
        return unitName;
    }
}
