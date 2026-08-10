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

package com.alibaba.nacos.api.exception.runtime;

import java.lang.reflect.Type;

import static com.alibaba.nacos.api.common.Constants.Exception.DESERIALIZE_ERROR_CODE;

/**
 * Nacos 反序列化运行时异常。
 *
 * <p>在 JSON/Protobuf 等反序列化失败时抛出，错误码为 {@link com.alibaba.nacos.api.common.Constants.Exception#DESERIALIZE_ERROR_CODE}。</p>
 *
 * @author yangyi
 */
public class NacosDeserializationException extends NacosRuntimeException {
    
    private static final long serialVersionUID = -2742350751684273728L;
    
    private static final String DEFAULT_MSG = "Nacos deserialize failed. ";
    
    private static final String MSG_FOR_SPECIFIED_CLASS =
        "Nacos deserialize for class [%s] failed. ";
    
    private static final String ERROR_MSG_FOR_SPECIFIED_CLASS =
        "Nacos deserialize for class [%s] failed, cause error[%s]. ";
    
    /** 反序列化失败的目标类型（若已知）。 */
    private Class<?> targetClass;
    
    /** 构造默认反序列化异常。 */
    public NacosDeserializationException() {
        super(DESERIALIZE_ERROR_CODE);
    }
    
    /**
     * 构造指定目标类型的反序列化异常。
     *
     * @param targetClass 反序列化失败的目标类
     */
    public NacosDeserializationException(Class<?> targetClass) {
        super(DESERIALIZE_ERROR_CODE,
            String.format(MSG_FOR_SPECIFIED_CLASS, targetClass.getName()));
        this.targetClass = targetClass;
    }
    
    /**
     * 构造指定目标 {@link Type} 的反序列化异常。
     *
     * @param targetType 反序列化失败的目标类型
     */
    public NacosDeserializationException(Type targetType) {
        super(DESERIALIZE_ERROR_CODE,
            String.format(MSG_FOR_SPECIFIED_CLASS, targetType.toString()));
    }
    
    /**
     * 构造带根因的反序列化异常。
     *
     * @param throwable 根因异常
     */
    public NacosDeserializationException(Throwable throwable) {
        super(DESERIALIZE_ERROR_CODE, DEFAULT_MSG, throwable);
    }
    
    /**
     * 构造指定目标类与根因的反序列化异常。
     *
     * @param targetClass 反序列化失败的目标类
     * @param throwable   根因异常
     */
    public NacosDeserializationException(Class<?> targetClass, Throwable throwable) {
        super(DESERIALIZE_ERROR_CODE, String.format(ERROR_MSG_FOR_SPECIFIED_CLASS,
            targetClass.getName(), throwable.getMessage()), throwable);
        this.targetClass = targetClass;
    }
    
    /**
     * 构造指定目标类型与根因的反序列化异常。
     *
     * @param targetType 反序列化失败的目标类型
     * @param throwable  根因异常
     */
    public NacosDeserializationException(Type targetType, Throwable throwable) {
        super(DESERIALIZE_ERROR_CODE, String.format(ERROR_MSG_FOR_SPECIFIED_CLASS,
            targetType.toString(), throwable.getMessage()), throwable);
    }
    
    /** 获取反序列化失败的目标类（可能为 {@code null}）。 */
    public Class<?> getTargetClass() {
        return targetClass;
    }
}
