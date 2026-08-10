/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.ai.pipeline.model;

/**
 * 资源文件内容模型，表示存储中的单个文本文件。
 *
 * @author mosong.lp
 * @since 3.2.0
 */
public class ResourceFileContent {
    
    /**
     * 文件相对路径，例如 {@code "templates/config_check.json"}、{@code "SKILL.md"}。
     */
    private String filePath;
    
    /**
     * 文件文本内容。
     */
    private String content;
    
    /** 无参构造。 */
    public ResourceFileContent() {
    }
    
    /**
     * 构造指定路径与内容的文件模型。
     *
     * @param filePath 文件路径
     * @param content  文本内容
     */
    public ResourceFileContent(String filePath, String content) {
        this.filePath = filePath;
        this.content = content;
    }
    
    /** @return 文件路径 */
    public String getFilePath() {
        return filePath;
    }
    
    /** @param filePath 文件路径 */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /** @return 文件文本内容 */
    public String getContent() {
        return content;
    }
    
    /** @param content 文件文本内容 */
    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public String toString() {
        return "ResourceFileContent{filePath='" + filePath + "'}";
    }
}
