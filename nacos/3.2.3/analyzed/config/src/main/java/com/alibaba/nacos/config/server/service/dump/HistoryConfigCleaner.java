/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.service.dump;

/**
 * 历史配置清理 SPI 接口：由 {@link HistoryConfigCleanerManager} 按名称加载实现。
 * The interface History config cleaner.
 * @author Sunrisea
 */
public interface HistoryConfigCleaner {
    
    /**
     * 执行历史配置清理逻辑（过期记录删除等）。
     * Clean history config.
     */
    public void cleanHistoryConfig();
    
    /**
     * 返回清理器唯一名称，用于配置项 {@code nacos.config.history.clear.name} 匹配。
     * Gets name.
     *
     * @return the name
     */
    public String getName();
}
