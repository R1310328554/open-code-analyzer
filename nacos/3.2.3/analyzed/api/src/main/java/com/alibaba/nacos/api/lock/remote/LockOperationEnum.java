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

package com.alibaba.nacos.api.lock.remote;

import java.io.Serializable;

/**
 * 分布式锁远程操作类型枚举。
 *
 * <p>标识 {@link com.alibaba.nacos.api.lock.remote.request.LockOperationRequest} 要执行的动作。</p>
 *
 * @author 985492783@qq.com
 */
public enum LockOperationEnum implements Serializable {
    
    /** 获取锁（加锁）。 */
    ACQUIRE,
    /** 释放锁（解锁）。 */
    RELEASE,
    /** 锁过期（服务端主动清理）。 */
    EXPIRE;
    
    private static final long serialVersionUID = -241044344531890549L;
}
