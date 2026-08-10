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

package com.alibaba.nacos.common.executor;

import com.alibaba.nacos.common.utils.StringUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Name thread factory.
 * <p>为线程统一添加前缀与递增序号的 {@link ThreadFactory}，创建出的线程默认为守护线程。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class NameThreadFactory implements ThreadFactory {
    
    /** 线程名后缀递增序号 */
    private final AtomicInteger id = new AtomicInteger(0);
    
    /** 线程名前缀（保证以 {@link com.alibaba.nacos.common.utils.StringUtils#DOT} 结尾） */
    private String name;
    
    /**
     * @param name 线程名前缀
     */
    public NameThreadFactory(String name) {
        if (!name.endsWith(StringUtils.DOT)) {
            name += StringUtils.DOT;
        }
        this.name = name;
    }
    
    /** {@inheritDoc} 创建名为 {@code prefix + 序号} 的守护线程 */
    @Override
    public Thread newThread(Runnable r) {
        String threadName = name + id.getAndIncrement();
        Thread thread = new Thread(r, threadName);
        thread.setDaemon(true);
        return thread;
    }
}
