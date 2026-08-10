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

import com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker.None;
import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Http;
import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Mysql;
import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Tcp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.io.Serializable;

/**
 * 健康检查器抽象基类，定义集群级探测策略的多态序列化结构。
 *
 * <p>Jackson 按 {@code type} 字段多态反序列化为 {@link Http}、{@link Mysql}、{@link Tcp} 或 {@link None} 等实现。</p>
 *
 * @author nkorange
 */
@JsonTypeInfo(use = Id.NAME, property = "type", defaultImpl = None.class)
@JsonSubTypes({@JsonSubTypes.Type(name = Http.TYPE, value = Http.class),
    @JsonSubTypes.Type(name = Mysql.TYPE, value = Mysql.class),
    @JsonSubTypes.Type(name = Tcp.TYPE, value = Tcp.class),
    @JsonSubTypes.Type(name = None.TYPE, value = None.class)})
public abstract class AbstractHealthChecker implements Cloneable, Serializable {
    
    private static final long serialVersionUID = 3848305577423336421L;
    
    /** 健康检查类型标识，序列化时写入 JSON 的 {@code type} 字段。 */
    @JsonIgnore
    protected final String type;
    
    /**
     * 子类构造时传入类型名。
     *
     * @param type 检查器类型标识
     */
    protected AbstractHealthChecker(String type) {
        this.type = type;
    }
    
    /** 获取健康检查类型标识。 */
    public String getType() {
        return type;
    }
    
    /**
     * 克隆当前检查器全部字段到新实例。
     *
     * @return 字段完全一致的新实例
     * @throws CloneNotSupportedException 不支持克隆时抛出
     */
    @Override
    public abstract AbstractHealthChecker clone() throws CloneNotSupportedException;
    
    /**
     * 默认空实现：不进行任何健康检查。
     */
    public static class None extends AbstractHealthChecker {
        
        /** 类型常量 {@code NONE}。 */
        public static final String TYPE = "NONE";
        
        private static final long serialVersionUID = -760631831097384737L;
        
        /** 构造 NONE 类型检查器。 */
        public None() {
            super(TYPE);
        }
        
        @Override
        public AbstractHealthChecker clone() throws CloneNotSupportedException {
            return new None();
        }
    }
}
