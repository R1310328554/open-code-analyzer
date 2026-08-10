/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.core.v2.metadata;

import com.alibaba.nacos.consistency.SerializeFactory;
import com.alibaba.nacos.consistency.Serializer;
import com.alibaba.nacos.naming.core.v2.pojo.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 实例元数据 Raft 快照操作。
 *
 * <p>将 {@link NamingMetadataManager} 中的实例元数据映射序列化到 {@code instance_metadata.zip} 归档。</p>
 *
 * @author xiweng.yy
 */
public class InstanceMetadataSnapshotOperation extends AbstractMetadataSnapshotOperation {
    
    private static final String SNAPSHOT_SAVE =
        InstanceMetadataSnapshotOperation.class.getSimpleName() + ".SAVE";
    
    private static final String SNAPSHOT_LOAD =
        InstanceMetadataSnapshotOperation.class.getSimpleName() + ".LOAD";
    
    /** 实例元数据快照 ZIP 文件名。 */
    private static final String SNAPSHOT_ARCHIVE = "instance_metadata.zip";
    
    /** 元数据来源管理器。 */
    private final NamingMetadataManager metadataManager;
    
    /** 快照序列化器。 */
    private final Serializer serializer;
    
    public InstanceMetadataSnapshotOperation(NamingMetadataManager metadataManager,
        ReentrantReadWriteLock lock) {
        super(lock);
        this.metadataManager = metadataManager;
        this.serializer = SerializeFactory.getDefault();
    }
    
    @Override
    protected InputStream dumpSnapshot() {
        Map<Service, ConcurrentMap<String, InstanceMetadata>> snapshot =
            metadataManager.getInstanceMetadataSnapshot();
        return new ByteArrayInputStream(serializer.serialize(snapshot));
    }
    
    @Override
    protected void loadSnapshot(byte[] snapshotBytes) {
        metadataManager.loadInstanceMetadataSnapshot(serializer.deserialize(snapshotBytes));
    }
    
    @Override
    protected String getSnapshotArchive() {
        return SNAPSHOT_ARCHIVE;
    }
    
    @Override
    protected String getSnapshotSaveTag() {
        return SNAPSHOT_SAVE;
    }
    
    @Override
    protected String getSnapshotLoadTag() {
        return SNAPSHOT_LOAD;
    }
}
