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

package com.alibaba.nacos.client.lock.core;

import com.alibaba.nacos.api.lock.common.LockConstants;
import com.alibaba.nacos.api.lock.model.LockInstance;

/**
 * Nacos 客户端锁实例实体。
 *
 * <p>继承 {@link LockInstance}，固定锁类型为 {@link LockConstants#NACOS_LOCK_TYPE}，
 * 供 {@link NLockFactory} 创建及 {@link com.alibaba.nacos.client.lock.NacosLockService} 远程操作。</p>
 *
 * @author 985492783@qq.com
 * @date 2023/8/24 19:52
 */
public class NLock extends LockInstance {
    
    private static final long serialVersionUID = -346054842454875524L;
    
    /**
     * 创建指定键与过期时间的 Nacos 锁实例。
     *
     * @param key             锁键
     * @param expireTimestamp 过期时间戳（毫秒）；{@code -1} 表示无过期
     */
    public NLock(String key, Long expireTimestamp) {
        super(key, expireTimestamp, LockConstants.NACOS_LOCK_TYPE);
    }
}
