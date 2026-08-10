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

package com.alibaba.nacos.core.distributed.raft;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.consistency.snapshot.LocalFileMeta;
import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.entity.LocalFileMetaOutter;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.google.protobuf.ZeroByteStringHelper;

/**
 * JRaft 快照操作适配接口：将 Nacos 一致性层快照语义映射到 JRaft {@link SnapshotWriter}/{@link SnapshotReader}。
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
interface JSnapshotOperation {
    
    /**
     * 执行快照保存：将状态持久化到 writer 指定路径。
     *
     * @param writer {@link SnapshotWriter}
     * @param done   {@link Closure}
     */
    void onSnapshotSave(SnapshotWriter writer, Closure done);
    
    /**
     * 从 reader 加载快照并恢复状态；成功返回 true。
     *
     * @param reader {@link SnapshotReader}
     * @return operation label
     */
    boolean onSnapshotLoad(SnapshotReader reader);
    
    /**
     * 返回快照执行器标识（用于日志与排错）。
     *
     * @return name
     */
    String info();
    
    /**
     * 将 Nacos {@link LocalFileMeta} 序列化为 JRaft 快照文件元数据。
     *
     * @param metadata meta data
     * @return {@link LocalFileMetaOutter.LocalFileMeta}
     * @throws Exception Exception
     */
    default LocalFileMetaOutter.LocalFileMeta buildMetadata(final LocalFileMeta metadata)
        throws Exception {
        return metadata == null ? null : LocalFileMetaOutter.LocalFileMeta.newBuilder()
            .setUserMeta(ZeroByteStringHelper.wrap(JacksonUtils.toJsonBytes(metadata))).build();
    }
    
}
