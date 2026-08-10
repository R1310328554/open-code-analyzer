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

package com.alibaba.nacos.client.config.impl;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.utils.StringUtils;
import com.alibaba.nacos.client.utils.ConcurrentDiskUtil;
import com.alibaba.nacos.client.config.utils.JvmUtil;
import com.alibaba.nacos.client.config.utils.SnapShotSwitch;
import com.alibaba.nacos.client.utils.LogUtils;
import com.alibaba.nacos.common.utils.IoUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

import static com.alibaba.nacos.client.utils.ParamUtil.simplyEnvNameIfOverLimit;

/**
 * 加密数据密钥（EncryptedDataKey）本地快照与容灾目录管理。
 *
 * <p>在 {@link LocalConfigInfoProcessor} 的本地目录结构下，为加密配置的密钥提供 failover 与 snapshot 读写，
 * 以便服务端不可达时仍能解密本地缓存配置。</p>
 *
 * @author luyanbo(RobberPhex)
 */
public class LocalEncryptedDataKeyProcessor extends LocalConfigInfoProcessor {
    
    private static final Logger LOGGER = LogUtils.logger(LocalEncryptedDataKeyProcessor.class);
    
    /** 容灾目录一级子路径：encrypted-data-key。 */
    private static final String FAILOVER_CHILD_1 = "encrypted-data-key";
    
    /** 容灾目录二级子路径（无 tenant 时）：failover。 */
    private static final String FAILOVER_CHILD_2 = "failover";
    
    /** 容灾目录二级子路径（有 tenant 时）：failover-tenant。 */
    private static final String FAILOVER_CHILD_3 = "failover-tenant";
    
    /** 快照目录一级子路径：encrypted-data-key。 */
    private static final String SNAPSHOT_CHILD_1 = "encrypted-data-key";
    
    /** 快照目录二级子路径（无 tenant 时）：snapshot。 */
    private static final String SNAPSHOT_CHILD_2 = "snapshot";
    
    /** 快照目录三级子路径（有 tenant 时）：snapshot-tenant。 */
    private static final String SNAPSHOT_CHILD_3 = "snapshot-tenant";
    
    /** 环境名后缀，拼接到本地根目录下。 */
    private static final String SUFFIX = "_nacos";
    
    /**
     * 读取容灾目录中的 EncryptedDataKey。
     *
     * <p>本地文件不存在或读取异常时返回 {@code null}。</p>
     *
     * @param envName 环境/服务端标识
     * @param dataId  配置 Data ID
     * @param group   配置分组
     * @param tenant  命名空间（可为空）
     * @return 密钥字符串；无本地文件或异常时为 null
     */
    public static String getEncryptDataKeyFailover(String envName, String dataId, String group,
        String tenant) {
        envName = simplyEnvNameIfOverLimit(envName);
        File file = getEncryptDataKeyFailoverFile(envName, dataId, group, tenant);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        
        try {
            return readFile(file);
        } catch (IOException ioe) {
            LOGGER.error("[" + envName + "] get failover error, " + file, ioe);
            return null;
        }
    }
    
    /**
     * 读取本地快照中的 EncryptedDataKey。
     *
     * <p>快照功能关闭、文件不存在或读取异常时返回 {@code null}。</p>
     *
     * @param envName 环境/服务端标识
     * @param dataId  配置 Data ID
     * @param group   配置分组
     * @param tenant  命名空间（可为空）
     * @return 密钥字符串；无快照或异常时为 null
     */
    public static String getEncryptDataKeySnapshot(String envName, String dataId, String group,
        String tenant) {
        
        if (!SnapShotSwitch.getIsSnapShot()) {
            return null;
        }
        File file = getEncryptDataKeySnapshotFile(envName, dataId, group, tenant);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        
        try {
            return readFile(file);
        } catch (IOException ioe) {
            LOGGER.error("[" + envName + "] get snapshot error, " + file, ioe);
            return null;
        }
    }
    
    /**
     * 保存 EncryptedDataKey 快照。
     *
     * <p>{@code encryptDataKey} 为 {@code null} 时删除对应快照文件。</p>
     *
     * @param envName        环境/服务端标识
     * @param dataId         配置 Data ID
     * @param group          配置分组
     * @param tenant         命名空间（可为空）
     * @param encryptDataKey 待持久化的密钥；null 表示删除快照
     */
    public static void saveEncryptDataKeySnapshot(String envName, String dataId, String group,
        String tenant,
        String encryptDataKey) {
        if (!SnapShotSwitch.getIsSnapShot()) {
            return;
        }
        File file = getEncryptDataKeySnapshotFile(envName, dataId, group, tenant);
        try {
            if (null == encryptDataKey) {
                // 密钥为空时删除快照文件
                try {
                    IoUtils.delete(file);
                } catch (IOException ioe) {
                    LOGGER.error("[" + envName + "] delete snapshot error, " + file, ioe);
                }
            } else {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    boolean isMdOk = parentFile.mkdirs();
                    if (!isMdOk) {
                        LOGGER.error("[{}] save snapshot error", envName);
                    }
                }
                // 多实例部署时使用并发安全写盘
                if (JvmUtil.isMultiInstance()) {
                    ConcurrentDiskUtil.writeFileContent(file, encryptDataKey, Constants.ENCODE);
                } else {
                    IoUtils.writeStringToFile(file, encryptDataKey, Constants.ENCODE);
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("[" + envName + "] save snapshot error, " + file, ioe);
        }
    }
    
    /**
     * 解析容灾 EncryptedDataKey 本地文件路径。
     *
     * @param envName 环境/服务端标识
     * @param dataId  配置 Data ID
     * @param group   配置分组
     * @param tenant  命名空间（可为空）
     * @return 容灾密钥文件
     */
    private static File getEncryptDataKeyFailoverFile(String envName, String dataId, String group,
        String tenant) {
        envName = simplyEnvNameIfOverLimit(envName);
        
        File tmp = new File(LOCAL_SNAPSHOT_PATH, envName + SUFFIX);
        tmp = new File(tmp, FAILOVER_CHILD_1);
        
        if (StringUtils.isBlank(tenant)) {
            tmp = new File(tmp, FAILOVER_CHILD_2);
        } else {
            tmp = new File(tmp, FAILOVER_CHILD_3);
            tmp = new File(tmp, tenant);
        }
        
        return new File(new File(tmp, group), dataId);
    }
    
    /**
     * 解析快照 EncryptedDataKey 本地文件路径。
     *
     * @param envName 环境/服务端标识
     * @param dataId  配置 Data ID
     * @param group   配置分组
     * @param tenant  命名空间（可为空）
     * @return 快照密钥文件
     */
    private static File getEncryptDataKeySnapshotFile(String envName, String dataId, String group,
        String tenant) {
        envName = simplyEnvNameIfOverLimit(envName);
        
        File tmp = new File(LOCAL_SNAPSHOT_PATH, envName + SUFFIX);
        tmp = new File(tmp, SNAPSHOT_CHILD_1);
        
        if (StringUtils.isBlank(tenant)) {
            tmp = new File(tmp, SNAPSHOT_CHILD_2);
        } else {
            tmp = new File(tmp, SNAPSHOT_CHILD_3);
            tmp = new File(tmp, tenant);
        }
        
        return new File(new File(tmp, group), dataId);
    }
    
}
