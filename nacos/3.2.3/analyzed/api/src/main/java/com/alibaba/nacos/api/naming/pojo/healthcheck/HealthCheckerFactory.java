/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.naming.pojo.healthcheck;

import com.alibaba.nacos.api.exception.runtime.NacosDeserializationException;
import com.alibaba.nacos.api.exception.runtime.NacosSerializationException;
import com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker.None;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

import java.io.IOException;

/**
 * 健康检查器工厂，负责 JSON 序列化/反序列化及子类型注册。
 *
 * <p>内部持有配置多态子类型的 {@link ObjectMapper}，忽略未知 JSON 字段以保持向前兼容。</p>
 *
 * @author yangyi
 */
public class HealthCheckerFactory {
    
    /** 共享 Jackson 映射器，已禁用未知属性反序列化失败。 */
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    static {
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
    
    /**
     * 注册扩展健康检查器子类型（从实例推断类名与 type）。
     *
     * @param extendHealthChecker 扩展检查器实例
     */
    public static void registerSubType(AbstractHealthChecker extendHealthChecker) {
        registerSubType(extendHealthChecker.getClass(), extendHealthChecker.getType());
    }
    
    /**
     * 注册扩展健康检查器子类型，供序列化与反序列化使用。
     *
     * @param extendHealthCheckerClass 扩展检查器实现类
     * @param typeName                 检查器类型名
     */
    public static void registerSubType(
        Class<? extends AbstractHealthChecker> extendHealthCheckerClass,
        String typeName) {
        MAPPER.registerSubtypes(new NamedType(extendHealthCheckerClass, typeName));
    }
    
    /**
     * 创建默认的 {@link None} 健康检查器（不执行探测）。
     *
     * @return 新的 NONE 检查器实例
     */
    public static None createNoneHealthChecker() {
        return new None();
    }
    
    /**
     * 从 JSON 字符串反序列化并创建健康检查器实例。
     *
     * @param jsonString 检查器 JSON 字符串
     * @return 反序列化后的检查器对象
     */
    public static AbstractHealthChecker deserialize(String jsonString) {
        try {
            return MAPPER.readValue(jsonString, AbstractHealthChecker.class);
        } catch (IOException e) {
            throw new NacosDeserializationException(AbstractHealthChecker.class, e);
        }
    }
    
    /**
     * 将健康检查器实例序列化为 JSON 字符串。
     *
     * @param healthChecker 检查器实例
     * @return 序列化后的 JSON 字符串
     */
    public static String serialize(AbstractHealthChecker healthChecker) {
        try {
            return MAPPER.writeValueAsString(healthChecker);
        } catch (JsonProcessingException e) {
            throw new NacosSerializationException(healthChecker.getClass(), e);
        }
    }
}
