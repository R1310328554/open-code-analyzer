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

package com.alibaba.nacos.api.naming.pojo.healthcheck.impl;

import com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker;

import java.util.Objects;

/**
 * TCP 协议健康检查器实现，通过尝试建立 TCP 连接判定实例是否存活。
 *
 * <p>无额外配置项，类型标识为 {@link #TYPE}。</p>
 *
 * @author yangyi
 */
public class Tcp extends AbstractHealthChecker {
    
    /** 健康检查类型常量 {@code TCP}。 */
    public static final String TYPE = "TCP";
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = -9116042038157496294L;
    
    /** 构造 TCP 类型健康检查器。 */
    public Tcp() {
        super(TYPE);
    }
    
    /** 基于 {@link #TYPE} 计算哈希码。 */
    @Override
    public int hashCode() {
        return Objects.hash(TYPE);
    }
    
    /** 判断是否为 {@link Tcp} 实例（类型相同即相等）。 */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Tcp;
    }
    
    /** 克隆当前 TCP 健康检查器。 */
    @Override
    public Tcp clone() throws CloneNotSupportedException {
        return new Tcp();
    }
}
