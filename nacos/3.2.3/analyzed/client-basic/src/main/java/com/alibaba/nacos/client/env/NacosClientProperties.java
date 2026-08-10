/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.env;

import java.util.Properties;

/**
 * NacosClientProperties interface. include all the properties from jvm args, system environment, default setting. more
 * details you can see https://github.com/alibaba/nacos/issues/8622
 * <p>Nacos 客户端统一配置门面：聚合 JVM 参数、系统环境变量与内存属性，支持按 {@link SourceType} 定向读取及类型化转换（Boolean/Integer/Long）。子实例通过 {@link #derive()} 继承父级配置并叠加本地覆盖。</p>
 *
 * @author onewe
 */
public interface NacosClientProperties {
    
    /**
     * all the NacosClientProperties object must be created by PROTOTYPE, so child NacosClientProperties can read
     * properties from the PROTOTYPE. it looks like this: |-PROTOTYPE----------------> ip=127.0.0.1
     * |---|-child1---------------> port=6379 if you search key called "port" from child1, certainly you will get 6379
     * if you search key called "ip" from child1, you will get 127.0.0.1. because the child can read properties from
     * parent NacosClientProperties
     * <p>全局原型实例，指向 {@link SearchableProperties#INSTANCE}；派生实例可向上回溯读取未在本层显式设置的键。</p>
     */
    NacosClientProperties PROTOTYPE = SearchableProperties.INSTANCE;
    
    /**
     * 按检索顺序读取字符串属性；未命中返回 {@code null}。
     *
     * @param key 属性键
     * @return 字符串值或 {@code null}
     */
    String getProperty(String key);
    
    /**
     * 按检索顺序读取字符串属性；未命中时返回默认值。
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 字符串值或 {@code defaultValue}
     */
    String getProperty(String key, String defaultValue);
    
    /**
     * 仅从指定属性源读取字符串值，不参与全局检索顺序。
     *
     * @param source 属性源类型
     * @param key    属性键
     * @return 字符串值或 {@code null}
     * @see SourceType
     */
    String getPropertyFrom(SourceType source, String key);
    
    /**
     * 导出指定属性源的全部键值快照。
     *
     * @param source 属性源类型
     * @return 该来源的 {@link Properties} 快照
     * @see SourceType
     */
    Properties getProperties(SourceType source);
    
    /**
     * 读取布尔属性；未命中或无法解析时返回 {@code null}。
     *
     * @param key 属性键
     * @return 布尔值或 {@code null}
     */
    Boolean getBoolean(String key);
    
    /**
     * 读取布尔属性；未命中时使用默认值。
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 布尔值或 {@code defaultValue}
     */
    Boolean getBoolean(String key, Boolean defaultValue);
    
    /**
     * 读取整型属性；未命中或解析失败时返回 {@code null}。
     *
     * @param key 属性键
     * @return 整型值或 {@code null}
     */
    Integer getInteger(String key);
    
    /**
     * 读取整型属性；未命中时使用默认值。
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 整型值或 {@code defaultValue}
     */
    Integer getInteger(String key, Integer defaultValue);
    
    /**
     * 读取长整型属性；未命中或解析失败时返回 {@code null}。
     *
     * @param key 属性键
     * @return 长整型值或 {@code null}
     */
    Long getLong(String key);
    
    /**
     * 读取长整型属性；未命中时使用默认值。
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 长整型值或 {@code defaultValue}
     */
    Long getLong(String key, Long defaultValue);
    
    /**
     * 在本层 {@link SourceType#PROPERTIES} 内存属性中写入或覆盖键值。
     *
     * @param key   属性键
     * @param value 属性值
     */
    void setProperty(String key, String value);
    
    /**
     * 批量合并属性到本层内存属性表（后者覆盖同名键）。
     *
     * @param properties 待合并的 {@link Properties}
     */
    void addProperties(Properties properties);
    
    /**
     * 判断任一属性源是否包含指定 key（按检索顺序短路）。
     *
     * @param key 待检测的键
     * @return 存在返回 {@code true}
     */
    boolean containsKey(String key);
    
    /**
     * 按检索顺序合并全部属性源，导出完整 {@link Properties} 快照。
     *
     * @return 合并后的属性表
     */
    Properties asProperties();
    
    /**
     * 派生子配置实例：继承当前层属性并可继续本地覆盖，不影响原型。
     *
     * @return 新的 {@link NacosClientProperties} 实例
     */
    NacosClientProperties derive();
    
    /**
     * 从 {@link #PROTOTYPE} 派生并预填充一批属性（常用于客户端初始化）。
     *
     * @param properties 初始属性
     * @return 已合并初始属性的新实例
     */
    NacosClientProperties derive(Properties properties);
}
