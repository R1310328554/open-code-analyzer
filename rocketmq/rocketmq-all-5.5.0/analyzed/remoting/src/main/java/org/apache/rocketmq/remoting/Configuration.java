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

package org.apache.rocketmq.remoting;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.remoting.protocol.DataVersion;

/**
 * Remoting 配置聚合器：注册配置对象与扩展属性，支持持久化与版本追踪。
 */
public class Configuration {

    private final Logger log;

    private List<Object> configObjectList = new ArrayList<>(4);
    private String storePath;
    private boolean storePathFromConfig = false;
    private Object storePathObject;
    private Field storePathField;
    private DataVersion dataVersion = new DataVersion();
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    /** 合并后的全部配置项（对象字段与扩展属性）。 */
    private Properties allConfigs = new Properties();

    public Configuration(Logger log) {
        this.log = log;
    }

    public Configuration(Logger log, Object... configObjects) {
        this.log = log;
        if (configObjects == null || configObjects.length == 0) {
            return;
        }
        for (Object configObject : configObjects) {
            if (configObject == null) {
                continue;
            }
            registerConfig(configObject);
        }
    }

    public Configuration(Logger log, String storePath, Object... configObjects) {
        this(log, configObjects);
        this.storePath = storePath;
    }

    /**
     * 注册配置对象并将其属性合并到全局配置。
     *
     * @return 当前 Configuration 实例
     */
    public Configuration registerConfig(Object configObject) {
        try {
            readWriteLock.writeLock().lockInterruptibly();

            try {

                Properties registerProps = MixAll.object2Properties(configObject);

                merge(registerProps, this.allConfigs);

                configObjectList.add(configObject);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("registerConfig lock error");
        }
        return this;
    }

    /**
     * 注册扩展 Properties 并合并到全局配置。
     *
     * @return 当前 Configuration 实例
     */
    public Configuration registerConfig(Properties extProperties) {
        if (extProperties == null) {
            return this;
        }

        try {
            readWriteLock.writeLock().lockInterruptibly();

            try {
                merge(extProperties, this.allConfigs);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("register lock error. {}" + extProperties);
        }

        return this;
    }

    /**
     * 指定从配置对象字段动态读取持久化路径。
     *
     * @throws java.lang.RuntimeException 字段不存在时抛出
     */
    public void setStorePathFromConfig(Object object, String fieldName) {
        assert object != null;

        try {
            readWriteLock.writeLock().lockInterruptibly();

            try {
                this.storePathFromConfig = true;
                this.storePathObject = object;
                // 校验字段存在且非 static
                this.storePathField = object.getClass().getDeclaredField(fieldName);
                assert this.storePathField != null
                    && !Modifier.isStatic(this.storePathField.getModifiers());
                this.storePathField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("setStorePathFromConfig lock error");
        }
    }

    private String getStorePath() {
        String realStorePath = null;
        try {
            readWriteLock.readLock().lockInterruptibly();

            try {
                realStorePath = this.storePath;

                if (this.storePathFromConfig) {
                    try {
                        realStorePath = (String) storePathField.get(this.storePathObject);
                    } catch (IllegalAccessException e) {
                        log.error("getStorePath error, ", e);
                    }
                }
            } finally {
                readWriteLock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("getStorePath lock error");
        }

        return realStorePath;
    }

    public void setStorePath(final String storePath) {
        this.storePath = storePath;
    }

    /** 按传入 Properties 更新已注册配置并持久化。 */
    public void update(Properties properties) {
        try {
            readWriteLock.writeLock().lockInterruptibly();

            try {
                // 更新时仅覆盖已存在的配置键
                mergeIfExist(properties, this.allConfigs);

                for (Object configObject : configObjectList) {
                    // 仅将变更项写回各注册对象，而非全量替换
                    MixAll.properties2Object(properties, configObject);
                }

                this.dataVersion.nextVersion();

            } finally {
                readWriteLock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("update lock error, {}", properties);
            return;
        }

        persist();
    }

    /** 将当前全部配置序列化写入存储路径。 */
    public void persist() {
        try {
            readWriteLock.readLock().lockInterruptibly();

            try {
                String allConfigs = getAllConfigsInternal();

                MixAll.string2File(allConfigs, getStorePath());
            } catch (IOException e) {
                log.error("persist string2File error, ", e);
            } finally {
                readWriteLock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("persist lock error");
        }
    }

    /** 返回全部配置的可读字符串表示。 */
    public String getAllConfigsFormatString() {
        try {
            readWriteLock.readLock().lockInterruptibly();

            try {

                return getAllConfigsInternal();

            } finally {
                readWriteLock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("getAllConfigsFormatString lock error");
        }

        return null;
    }

    /** 按指定键列表导出客户端可见配置字符串。 */
    public String getClientConfigsFormatString(List<String> clientKeys) {
        try {
            readWriteLock.readLock().lockInterruptibly();

            try {

                return getClientConfigsInternal(clientKeys);

            } finally {
                readWriteLock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("getAllConfigsFormatString lock error");
        }

        return null;
    }

    public String getDataVersionJson() {
        return this.dataVersion.toJson();
    }

    /** 获取合并后的全部配置 Properties。 */
    public Properties getAllConfigs() {
        try {
            readWriteLock.readLock().lockInterruptibly();

            try {

                return this.allConfigs;

            } finally {
                readWriteLock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            log.error("getAllConfigs lock error");
        }

        return null;
    }

    private String getAllConfigsInternal() {
        StringBuilder stringBuilder = new StringBuilder();

        // 导出前从各配置对象重新加载属性
        for (Object configObject : this.configObjectList) {
            Properties properties = MixAll.object2Properties(configObject);
            if (properties != null) {
                merge(properties, this.allConfigs);
            } else {
                log.warn("getAllConfigsInternal object2Properties is null, {}", configObject.getClass());
            }
        }

        {
            stringBuilder.append(MixAll.properties2String(this.allConfigs, true));
        }

        return stringBuilder.toString();
    }

    private String getClientConfigsInternal(List<String> clientConigKeys) {
        StringBuilder stringBuilder = new StringBuilder();
        Properties clientProperties = new Properties();

        // reload from config object ?
        for (Object configObject : this.configObjectList) {
            Properties properties = MixAll.object2Properties(configObject);

            for (String nameNow : clientConigKeys) {
                if (properties.containsKey(nameNow)) {
                    clientProperties.put(nameNow, properties.get(nameNow));
                }
            }

        }
        stringBuilder.append(MixAll.properties2String(clientProperties));

        return stringBuilder.toString();
    }

    private void merge(Properties from, Properties to) {
        for (Entry<Object, Object> next : from.entrySet()) {
            Object fromObj = next.getValue(), toObj = to.get(next.getKey());
            if (toObj != null && !toObj.equals(fromObj)) {
                log.info("Replace, key: {}, value: {} -> {}", next.getKey(), toObj, fromObj);
            }
            to.put(next.getKey(), fromObj);
        }
    }

    private void mergeIfExist(Properties from, Properties to) {
        for (Entry<Object, Object> next : from.entrySet()) {
            if (!to.containsKey(next.getKey())) {
                continue;
            }

            Object fromObj = next.getValue(), toObj = to.get(next.getKey());
            if (toObj != null && !toObj.equals(fromObj)) {
                log.info("Replace, key: {}, value: {} -> {}", next.getKey(), toObj, fromObj);
            }
            to.put(next.getKey(), fromObj);
        }
    }

}
