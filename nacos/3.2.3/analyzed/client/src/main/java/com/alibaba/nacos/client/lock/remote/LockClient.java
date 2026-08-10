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

package com.alibaba.nacos.client.lock.remote;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.lock.model.LockInstance;
import com.alibaba.nacos.common.lifecycle.Closeable;

/**
 * 锁远程客户端接口。
 *
 * <p>定义与服务端交互的加锁/解锁操作，实现类负责传输层细节（如 gRPC）。</p>
 *
 * @author 985492783@qq.com
 * @description LockClient
 * @date 2023/6/28 17:19
 */
public interface LockClient extends Closeable {
    
    /**
     * 向服务端请求加锁。
     *
     * @param instance 锁实例
     * @return 加锁是否成功
     * @throws NacosException RPC 或业务失败时抛出
     */
    Boolean lock(LockInstance instance) throws NacosException;
    
    /**
     * 向服务端请求解锁。
     *
     * @param instance 锁实例
     * @return 解锁是否成功
     * @throws NacosException RPC 或业务失败时抛出
     */
    Boolean unLock(LockInstance instance) throws NacosException;
    
}
