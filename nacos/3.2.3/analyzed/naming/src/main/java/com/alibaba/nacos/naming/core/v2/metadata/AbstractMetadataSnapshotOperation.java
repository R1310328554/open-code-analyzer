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

import com.alibaba.nacos.consistency.snapshot.LocalFileMeta;
import com.alibaba.nacos.consistency.snapshot.Reader;
import com.alibaba.nacos.consistency.snapshot.Writer;
import com.alibaba.nacos.naming.consistency.persistent.impl.AbstractSnapshotOperation;
import com.alibaba.nacos.sys.utils.DiskUtils;
import com.alipay.sofa.jraft.util.CRC64;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.Checksum;

/**
 * 命名元数据快照操作抽象基类。
 *
 * <p>将元数据序列化后压缩为 ZIP 归档写入 Raft 快照，读取时校验 CRC64 后还原。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractMetadataSnapshotOperation extends AbstractSnapshotOperation {
    
    /** ZIP 归档内元数据条目的文件名。 */
    private static final String METADATA_CHILD_NAME = "metadata";
    
    public AbstractMetadataSnapshotOperation(ReentrantReadWriteLock lock) {
        super(lock);
    }
    
    @Override
    protected boolean writeSnapshot(Writer writer) throws IOException {
        final String writePath = writer.getPath();
        final String outputFile = Paths.get(writePath, getSnapshotArchive()).toString();
        final Checksum checksum = new CRC64();
        try (InputStream inputStream = dumpSnapshot()) {
            DiskUtils.compressIntoZipFile(METADATA_CHILD_NAME, inputStream, outputFile, checksum);
        }
        final LocalFileMeta meta = new LocalFileMeta();
        meta.append(CHECK_SUM_KEY, Long.toHexString(checksum.getValue()));
        return writer.addFile(getSnapshotArchive(), meta);
    }
    
    @Override
    protected boolean readSnapshot(Reader reader) throws Exception {
        final String readerPath = reader.getPath();
        final String sourceFile = Paths.get(readerPath, getSnapshotArchive()).toString();
        final Checksum checksum = new CRC64();
        byte[] snapshotBytes = DiskUtils.decompress(sourceFile, checksum);
        LocalFileMeta fileMeta = reader.getFileMeta(getSnapshotArchive());
        if (fileMeta.getFileMeta().containsKey(CHECK_SUM_KEY)) {
            if (!Objects.equals(Long.toHexString(checksum.getValue()),
                fileMeta.get(CHECK_SUM_KEY))) {
                throw new IllegalArgumentException("Snapshot checksum failed");
            }
        }
        loadSnapshot(snapshotBytes);
        return true;
    }
    
    /**
     * 返回快照 ZIP 归档文件名。
     *
     * @return 快照归档名
     */
    protected abstract String getSnapshotArchive();
    
    /**
     * 将当前元数据导出为字节流。
     *
     * @return 快照字节流
     */
    protected abstract InputStream dumpSnapshot();
    
    /**
     * 从解压后的快照字节加载元数据。
     *
     * @param snapshotBytes 快照原始字节
     */
    protected abstract void loadSnapshot(byte[] snapshotBytes);
}
