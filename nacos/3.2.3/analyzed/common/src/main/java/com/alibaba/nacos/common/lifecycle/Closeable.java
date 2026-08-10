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

package com.alibaba.nacos.common.lifecycle;

import com.alibaba.nacos.api.exception.NacosException;

/**
 * An interface is used to define the resource's close and shutdown, such as IO Connection and ThreadPool.
 * <p>可关闭资源生命周期接口：统一描述 IO 连接、线程池等组件的优雅关闭行为，与 {@link java.io.Closeable} 不同，关闭失败时抛出 {@link NacosException}。</p>
 *
 * @author zongtanghu
 */
public interface Closeable {
    
    /**
     * Shutdown the Resources, such as Thread Pool.
     * <p>释放资源并停止后台任务；实现类应保证幂等或线程安全。</p>
     *
     * @throws NacosException exception.
     */
    void shutdown() throws NacosException;
    
}
