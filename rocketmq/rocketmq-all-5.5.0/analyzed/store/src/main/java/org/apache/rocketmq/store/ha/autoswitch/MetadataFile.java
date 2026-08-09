/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.store.ha.autoswitch;

import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.UtilAll;

import java.io.File;

/**
 * 自动切换元数据文件抽象基类：定义编码、持久化与内存清理契约。
 */
public abstract class MetadataFile {

    /** 元数据文件在磁盘上的路径。 */
    protected String filePath;

    /** 将内存中的元数据编码为字符串。 */
    public abstract String encodeToStr();

    /** 从字符串解码并加载元数据到内存。 */
    public abstract void decodeFromStr(String dataStr);

    /** 判断元数据是否已成功加载。 */
    public abstract boolean isLoaded();

    /** 清空内存中的元数据字段。 */
    public abstract void clearInMem();

    /** 先删除旧文件，再将编码结果写入磁盘。 */
    public void writeToFile() throws Exception {
        UtilAll.deleteFile(new File(filePath));
        MixAll.string2File(encodeToStr(), this.filePath);
    }

    /** 从磁盘读取文件内容并解码到内存。 */
    public void readFromFile() throws Exception {
        String dataStr = MixAll.file2String(filePath);
        decodeFromStr(dataStr);
    }
    /** 判断元数据文件是否存在于磁盘。 */
    public boolean fileExists() {
        File file = new File(filePath);
        return file.exists();
    }

    /** 清空内存并删除磁盘上的元数据文件。 */
    public void clear() {
        clearInMem();
        UtilAll.deleteFile(new File(filePath));
    }

    /** 返回元数据文件路径。 */
    public String getFilePath() {
        return filePath;
    }
}
