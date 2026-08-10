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

package com.alibaba.nacos.client.config.impl;

import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.common.utils.StringUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.ComposerException;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * YAML 格式配置变更解析器。
 *
 * <p>使用 SnakeYAML 将嵌套 YAML 展平为点分键路径，再比对新旧映射生成 {@link ConfigChangeItem}。
 * 仅支持基础 Java 数据类型；自定义类型需使用 {@link com.alibaba.nacos.api.config.listener.Listener} 监听整段配置自行解析。</p>
 *
 * @author rushsky518
 */
public class YmlChangeParser extends AbstractConfigChangeParser {
    
    /** SnakeYAML 无法确定构造器时的错误信息前缀。 */
    private static final String INVALID_CONSTRUCTOR_ERROR_INFO =
        "could not determine a constructor for the tag";
    
    /** 本解析器对应的配置类型标识。 */
    private static final String CONFIG_TYPE = "yaml";
    
    /** 注册 yaml 类型解析器。 */
    public YmlChangeParser() {
        super(CONFIG_TYPE);
    }
    
    /**
     * 解析 YAML 新旧内容并提取变更项。
     *
     * @param oldContent 变更前配置文本
     * @param newContent 变更后配置文本
     * @param type       配置类型（yaml）
     * @return 展平键到 {@link ConfigChangeItem} 的映射
     */
    @Override
    public Map<String, ConfigChangeItem> doParse(String oldContent, String newContent,
        String type) {
        Map<String, Object> oldMap = Collections.emptyMap();
        Map<String, Object> newMap = Collections.emptyMap();
        try {
            Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
            if (StringUtils.isNotBlank(oldContent)) {
                oldMap = yaml.load(oldContent);
                oldMap = getFlattenedMap(oldMap);
            }
            if (StringUtils.isNotBlank(newContent)) {
                newMap = yaml.load(newContent);
                newMap = getFlattenedMap(newMap);
            }
        } catch (MarkedYAMLException e) {
            handleYamlException(e);
        }
        
        return filterChangeData(oldMap, newMap);
    }
    
    /**
     * 将 YAML 解析异常转换为客户端可理解的运行时异常。
     *
     * @param e SnakeYAML 标记异常
     */
    private void handleYamlException(MarkedYAMLException e) {
        if (e.getMessage().startsWith(INVALID_CONSTRUCTOR_ERROR_INFO)
            || e instanceof ComposerException) {
            throw new NacosRuntimeException(NacosException.INVALID_PARAM,
                "AbstractConfigChangeListener only support basic java data type for yaml. If you want to listen "
                    + "key changes for custom classes, please use `Listener` to listener whole yaml configuration and parse it by yourself.",
                e);
        }
        throw e;
    }
    
    /**
     * 将嵌套 Map 展平为点分键路径映射。
     *
     * @param source 原始 YAML 根对象
     * @return 展平后的键值映射
     */
    private Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>(128);
        buildFlattenedMap(result, source, null);
        return result;
    }
    
    /**
     * 递归构建展平映射，支持 Map、Collection 与标量值。
     *
     * @param result 输出映射
     * @param source 当前层级源 Map
     * @param path   父级键路径前缀；根节点为 null
     */
    private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source,
        String path) {
        for (Iterator<Map.Entry<String, Object>> itr = source.entrySet().iterator(); itr
            .hasNext();) {
            Map.Entry<String, Object> e = itr.next();
            String key = e.getKey();
            if (StringUtils.isNotBlank(path)) {
                // 数组下标键以 [ 开头，直接拼接路径
                if (e.getKey().startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + '.' + key;
                }
            }
            if (e.getValue() instanceof String) {
                result.put(key, e.getValue());
            } else if (e.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) e.getValue();
                buildFlattenedMap(result, map, key);
            } else if (e.getValue() instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) e.getValue();
                if (collection.isEmpty()) {
                    result.put(key, "");
                } else {
                    int count = 0;
                    for (Object object : collection) {
                        buildFlattenedMap(result,
                            Collections.singletonMap("[" + (count++) + "]", object), key);
                    }
                }
            } else {
                result.put(key, (e.getValue() != null ? e.getValue() : ""));
            }
        }
    }
    
}
