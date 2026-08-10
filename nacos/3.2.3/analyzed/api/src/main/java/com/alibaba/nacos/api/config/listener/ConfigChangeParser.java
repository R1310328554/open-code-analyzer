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

package com.alibaba.nacos.api.config.listener;

import com.alibaba.nacos.api.config.ConfigChangeItem;

import java.io.IOException;
import java.util.Map;

/**
 * 配置变更内容解析器 SPI。
 *
 * <p>按配置类型（如 properties、yaml）对比新旧内容，产出 {@link ConfigChangeItem} 映射，
 * 供 {@link com.alibaba.nacos.api.config.ConfigChangeEvent} 使用。</p>
 *
 * @author rushsky518
 */
public interface ConfigChangeParser {
    
    /**
     * 判断是否负责解析指定配置类型。
     *
     * @param type 配置类型标识
     * @return 若本解析器可处理该类型则返回 {@code true}
     */
    boolean isResponsibleFor(String type);
    
    /**
     * 对比新旧配置内容并解析变更项。
     *
     * @param oldContent 变更前内容
     * @param newContent 变更后内容
     * @param type       配置类型
     * @return 键到 {@link ConfigChangeItem} 的映射
     * @throws IOException 解析 IO 异常
     */
    Map<String, ConfigChangeItem> doParse(String oldContent, String newContent, String type)
        throws IOException;
}
