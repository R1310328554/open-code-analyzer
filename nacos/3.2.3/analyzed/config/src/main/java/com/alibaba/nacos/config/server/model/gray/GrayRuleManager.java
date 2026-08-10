/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.config.server.model.gray;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.google.gson.Gson;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 灰度规则管理器：通过 SPI 收集 type+version 到实现类的映射，负责构造、序列化与反序列化。
 * 持久化形态为 {@link ConfigGrayPersistInfo} JSON，运行时还原为 {@link GrayRule} 实例。
 * GrayRuleManager.
 *
 * @author zunfei.lzf
 */
public class GrayRuleManager {
    
    /** type_version 键到 GrayRule 实现类的并发映射表 */
    private static final Map<String, Class<?>> GRAY_RULE_MAP = new ConcurrentHashMap<>(8);
    
    /** type 与 version 拼接分隔符 */
    public static final String SPLIT = "_";
    
    static {
        Collection<GrayRule> grayRuleCollection = NacosServiceLoader.load(GrayRule.class);
        for (GrayRule grayRule : grayRuleCollection) {
            GRAY_RULE_MAP.put(grayRule.getType() + SPLIT + grayRule.getVersion(),
                grayRule.getClass());
        }
    }
    
    /**
     * 按 type 与 version 查找已注册的 GrayRule 实现类。
     *
     * @param type    规则类型
     * @param version 规则版本
     * @return 实现 Class，未注册则 null
     * @date 2024/3/14
     */
    public static Class<?> getClassByTypeAndVersion(String type, String version) {
        return GRAY_RULE_MAP.get(type + SPLIT + version);
    }
    
    /**
     * 由持久化 DTO 反射构造 GrayRule 实例（String, int 构造器）。
     *
     * @param configGrayPersistInfo 持久化信息
     * @return 灰度规则实例，类型未注册时 null
     * @date 2024/3/14
     */
    public static GrayRule constructGrayRule(ConfigGrayPersistInfo configGrayPersistInfo) {
        Class<?> classByTypeAndVersion = getClassByTypeAndVersion(configGrayPersistInfo.getType(),
            configGrayPersistInfo.getVersion());
        if (classByTypeAndVersion == null) {
            return null;
        }
        try {
            Constructor<?> declaredConstructor =
                classByTypeAndVersion.getDeclaredConstructor(String.class, int.class);
            declaredConstructor.setAccessible(true);
            return (GrayRule) declaredConstructor.newInstance(configGrayPersistInfo.getExpr(),
                configGrayPersistInfo.getPriority());
        } catch (Exception e) {
            throw new RuntimeException(
                String.format("construct gray rule failed with type[%s], version[%s].",
                    configGrayPersistInfo.getType(), configGrayPersistInfo.getVersion()),
                e);
        }
    }
    
    /**
     * 将运行时 GrayRule 转为可持久化的 DTO。
     *
     * @param grayRule 灰度规则实例
     * @return ConfigGrayPersistInfo
     * @date 2024/3/14
     */
    public static ConfigGrayPersistInfo constructConfigGrayPersistInfo(GrayRule grayRule) {
        return new ConfigGrayPersistInfo(grayRule.getType(), grayRule.getVersion(),
            grayRule.getRawGrayRuleExp(),
            grayRule.getPriority());
    }
    
    /**
     * 从数据库 JSON 字符串反序列化为持久化 DTO。
     *
     * @param grayRuleRawStringFromDb 数据库中的 JSON
     * @return ConfigGrayPersistInfo
     * @date 2024/3/14
     */
    public static ConfigGrayPersistInfo deserializeConfigGrayPersistInfo(
        String grayRuleRawStringFromDb) {
        return (new Gson()).fromJson(grayRuleRawStringFromDb, ConfigGrayPersistInfo.class);
    }
    
    /**
     * 将持久化 DTO 序列化为 JSON 字符串写入数据库。
     *
     * @param configGrayPersistInfo 持久化对象
     * @return JSON 字符串
     * @date 2024/3/14
     */
    public static String serializeConfigGrayPersistInfo(
        ConfigGrayPersistInfo configGrayPersistInfo) {
        return (new Gson()).toJson(configGrayPersistInfo);
    }
}
