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
package org.apache.rocketmq.store;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.store.config.MessageStoreConfig;
import org.apache.rocketmq.store.logfile.MappedFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 多路径 MappedFile 队列：CommitLog 可分布在多个磁盘路径，创建与加载时轮询选取。
 */
public class MultiPathMappedFileQueue extends MappedFileQueue {

    /** 消息存储配置（含多路径与只读路径）。 */
    private final MessageStoreConfig config;
    /** 磁盘空间已满路径集合供应器，创建文件时排除。 */
    private final Supplier<Set<String>> fullStorePathsSupplier;

    /** 构造多路径队列（无 RunningFlags）。 */
    public MultiPathMappedFileQueue(MessageStoreConfig messageStoreConfig, int mappedFileSize,
        AllocateMappedFileService allocateMappedFileService,
        Supplier<Set<String>> fullStorePathsSupplier) {
        this(messageStoreConfig, mappedFileSize, allocateMappedFileService, fullStorePathsSupplier, null);
    }
    /** 构造多路径队列并指定 RunningFlags。 */
    public MultiPathMappedFileQueue(MessageStoreConfig messageStoreConfig, int mappedFileSize,
                                    AllocateMappedFileService allocateMappedFileService,
                                    Supplier<Set<String>> fullStorePathsSupplier, RunningFlags runningFlags) {
        super(messageStoreConfig.getStorePathCommitLog(), mappedFileSize, allocateMappedFileService, runningFlags,
              messageStoreConfig.isWriteWithoutMmap());
        this.config = messageStoreConfig;
        this.fullStorePathsSupplier = fullStorePathsSupplier;
    }

    /** 解析可写 CommitLog 存储路径集合。 */
    private Set<String> getPaths() {
        String[] paths = config.getStorePathCommitLog().trim().split(MixAll.MULTI_PATH_SPLITTER);
        return new HashSet<>(Arrays.asList(paths));
    }

    /** 解析只读 CommitLog 路径集合。 */
    private Set<String> getReadonlyPaths() {
        String pathStr = config.getReadOnlyCommitLogStorePaths();
        if (StringUtils.isBlank(pathStr)) {
            return Collections.emptySet();
        }
        String[] paths = pathStr.trim().split(MixAll.MULTI_PATH_SPLITTER);
        return new HashSet<>(Arrays.asList(paths));
    }

    /** 从所有可写与只读路径加载 MappedFile。 */
    @Override
    public boolean load() {
        Set<String> storePathSet = getPaths();
        storePathSet.addAll(getReadonlyPaths());

        List<File> files = new ArrayList<>();
        for (String path : storePathSet) {
            File dir = new File(path);
            File[] ls = dir.listFiles();
            if (ls != null) {
                Collections.addAll(files, ls);
            }
        }

        return doLoad(files);
    }

    /** 按 fileIdx 轮询选取路径并创建 MappedFile。 */
    @Override
    public MappedFile tryCreateMappedFile(long createOffset) {
        long fileIdx = createOffset / this.mappedFileSize;
        Set<String> storePath = getPaths();
        Set<String> readonlyPathSet = getReadonlyPaths();
        Set<String> fullStorePaths =
                fullStorePathsSupplier == null ? Collections.emptySet() : fullStorePathsSupplier.get();


        HashSet<String> availableStorePath = new HashSet<>(storePath);
        // 不在只读路径上创建新文件
        availableStorePath.removeAll(readonlyPathSet);

        // 磁盘空间将满的路径不创建新文件
        availableStorePath.removeAll(fullStorePaths);

        // 若无可用路径则回退到可写路径
        if (availableStorePath.isEmpty()) {
            availableStorePath = new HashSet<>(storePath);
            availableStorePath.removeAll(readonlyPathSet);
        }

        String[] paths = availableStorePath.toArray(new String[]{});
        Arrays.sort(paths);
        String nextFilePath = paths[(int) (fileIdx % paths.length)] + File.separator
                + UtilAll.offset2FileName(createOffset);
        String nextNextFilePath = paths[(int) ((fileIdx + 1) % paths.length)] + File.separator
                + UtilAll.offset2FileName(createOffset + this.mappedFileSize);
        return doCreateMappedFile(nextFilePath, nextNextFilePath);
    }

    /** 销毁所有 MappedFile 并清空各路径目录。 */
    @Override
    public void destroy() {
        for (MappedFile mf : this.mappedFiles) {
            mf.destroy(1000 * 3);
        }
        this.mappedFiles.clear();
        this.setFlushedWhere(0);

        Set<String> storePathSet = getPaths();
        storePathSet.addAll(getReadonlyPaths());

        for (String path : storePathSet) {
            File file = new File(path);
            if (file.isDirectory()) {
                file.delete();
            }
        }
    }
}
