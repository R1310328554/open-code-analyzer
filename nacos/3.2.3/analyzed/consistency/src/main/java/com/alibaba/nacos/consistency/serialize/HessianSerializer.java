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

package com.alibaba.nacos.consistency.serialize;

import com.alibaba.nacos.api.exception.runtime.NacosDeserializationException;
import com.alibaba.nacos.common.utils.ByteUtils;
import com.alibaba.nacos.consistency.Serializer;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 基于 Hessian2 的 {@link com.alibaba.nacos.consistency.Serializer} 实现，用于一致性日志与快照的二进制序列化。
 *
 * Serializer implement by hessian.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("all")
public class HessianSerializer implements Serializer {
    
    /** 序列化器注册名。 */
    private static final String NAME = "Hessian";
    
    /** 带白名单的 Hessian 工厂，限制可反序列化类型。 */
    private SerializerFactory serializerFactory = new NacosHessianSerializerFactory();
    
    /** 使用默认 {@link NacosHessianSerializerFactory} 构造。 */
    public HessianSerializer() {
    }
    
    /** 反序列化字节数组。 */
    @Override
    public <T> T deserialize(byte[] data) {
        return deseiralize0(data);
    }
    
    /** 反序列化字节数组。 */
    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        T result = deserialize(data);
        if (result == null) {
            return null;
        }
        if (cls.isAssignableFrom(result.getClass())) {
            return result;
        }
        throw new NacosDeserializationException(cls, new ClassCastException(
            "%s cannot be cast to %s".format(result.getClass().getCanonicalName(),
                cls.getCanonicalName())));
    }
    
    /** 反序列化字节数组。 */
    @Override
    public <T> T deserialize(byte[] data, Type type) {
        return deserialize(data);
    }
    
    /** Hessian2 反序列化核心逻辑；空数组返回 null。 */
    private <T> T deseiralize0(byte[] data) {
        if (ByteUtils.isEmpty(data)) {
            return null;
        }
        
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(data));
        input.setSerializerFactory(serializerFactory);
        Object resultObject;
        try {
            resultObject = input.readObject();
            input.close();
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred when Hessian serializer decode!", e);
        }
        return (T) resultObject;
    }
    
    /** 序列化对象为字节数组。 */
    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(byteArray);
        output.setSerializerFactory(serializerFactory);
        try {
            output.writeObject(obj);
            output.close();
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred when Hessian serializer encode!", e);
        }
        
        return byteArray.toByteArray();
    }
    
    /** 返回序列化器名称。 */
    @Override
    public String name() {
        return NAME;
    }
    
}
