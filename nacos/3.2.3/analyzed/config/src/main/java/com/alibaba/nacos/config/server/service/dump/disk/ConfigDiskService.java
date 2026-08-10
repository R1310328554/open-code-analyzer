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

package com.alibaba.nacos.config.server.service.dump.disk;

import java.io.IOException;

/**
 * 配置本地磁盘持久化 SPI：抽象正式/灰度配置的读写删与全量清空。
 * config disk service.
 *
 * @author zunfei.lzf
 */
public interface ConfigDiskService {
    
    /**
     * 将正式配置内容写入本地磁盘。
     * Save configuration information to disk.
     *
     * @param dataId  dataId.
     * @param group   group.
     * @param tenant  tenant.
     * @param content content.
     * @throws IOException io exception.
     */
    void saveToDisk(String dataId, String group, String tenant, String content) throws IOException;
    
    /**
     * 将灰度配置内容写入本地磁盘。
     * Save gray information to disk.
     *
     * @param dataId  dataId.
     * @param group   group.
     * @param tenant  tenant.
     * @param grayName grayName.
     * @param content content.
     * @throws IOException io exception.
     */
    void saveGrayToDisk(String dataId, String group, String tenant, String grayName, String content)
        throws IOException;
    
    /**
     * 删除磁盘上的灰度配置文件。
     * Deletes gray configuration files on disk.
     *
     * @param dataId dataId.
     * @param group  group.
     * @param tenant tenant.
     * @param grayName grayName.
     */
    void removeConfigInfo4Gray(String dataId, String group, String tenant, String grayName);
    
    /**
     * 读取服务端灰度缓存文件内容，不存在时返回 null。
     * Returns the content of the gray cache file in server.
     *
     * @param dataId dataId.
     * @param group  group.
     * @param tenant tenant.
     * @param grayName grayName.
     * @return gray content, null if not exist.
     * @throws IOException io exception.
     */
    String getGrayContent(String dataId, String group, String tenant, String grayName)
        throws IOException;
    
    /**
     * 删除磁盘上的正式配置文件。
     * Deletes configuration files on disk.
     *
     * @param dataId dataId.
     * @param group  group.
     * @param tenant tenant.
     */
    void removeConfigInfo(String dataId, String group, String tenant);
    
    /**
     * 读取服务端正式配置缓存文件内容，不存在时返回 null。
     * Returns the content of the  cache file in server.
     *
     * @param dataId dataId.
     * @param group  group.
     * @param tenant tenant.
     * @return content null if not exist.
     * @throws IOException io exception.
     */
    String getContent(String dataId, String group, String tenant) throws IOException;
    
    /**
     * 清空全部正式配置磁盘文件（启动全量 dump 前调用）。
     * Clear all config file.
     */
    void clearAll();
    
    /**
     * 清空全部灰度配置磁盘文件。
     * Clear all gray config file.
     */
    void clearAllGray();
    
}
