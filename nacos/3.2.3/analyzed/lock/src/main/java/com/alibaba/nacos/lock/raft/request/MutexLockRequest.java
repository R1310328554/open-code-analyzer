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

package com.alibaba.nacos.lock.raft.request;

import com.alibaba.nacos.lock.model.LockInfo;

import java.io.Serializable;

/**
 * Raft 互斥锁操作请求体。
 *
 * <p>封装 {@link LockInfo}，在 Raft 提案中传递加锁/解锁参数。</p>
 *
 * @author 985492783@qq.com
 * @date 2023/8/24 18:40
 */
public class MutexLockRequest implements Serializable {
    
    private static final long serialVersionUID = -925543547156890549L;
    
    /** 锁操作信息。 */
    private LockInfo lockInfo;
    
    /** 获取锁操作信息。 */
    public LockInfo getLockInfo() {
        return lockInfo;
    }
    
    /** 设置锁操作信息。 */
    public void setLockInfo(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }
}
