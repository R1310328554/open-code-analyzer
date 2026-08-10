/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.service.repository;

import com.alibaba.nacos.ai.model.AiResourceVersion;
import com.alibaba.nacos.api.model.Page;

/**
 * Persist service for ai_resource_version.
 * <p>{@code ai_resource_version} 表持久化服务接口，管理版本行 CRUD、存储 JSON 与下载计数。</p>
 *
 * @author nacos
 * @since 3.2.0
 */
public interface AiResourceVersionPersistService {
    
    long insert(AiResourceVersion version);
    
    AiResourceVersion find(String namespaceId, String name, String type, String version);
    
    Page<AiResourceVersion> list(String namespaceId, String name, String type, String status,
        int pageNo, int pageSize);
    
    int delete(String namespaceId, String name, String type, String version);
    
    int deleteByName(String namespaceId, String name);
    
    int deleteByNameAndType(String namespaceId, String name, String type);
    
    int updateStatus(String namespaceId, String name, String type, String version, String status);
    
    int updateStorage(String namespaceId, String name, String type, String version, String storage);
    
    int updateStorageAndDesc(String namespaceId, String name, String type, String version,
        String storage, String desc);
    
    /**
     * Update only the {@code contentMd5} entry inside the {@code storage} JSON column. The provider,
     * scope and files entries are preserved by performing a read-merge-write on the existing row.
     * <p>仅更新 storage JSON 中的 {@code contentMd5} 字段，读-合并-写保留 provider/scope/files；用于监听器路径为历史版本回填 MD5。</p>
     *
     * <p>Used by the skill listener path to back-fill the content MD5 for historical versions that
     * were published before the listener feature shipped.
     *
     * @param namespaceId namespace ID
     * @param name        resource name
     * @param type        resource type
     * @param version     version string
     * @param contentMd5  content MD5 to write into {@code storage.contentMd5}
     * @return number of rows affected; {@code 0} when the version row is missing
     */
    int updateStorageMd5(String namespaceId, String name, String type, String version,
        String contentMd5);
    
    int updatePublishPipelineInfo(String namespaceId, String name, String type, String version,
        String publishPipelineInfo);
    
    /**
     * Increment download count for a specific version.
     *
     * @param namespaceId namespace ID
     * @param name        resource name
     * @param type        resource type
     * @param version     version string
     * @param increment   amount to add
     * @return number of rows affected
      * <p>Nacos AI 模块；详见上方英文说明。</p>
     */
    int incrementDownloadCount(String namespaceId, String name, String type, String version,
        long increment);
}
