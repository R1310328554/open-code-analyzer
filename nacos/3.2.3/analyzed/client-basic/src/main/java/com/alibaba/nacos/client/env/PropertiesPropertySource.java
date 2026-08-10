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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

/**
 * 内存 {@link Properties} 属性源，支持链式父级以实现配置继承与覆盖。
 * <p>类型为 {@link SourceType#PROPERTIES}，通常位于检索顺序首位；子实例通过 {@link #PropertiesPropertySource(PropertiesPropertySource)} 指向父级。</p>
 */
class PropertiesPropertySource extends AbstractPropertySource {
    
    /** 本层持有的可变属性表 */
    private final Properties properties = new Properties();
    
    /** 父级属性源；{@code null} 表示根节点 */
    private final PropertiesPropertySource parent;
    
    /** 创建无父级的根属性源。 */
    PropertiesPropertySource() {
        this.parent = null;
    }
    
    /** 创建指向指定父级的派生属性源。 */
    PropertiesPropertySource(PropertiesPropertySource parent) {
        this.parent = parent;
    }
    
    @Override
    SourceType getType() {
        return SourceType.PROPERTIES;
    }
    
    @Override
    String getProperty(String key) {
        return getProperty(this, key);
    }
    
    /** 自当前节点向父级递归查找 key，先命中本层再向上。 */
    private String getProperty(PropertiesPropertySource propertiesPropertySource, String key) {
        final String value = propertiesPropertySource.properties.getProperty(key);
        if (value != null) {
            return value;
        }
        final PropertiesPropertySource parent = propertiesPropertySource.parent;
        if (parent == null) {
            return null;
        }
        return getProperty(parent, key);
    }
    
    @Override
    boolean containsKey(String key) {
        return containsKey(this, key);
    }
    
    /** 递归判断 key 是否存在于当前链路的任一节点。 */
    boolean containsKey(PropertiesPropertySource propertiesPropertySource, String key) {
        final boolean exist = propertiesPropertySource.properties.containsKey(key);
        if (exist) {
            return true;
        }
        final PropertiesPropertySource parent = propertiesPropertySource.parent;
        if (parent == null) {
            return false;
        }
        return containsKey(parent, key);
    }
    
    @Override
    Properties asProperties() {
        List<Properties> propertiesList = new ArrayList<>(8);
        
        propertiesList = lookForProperties(this, propertiesList);
        
        Properties ret = new Properties();
        final ListIterator<Properties> iterator =
            propertiesList.listIterator(propertiesList.size());
        while (iterator.hasPrevious()) {
            final Properties properties = iterator.previous();
            ret.putAll(properties);
        }
        return ret;
    }
    
    /** 自底向上收集链路上各层 {@link Properties}，供 {@link #asProperties()} 按父先子后合并。 */
    List<Properties> lookForProperties(PropertiesPropertySource propertiesPropertySource,
        List<Properties> propertiesList) {
        propertiesList.add(propertiesPropertySource.properties);
        final PropertiesPropertySource parent = propertiesPropertySource.parent;
        if (parent == null) {
            return propertiesList;
        }
        return lookForProperties(parent, propertiesList);
    }
    
    /** 线程安全地在本层写入单个键值（不修改父级）。 */
    synchronized void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /** 线程安全地批量合并属性到本层。 */
    synchronized void addProperties(Properties source) {
        properties.putAll(source);
    }
}
