/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.auth.authentication.provider;

import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.auth.authentication.exception.AuthenticationException;
import org.apache.rocketmq.auth.authentication.model.User;
import org.apache.rocketmq.auth.config.AuthConfig;
import org.apache.rocketmq.common.config.ConfigRocksDBStorage;
import org.apache.rocketmq.common.thread.ThreadPoolMonitor;
import org.rocksdb.RocksDB;

/**
 * 本地 RocksDB 用户元数据提供者：JSON 序列化用户记录，Caffeine 缓存加速读路径。
 */
public class LocalAuthenticationMetadataProvider implements AuthenticationMetadataProvider {

    /** RocksDB 默认列族名，用于存储用户键值。 */
    private final static String AUTH_METADATA_COLUMN_FAMILY = new String(RocksDB.DEFAULT_COLUMN_FAMILY,
        StandardCharsets.UTF_8);

    private ConfigRocksDBStorage storage;

    private LoadingCache<String, User> userCache;

    protected ThreadPoolExecutor cacheRefreshExecutor;

    /** 打开 users 目录下 RocksDB，并按配置初始化 Caffeine 用户缓存。 */
    @Override
    public void initialize(AuthConfig authConfig, Supplier<?> metadataService) {
        this.storage = ConfigRocksDBStorage.getStore(authConfig.getAuthConfigPath() + File.separator + "users", false);
        if (!this.storage.start()) {
            throw new RuntimeException("Failed to load rocksdb for auth_user, please check whether it is occupied");
        }

        this.cacheRefreshExecutor = ThreadPoolMonitor.createAndMonitor(
            1,
            1,
            1000 * 60,
            TimeUnit.MILLISECONDS,
            "UserCacheRefresh",
            100000
        );

        this.userCache = Caffeine.newBuilder()
            .maximumSize(authConfig.getUserCacheMaxNum())
            .expireAfterAccess(authConfig.getUserCacheExpiredSecond(), TimeUnit.SECONDS)
            .refreshAfterWrite(authConfig.getUserCacheRefreshSecond(), TimeUnit.SECONDS)
            .executor(cacheRefreshExecutor)
            .build(new UserCacheLoader(this.storage));
    }

    /** 写入用户 JSON 到 RocksDB，刷 WAL 并失效缓存。 */
    @Override
    public CompletableFuture<Void> createUser(User user) {
        try {
            byte[] keyBytes = user.getUsername().getBytes(StandardCharsets.UTF_8);
            byte[] valueBytes = JSON.toJSONBytes(user);
            this.storage.put(AUTH_METADATA_COLUMN_FAMILY, keyBytes, keyBytes.length, valueBytes);
            this.storage.flushWAL();
            this.userCache.invalidate(user.getUsername());
        } catch (Exception e) {
            throw new AuthenticationException("create user to RocksDB failed", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /** 从 RocksDB 删除用户并失效缓存。 */
    @Override
    public CompletableFuture<Void> deleteUser(String username) {
        try {
            this.storage.delete(AUTH_METADATA_COLUMN_FAMILY, username.getBytes(StandardCharsets.UTF_8));
            this.storage.flushWAL();
            this.userCache.invalidate(username);
        } catch (Exception e) {
            throw new AuthenticationException("delete user from RocksDB failed", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /** 覆盖写入用户信息并失效缓存。 */
    @Override
    public CompletableFuture<Void> updateUser(User user) {
        try {
            byte[] keyBytes = user.getUsername().getBytes(StandardCharsets.UTF_8);
            byte[] valueBytes = JSON.toJSONBytes(user);
            this.storage.put(AUTH_METADATA_COLUMN_FAMILY, keyBytes, keyBytes.length, valueBytes);
            this.storage.flushWAL();
            this.userCache.invalidate(user.getUsername());
        } catch (Exception e) {
            throw new AuthenticationException("update user to RocksDB failed", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /** 经 Caffeine 缓存读取用户；未命中时返回 null。 */
    @Override
    public CompletableFuture<User> getUser(String username) {
        User user = this.userCache.get(username);
        if (user == UserCacheLoader.EMPTY_USER) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(user);
    }

    /** 全表扫描用户，可选按用户名子串过滤。 */
    @Override
    public CompletableFuture<List<User>> listUser(String filter) {
        List<User> result = new ArrayList<>();
        CompletableFuture<List<User>> future = new CompletableFuture<>();
        try {
            this.storage.iterate(AUTH_METADATA_COLUMN_FAMILY, (key, value) -> {
                String username = new String(key, StandardCharsets.UTF_8);
                if (StringUtils.isNotBlank(filter) && !username.contains(filter)) {
                    return;
                }
                User user = JSON.parseObject(new String(value, StandardCharsets.UTF_8), User.class);
                result.add(user);
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        future.complete(result);
        return future;
    }

    /** 关闭 RocksDB 存储与缓存刷新线程池。 */
    @Override
    public void shutdown() {
        if (this.storage != null) {
            this.storage.shutdown();
        }
        if (this.cacheRefreshExecutor != null) {
            this.cacheRefreshExecutor.shutdown();
        }
    }

    /** Caffeine {@link CacheLoader}：从 RocksDB 加载用户，空值用 {@link #EMPTY_USER} 占位。 */
    private static class UserCacheLoader implements CacheLoader<String, User> {
        private final ConfigRocksDBStorage storage;
        /** 表示用户不存在的哨兵对象，避免缓存穿透。 */
        public static final User EMPTY_USER = new User();

        public UserCacheLoader(ConfigRocksDBStorage storage) {
            this.storage = storage;
        }

        /** 按用户名从 RocksDB 反序列化 {@link User}。 */
        @Override
        public User load(String username) {
            try {
                byte[] keyBytes = username.getBytes(StandardCharsets.UTF_8);
                byte[] valueBytes = storage.get(AUTH_METADATA_COLUMN_FAMILY, keyBytes);
                if (ArrayUtils.isEmpty(valueBytes)) {
                    return EMPTY_USER;
                }
                return JSON.parseObject(new String(valueBytes, StandardCharsets.UTF_8), User.class);
            } catch (Exception e) {
                throw new AuthenticationException("Get user from RocksDB failed.", e);
            }
        }
    }
}
