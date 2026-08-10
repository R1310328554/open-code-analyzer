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

import static com.alibaba.nacos.api.common.Constants.Exception.SERIALIZE_ERROR_CODE;

/**
 * Nacos 序列化运行时异常。
 *
 * <p>在对象序列化为 JSON/Protobuf 等格式失败时抛出，错误码为 {@link com.alibaba.nacos.api.common.Constants.Exception#SERIALIZE_ERROR_CODE}。</p>
 *
 * @author yangyi
 */
public class NacosSerializationException extends NacosRuntimeException {
    
    private static final long serialVersionUID = -4308536346316915612L;
    
    private static final String DEFAULT_MSG = "Nacos serialize failed. ";
    
    private static final String MSG_FOR_SPECIFIED_CLASS = "Nacos serialize for class [%s] failed. ";
    
    /** 序列化失败的目标类型（若已知）。 */
    private Class<?> serializedClass;
    
    /** 构造默认序列化异常。 */
    public NacosSerializationException() {
        super(SERIALIZE_ERROR_CODE);
    }
    
    /**
     * 构造指定目标类的序列化异常。
     *
     * @param serializedClass 序列化失败的目标类
     */
    public NacosSerializationException(Class<?> serializedClass) {
        super(SERIALIZE_ERROR_CODE,
            String.format(MSG_FOR_SPECIFIED_CLASS, serializedClass.getName()));
        this.serializedClass = serializedClass;
    }
    
    /**
     * 构造带根因的序列化异常。
     *
     * @param throwable 根因异常
     */
    public NacosSerializationException(Throwable throwable) {
        super(SERIALIZE_ERROR_CODE, DEFAULT_MSG, throwable);
    }
    
    /**
     * 构造指定目标类与根因的序列化异常。
     *
     * @param serializedClass 序列化失败的目标类
     * @param throwable       根因异常
     */
    public NacosSerializationException(Class<?> serializedClass, Throwable throwable) {
        super(SERIALIZE_ERROR_CODE,
            String.format(MSG_FOR_SPECIFIED_CLASS, serializedClass.getName()), throwable);
        this.serializedClass = serializedClass;
    }
    
    /** 获取序列化失败的目标类（可能为 {@code null}）。 */
    public Class<?> getSerializedClass() {
        return serializedClass;
    }
}
