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

import java.util.List;

/**
 * 以多文件形式存储的 AI 资源发布流水线上下文。
 *
 * <p>在通用元数据之外携带文件列表或懒加载器，供 Skill、AgentSpec 等多文件资源
 * 的发布审核插件读取完整内容。</p>
 *
 * @author nacos
 */
public class ResourceFilesPipelineContext extends PublishPipelineContext {
    
    /**
     * 文件内容懒加载函数式接口；首次访问 {@link #getFiles()} 时触发加载。
     */
    @FunctionalInterface
    public interface FilesLoader {
        
        /** @return 从存储加载的文件内容列表 */
        List<ResourceFileContent> load();
    }
    
    /**
     * 已从存储加载的资源文件内容列表。
     */
    private List<ResourceFileContent> files;
    
    /** 懒加载器；当 {@link #files} 尚未填充时由 {@link #getFiles()} 调用。 */
    private FilesLoader filesLoader;
    
    /**
     * 获取文件列表；若尚未加载且配置了 {@link #filesLoader}，则先执行懒加载。
     *
     * @return 资源文件内容列表，可能为 {@code null}
     */
    public List<ResourceFileContent> getFiles() {
        if (files == null && filesLoader != null) {
            files = filesLoader.load();
        }
        return files;
    }
    
    /** @param files 资源文件内容列表 */
    public void setFiles(List<ResourceFileContent> files) {
        this.files = files;
    }
    
    /** @return 文件懒加载器 */
    public FilesLoader getFilesLoader() {
        return filesLoader;
    }
    
    /** @param filesLoader 文件懒加载器 */
    public void setFilesLoader(FilesLoader filesLoader) {
        this.filesLoader = filesLoader;
    }
}
