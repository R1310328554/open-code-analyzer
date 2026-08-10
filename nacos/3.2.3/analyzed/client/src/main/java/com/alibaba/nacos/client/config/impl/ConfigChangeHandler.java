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
import com.alibaba.nacos.api.config.listener.ConfigChangeParser;
import com.alibaba.nacos.common.spi.NacosServiceLoader;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 配置变更解析处理器（单例）。
 *
 * <p>通过 SPI 加载 {@link ConfigChangeParser} 实现，并按配置类型选择解析器，对比新旧内容生成 {@link ConfigChangeItem} 映射。</p>
 *
 * @author rushsky518
 */
public class ConfigChangeHandler {
    
    private static class ConfigChangeHandlerHolder {
        
        private static final ConfigChangeHandler INSTANCE = new ConfigChangeHandler();
    }
    
    private ConfigChangeHandler() {
        this.parserList = new LinkedList<>();
        
        Collection<ConfigChangeParser> loader = NacosServiceLoader.load(ConfigChangeParser.class);
        this.parserList.addAll(loader);
        
        this.parserList.add(new PropertiesChangeParser());
        this.parserList.add(new YmlChangeParser());
    }
    
    public static ConfigChangeHandler getInstance() {
        return ConfigChangeHandlerHolder.INSTANCE;
    }
    
    /**
     * 解析配置变更项。
     *
     * @param oldContent 变更前内容
     * @param newContent 变更后内容
     * @param type       配置类型（如 properties、yaml）
     * @return 变更项映射，无匹配解析器时返回空映射
     * @throws IOException 解析 IO 异常
     */
    public Map<String, ConfigChangeItem> parseChangeData(String oldContent, String newContent,
        String type) throws IOException {
        for (ConfigChangeParser changeParser : this.parserList) {
            if (changeParser.isResponsibleFor(type)) {
                return changeParser.doParse(oldContent, newContent, type);
            }
        }
        
        return Collections.emptyMap();
    }
    
    /** 已注册的变更解析器链，按优先级依次匹配。 */
    private final List<ConfigChangeParser> parserList;
    
}
