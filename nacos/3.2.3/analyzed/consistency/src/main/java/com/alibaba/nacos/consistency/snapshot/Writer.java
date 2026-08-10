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

package com.alibaba.nacos.consistency.snapshot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 快照保存阶段的写入器：维护待落盘文件列表及各自 {@link LocalFileMeta}。
 *
 * Snapshot write interface.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class Writer {
    
    /** 待写入快照的文件名到元数据映射。 */
    private final Map<String, LocalFileMeta> files = new HashMap<>();
    
    /** 快照目标根路径。 */
    private String path;
    
    /** 指定快照根路径构造写入器。 */
    public Writer(String path) {
        this.path = path;
    }
    
    /** 返回快照根路径。 */
    public String getPath() {
        return path;
    }
    
    /**
     * 添加仅含默认 file-name 元数据的快照文件。
     * Adds a snapshot file without metadata.
     *
     * @param fileName file name
     * @return true on success
     */
    public boolean addFile(final String fileName) {
        files.put(fileName, new LocalFileMeta().append("file-name", fileName));
        return true;
    }
    
    /**
     * 添加带自定义 {@link LocalFileMeta} 的快照文件。
     * Adds a snapshot file with metadata.
     *
     * @param fileName file name
     * @return true on success
     */
    public boolean addFile(final String fileName, final LocalFileMeta meta) {
        files.put(fileName, meta);
        return true;
    }
    
    /**
     * 从待写入列表移除指定快照文件。
     * Remove a snapshot file.
     *
     * @param fileName file name
     * @return true on success
     */
    public boolean removeFile(final String fileName) {
        files.remove(fileName);
        return true;
    }
    
    /** 返回当前待写入文件的不可变视图。 */
    public Map<String, LocalFileMeta> listFiles() {
        return Collections.unmodifiableMap(files);
    }
    
}
