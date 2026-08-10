/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.persistence;

import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.consistency.snapshot.LocalFileMeta;
import com.alibaba.nacos.consistency.snapshot.Reader;
import com.alibaba.nacos.consistency.snapshot.SnapshotOperation;
import com.alibaba.nacos.consistency.snapshot.Writer;
import com.alibaba.nacos.persistence.constants.PersistenceConstant;
import com.alibaba.nacos.persistence.datasource.DataSourceService;
import com.alibaba.nacos.persistence.datasource.DynamicDataSource;
import com.alibaba.nacos.persistence.datasource.LocalDataSourceServiceImpl;
import com.alibaba.nacos.persistence.model.event.DerbyLoadEvent;
import com.alibaba.nacos.persistence.utils.PersistenceExecutor;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.utils.DiskUtils;
import com.alibaba.nacos.sys.utils.TimerContext;
import com.alipay.sofa.jraft.util.CRC64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.zip.Checksum;

/**
 * 内嵌 Derby 数据库 Raft 快照操作：备份、压缩、校验与恢复。
 * <p>实现 {@link SnapshotOperation}，在集群模式下保证 Derby 数据与 Raft 日志一致。</p>
 * Derby Snapshot operation.
 * TODO depend on jraft strongly, Waiting for addition split.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class DerbySnapshotOperation implements SnapshotOperation {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DerbySnapshotOperation.class);
    
    /** 快照保存计时上下文键。 */
    private static final String DERBY_SNAPSHOT_SAVE =
        DerbySnapshotOperation.class.getSimpleName() + ".SAVE";
    
    /** 快照加载计时上下文键。 */
    private static final String DERBY_SNAPSHOT_LOAD =
        DerbySnapshotOperation.class.getSimpleName() + ".LOAD";
    
    /** Derby 在线备份存储过程 SQL。 */
    private final String backupSql = "CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)";
    
    /** 快照解压临时目录名。 */
    private final String snapshotDir = "derby_data";
    
    /** 快照压缩包文件名。 */
    private final String snapshotArchive = "derby_data.zip";
    
    private final String derbyBaseDir =
        Paths.get(EnvUtil.getNacosHome(), "data", PersistenceConstant.DERBY_BASE_DIR)
            .toString();
    
    private final String restoreDB = "jdbc:derby:" + derbyBaseDir;
    
    /** 快照元数据中 CRC64 校验和键名。 */
    private final String checkSumKey = "checkSum";
    
    private final ReentrantReadWriteLock.WriteLock writeLock;
    
    /** 注入写锁，快照保存/加载期间独占 Derby 访问。 */
    public DerbySnapshotOperation(ReentrantReadWriteLock.WriteLock writeLock) {
        this.writeLock = writeLock;
    }
    
    /**
     * 执行 Derby 备份、压缩为 zip 并写入 Raft 快照，附带 CRC64 校验和。
     *
     * @param writer 快照写入器
     * @param callFinally 完成回调
     */
    @Override
    public void onSnapshotSave(Writer writer, BiConsumer<Boolean, Throwable> callFinally) {
        PersistenceExecutor.executeSnapshot(() -> {
            TimerContext.start(DERBY_SNAPSHOT_SAVE);
            
            final Lock lock = writeLock;
            lock.lock();
            try {
                final String writePath = writer.getPath();
                final String parentPath = Paths.get(writePath, snapshotDir).toString();
                DiskUtils.deleteDirectory(parentPath);
                DiskUtils.forceMkdir(parentPath);
                
                doDerbyBackup(parentPath);
                
                final String outputFile = Paths.get(writePath, snapshotArchive).toString();
                final Checksum checksum = new CRC64();
                DiskUtils.compress(writePath, snapshotDir, outputFile, checksum);
                DiskUtils.deleteDirectory(parentPath);
                
                final LocalFileMeta meta = new LocalFileMeta();
                meta.append(checkSumKey, Long.toHexString(checksum.getValue()));
                
                callFinally.accept(writer.addFile(snapshotArchive, meta), null);
            } catch (Throwable t) {
                LOGGER.error("Fail to compress snapshot, path={}, file list={}, {}.",
                    writer.getPath(),
                    writer.listFiles(), t);
                callFinally.accept(false, t);
            } finally {
                lock.unlock();
                TimerContext.end(DERBY_SNAPSHOT_SAVE, LOGGER);
            }
        });
    }
    
    /**
     * 从快照解压 Derby 数据、校验 CRC 并恢复到本地目录，发布 {@link DerbyLoadEvent}。
     *
     * @param reader 快照读取器
     * @return 加载是否成功
     */
    @Override
    public boolean onSnapshotLoad(Reader reader) {
        final String readerPath = reader.getPath();
        final String sourceFile = Paths.get(readerPath, snapshotArchive).toString();
        TimerContext.start(DERBY_SNAPSHOT_LOAD);
        final Lock lock = writeLock;
        lock.lock();
        try {
            final Checksum checksum = new CRC64();
            DiskUtils.decompress(sourceFile, readerPath, checksum);
            
            LocalFileMeta fileMeta = reader.getFileMeta(snapshotArchive);
            
            if (fileMeta.getFileMeta().containsKey(checkSumKey)) {
                if (!Objects.equals(Long.toHexString(checksum.getValue()),
                    fileMeta.get(checkSumKey))) {
                    throw new IllegalArgumentException("Snapshot checksum failed");
                }
            }
            
            final String loadPath =
                Paths.get(readerPath, snapshotDir, PersistenceConstant.DERBY_BASE_DIR).toString();
            LOGGER.info("snapshot load from : {}, and copy to : {}", loadPath, derbyBaseDir);
            
            doDerbyRestoreFromBackup(() -> {
                final File srcDir = new File(loadPath);
                final File destDir = new File(derbyBaseDir);
                
                DiskUtils.copyDirectory(srcDir, destDir);
                LOGGER.info("Complete database recovery");
                return null;
            });
            DiskUtils.deleteDirectory(loadPath);
            NotifyCenter.publishEvent(DerbyLoadEvent.INSTANCE);
            return true;
        } catch (final Throwable t) {
            LOGGER.error("Fail to load snapshot, path={}, file list={}, {}.", readerPath,
                reader.listFiles(), t);
            return false;
        } finally {
            lock.unlock();
            TimerContext.end(DERBY_SNAPSHOT_LOAD, LOGGER);
        }
    }
    
    /** 调用 Derby 存储过程将数据库备份到指定目录。 */
    private void doDerbyBackup(String backupDirectory) throws Exception {
        DataSourceService sourceService = DynamicDataSource.getInstance().getDataSource();
        DataSource dataSource = sourceService.getJdbcTemplate().getDataSource();
        try (Connection holder = Objects.requireNonNull(dataSource, "dataSource").getConnection();
            CallableStatement cs = holder.prepareCall(backupSql)) {
            cs.setString(1, backupDirectory);
            cs.execute();
        }
    }
    
    /** 关闭并重建 Derby 连接后执行恢复回调（拷贝快照数据）。 */
    private void doDerbyRestoreFromBackup(Callable<Void> callable) throws Exception {
        DataSourceService sourceService = DynamicDataSource.getInstance().getDataSource();
        LocalDataSourceServiceImpl localDataSourceService =
            (LocalDataSourceServiceImpl) sourceService;
        localDataSourceService.restoreDerby(restoreDB, callable);
    }
    
}
