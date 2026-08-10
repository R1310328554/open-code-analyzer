/*
 *  Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.alibaba.nacos.core.exception;

/**
 * Core 模块错误码枚举：编号自 40001 起，涵盖 KV 存储、磁盘 IO 与一致性协议异常。
 * Core module code starts with 40001.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public enum ErrorCode {
    
    /** 未知错误（40001）。 */
    /** unknown error. */
    UnKnowError(40001),
    
    // KV 存储相关错误码
    
    /** KV 存储写入失败（40100）。 */
    /** KVStorage write error. */
    KVStorageWriteError(40100),
    
    /** KV 存储读取失败（40101）。 */
    /** KVStorage read error. */
    KVStorageReadError(40101),
    
    /** KV 存储删除失败（40102）。 */
    /** KVStorage delete error. */
    KVStorageDeleteError(40102),
    
    /** KV 快照保存失败（40103）。 */
    /** KVStorage snapshot save error. */
    KVStorageSnapshotSaveError(40103),
    
    /** KV 快照加载失败（40104）。 */
    /** KVStorage snapshot load error. */
    KVStorageSnapshotLoadError(40104),
    
    /** KV 存储重置失败（40105）。 */
    /** KVStorage reset error. */
    KVStorageResetError(40105),
    
    /** KV 存储创建失败（40106）。 */
    /** KVStorage create error. */
    KVStorageCreateError(40106),
    
    /** KV 批量写入失败（40107）。 */
    /** KVStorage write error. */
    KVStorageBatchWriteError(40107),
    
    // 磁盘 IO 相关错误码
    
    /** 创建目录失败（40201）。 */
    /** mkdir error. */
    IOMakeDirError(40201),
    
    /** 目录复制失败（40202）。 */
    /** copy directory has error. */
    IOCopyDirError(40202),
    
    // 一致性协议相关错误码
    
    /** 一致性协议提交写请求失败（40301）。 */
    /** protocol write error. */
    ProtoSubmitError(40301),
    
    /** 一致性协议读请求失败（40302）。 */
    /** protocol read error. */
    ProtoReadError(40302);
    
    /** 数值错误码。 */
    private final int code;
    
    /** 绑定错误码数值。 */
    ErrorCode(int code) {
        this.code = code;
    }
    
    /** 返回错误码。 */
    public int getCode() {
        return code;
    }
}
