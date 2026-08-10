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

package com.alibaba.nacos.sys.file;

import java.nio.file.WatchEvent;
import java.util.concurrent.Executor;

/**
 * 文件变更监听器抽象基类。
 *
 * <p>子类实现 {@link #interest(String)} 过滤关注文件，在 {@link #onChange(FileChangeEvent)} 中处理变更；可自定义 {@link #executor()} 线程池。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public abstract class FileWatcher {
    
    /**
     * 文件变更回调，由 {@link WatchFileCenter} 异步触发。
     *
     * @param event {@link FileChangeEvent}
     */
    public abstract void onChange(FileChangeEvent event);
    
    /**
     * 判断是否关注该 {@link WatchEvent#context()}（通常为相对路径或文件名）。
     *
     * @param context {@link WatchEvent#context()}
     * @return is this watcher interest context
     */
    public abstract boolean interest(String context);
    
    /**
     * 可选自定义执行器；返回 {@code null} 时在 WatchFileCenter 回调线程同步执行。
     *
     * @return {@link Executor}
     */
    public Executor executor() {
        return null;
    }
    
}
