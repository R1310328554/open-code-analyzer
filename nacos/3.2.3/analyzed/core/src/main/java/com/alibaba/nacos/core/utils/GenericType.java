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

package com.alibaba.nacos.core.utils;

import com.alibaba.nacos.common.utils.Preconditions;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * 泛型类型捕获工具基类，子类声明 {@code extends GenericType<实际类型>} 即可在运行时获取 {@code T}。
 *
 * <p>类似 Guava {@code TypeToken}，通过匿名子类保留泛型签名。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class GenericType<T> {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = -2103808581228167629L;
    
    /** 捕获到的运行时泛型 {@link Type}。 */
    private final Type runtimeType;
    
    /** 从匿名子类的 {@link ParameterizedType} 中提取第一个类型参数。 */
    final Type capture() {
        Type superclass = getClass().getGenericSuperclass();
        Preconditions.checkArgument(superclass instanceof ParameterizedType,
            "%s isn't parameterized", superclass);
        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }
    
    /** 构造时捕获泛型参数，若为 {@link TypeVariable} 则抛出异常。 */
    protected GenericType() {
        this.runtimeType = capture();
        if (runtimeType instanceof TypeVariable) {
            throw new IllegalArgumentException("runtimeType must be ParameterizedType Class");
        }
    }
    
    /** 返回捕获到的泛型 {@link Type}。 */
    public final Type getType() {
        return runtimeType;
    }
}
