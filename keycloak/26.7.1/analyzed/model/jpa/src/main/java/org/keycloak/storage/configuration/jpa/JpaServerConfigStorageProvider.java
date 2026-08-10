/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.storage.configuration.jpa;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.keycloak.storage.configuration.ServerConfigStorageProvider;
import org.keycloak.storage.configuration.jpa.entity.ServerConfigEntity;
import org.keycloak.utils.StreamsUtil;

import org.hibernate.jpa.HibernateHints;

/**
 * 基于 JPA 的 {@link ServerConfigStorageProvider}：通过 {@link EntityManager} 持久化服务器配置键值。
 */
public class JpaServerConfigStorageProvider implements ServerConfigStorageProvider {

    /** JPA 实体管理器。 */
    private final EntityManager entityManager;

    public JpaServerConfigStorageProvider(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager);
    }

    @Override
    public Optional<String> find(String key) {
        return Optional.ofNullable(getEntity(key))
                .map(ServerConfigEntity::getValue);
    }

    @Override
    public void store(String key, String value) {
        var entity = getEntity(key);
        if (entity == null) {
            entity = new ServerConfigEntity();
            entity.setKey(Objects.requireNonNull(key));
            entity.setValue(Objects.requireNonNull(value));
            entityManager.persist(entity);
            return;
        }
        entity.setValue(Objects.requireNonNull(value));
        entityManager.merge(entity);
    }

    @Override
    public void remove(String key) {
        var entity = getEntity(key);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public String loadOrCreate(String key, Supplier<String> valueGenerator) {
        var entity = getEntity(key);
        if (entity != null) {
            return entity.getValue();
        }
        var value = Objects.requireNonNull(valueGenerator.get());
        entity = new ServerConfigEntity();
        entity.setKey(Objects.requireNonNull(key));
        entity.setValue(value);
        entityManager.persist(entity);
        return value;
    }

    @Override
    public boolean replace(String key, Predicate<String> replacePredicate, Supplier<String> valueGenerator) {
        Objects.requireNonNull(replacePredicate);
        Objects.requireNonNull(valueGenerator);
        var entity = getEntity(key);
        if (entity == null || !replacePredicate.test(entity.getValue())) {
            return false;
        }
        entity.setValue(valueGenerator.get());
        entityManager.merge(entity);
        return true;
    }

    @Override
    public Stream<String> keys() {
        var stream = entityManager.createNamedQuery("findServerConfigKeys", String.class)
                .setHint(HibernateHints.HINT_READ_ONLY, true)
                .getResultStream();
        return StreamsUtil.closing(stream);
    }

    @Override
    public void close() {
        // 无操作
    }

    /**
     * 以乐观锁读取配置实体，避免并发更新丢失。
     * <p>
     * 乐观锁足以防止以下场景：事务 T1 读取一行，事务 T2 在 T1 提交前修改或删除该行，随后两事务均成功提交。
     */
    private ServerConfigEntity getEntity(String key) {
        return entityManager.find(ServerConfigEntity.class, Objects.requireNonNull(key), LockModeType.OPTIMISTIC);
    }
}
