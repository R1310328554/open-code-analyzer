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

import java.util.Properties;

/**
 * 快照单文件的元数据容器，基于 {@link Properties} 存储键值对（如 file-name）。
 *
 * Meta information for the snapshot file.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class LocalFileMeta {
    
    /** 快照文件元数据属性表。 */
    private final Properties fileMeta;
    
    /** 创建空元数据。 */
    public LocalFileMeta() {
        this.fileMeta = new Properties();
    }
    
    /** 使用已有 Properties 包装。 */
    public LocalFileMeta(Properties properties) {
        this.fileMeta = properties;
    }
    
    /** 追加键值并返回 this，支持链式调用。 */
    public LocalFileMeta append(Object key, Object value) {
        fileMeta.put(key, value);
        return this;
    }
    
    /** 按字符串键读取属性值。 */
    public Object get(String key) {
        return fileMeta.getProperty(key);
    }
    
    /** 返回底层 Properties 引用。 */
    public Properties getFileMeta() {
        return fileMeta;
    }
    
    /** 调试用字符串表示。 */
    @Override
    public String toString() {
        return "LocalFileMeta{" + "fileMeta=" + fileMeta + '}';
    }
}
