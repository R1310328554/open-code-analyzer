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
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

/**
 * Properties 格式配置变更解析器。
 *
 * <p>将新旧 properties 文本加载为 {@link Properties}，再委托父类 {@link AbstractConfigChangeParser}
 * 比对键值差异并生成 {@link ConfigChangeItem} 映射。</p>
 *
 * @author rushsky518
 */
public class PropertiesChangeParser extends AbstractConfigChangeParser {
    
    /** 本解析器对应的配置类型标识。 */
    private static final String CONFIG_TYPE = "properties";
    
    /** 注册 properties 类型解析器。 */
    public PropertiesChangeParser() {
        super(CONFIG_TYPE);
    }
    
    /**
     * 解析 properties 新旧内容并提取变更项。
     *
     * @param oldContent 变更前配置文本
     * @param newContent 变更后配置文本
     * @param type       配置类型（properties）
     * @return 键到 {@link ConfigChangeItem} 的映射
     * @throws IOException 加载 properties 失败时抛出
     */
    @Override
    public Map<String, ConfigChangeItem> doParse(String oldContent, String newContent, String type)
        throws IOException {
        Properties oldProps = new Properties();
        Properties newProps = new Properties();
        
        if (StringUtils.isNotBlank(oldContent)) {
            oldProps.load(new StringReader(oldContent));
        }
        if (StringUtils.isNotBlank(newContent)) {
            newProps.load(new StringReader(newContent));
        }
        
        return filterChangeData(oldProps, newProps);
    }
}
