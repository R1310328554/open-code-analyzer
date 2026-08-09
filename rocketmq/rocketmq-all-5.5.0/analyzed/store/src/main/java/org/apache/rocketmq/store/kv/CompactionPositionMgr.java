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
package org.apache.rocketmq.store.kv;

import org.apache.rocketmq.common.ConfigManager;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 压缩进度管理器：持久化各 Topic-Queue 的已压缩偏移检查点。
 */
public class CompactionPositionMgr extends ConfigManager {

    /** 检查点文件名。 */
    public static final String CHECKPOINT_FILE = "position-checkpoint";

    /** 压缩数据根目录路径。 */
    private transient String compactionPath;
    /** 检查点文件完整路径。 */
    private transient String checkpointFileName;

    /** Topic_QueueId 到已压缩偏移的映射。 */
    private ConcurrentHashMap<String, Long> queueOffsetMap = new ConcurrentHashMap<>();

    private CompactionPositionMgr() {

    }

    /** 指定压缩路径并加载检查点文件。 */
    public CompactionPositionMgr(final String compactionPath) {
        this.compactionPath = compactionPath;
        this.checkpointFileName = compactionPath + File.separator + CHECKPOINT_FILE;
        this.load();
    }

    /** 记录指定队列的已压缩偏移。 */
    public void setOffset(String topic, int queueId, final long offset) {
        queueOffsetMap.put(topic + "_" + queueId, offset);
    }

    /** 获取指定队列的已压缩偏移，不存在返回 -1。 */
    public long getOffset(String topic, int queueId) {
        return queueOffsetMap.getOrDefault(topic + "_" + queueId, -1L);
    }

    /** 判断是否尚未记录任何压缩偏移。 */
    public boolean isEmpty() {
        return queueOffsetMap.isEmpty();
    }

    /** 判断给定偏移是否已被压缩覆盖。 */
    public boolean isCompaction(String topic, int queueId, long offset) {
        return getOffset(topic, queueId) > offset;
    }

    /** 返回检查点文件路径。 */
    @Override
    public String configFilePath() {
        return checkpointFileName;
    }

    /** 将进度映射编码为 JSON 字符串。 */
    @Override
    public String encode() {
        return this.encode(false);
    }

    /** 将进度映射编码为 JSON，可选格式化。 */
    @Override
    public String encode(boolean prettyFormat) {
        return RemotingSerializable.toJson(this, prettyFormat);
    }

    /** 从 JSON 字符串解码进度映射。 */
    @Override
    public void decode(String jsonString) {
        if (jsonString != null) {
            CompactionPositionMgr obj = RemotingSerializable.fromJson(jsonString, CompactionPositionMgr.class);
            if (obj != null) {
                this.queueOffsetMap = obj.queueOffsetMap;
            }
        }
    }

    /** 返回队列偏移映射表。 */
    public ConcurrentHashMap<String, Long> getQueueOffsetMap() {
        return queueOffsetMap;
    }

    /** 设置队列偏移映射表。 */
    public void setQueueOffsetMap(ConcurrentHashMap<String, Long> queueOffsetMap) {
        this.queueOffsetMap = queueOffsetMap;
    }
}
