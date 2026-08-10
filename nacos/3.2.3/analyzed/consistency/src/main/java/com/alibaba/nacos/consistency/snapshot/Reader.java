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
import java.util.Map;

/**
 * 快照加载阶段的只读视图：暴露快照根路径及文件名到 {@link LocalFileMeta} 的映射。
 *
 * Read the snapshot file interface.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class Reader {
    
    /** 快照根目录路径。 */
    private final String path;
    
    /** 文件名到元数据的不可变映射。 */
    private final Map<String, LocalFileMeta> allFiles;
    
    /** 构造快照读取器，allFiles 会被包装为不可变 Map。 */
    public Reader(String path, Map<String, LocalFileMeta> allFiles) {
        this.path = path;
        this.allFiles = Collections.unmodifiableMap(allFiles);
    }
    
    /** 返回快照根路径。 */
    public String getPath() {
        return path;
    }
    
    /** 列出全部快照文件及其元数据。 */
    public Map<String, LocalFileMeta> listFiles() {
        return allFiles;
    }
    
    /** 按文件名获取元数据，不存在时返回 null。 */
    public LocalFileMeta getFileMeta(String fileName) {
        return allFiles.get(fileName);
    }
}
