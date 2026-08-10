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

package com.alibaba.nacos.core.distributed.raft.utils;

/**
 * 可重试任务函数式接口：供 Raft 相关逻辑封装需反复执行的操作。
 * Retry function.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@FunctionalInterface
public interface RetryRunner {
    
    /**
     * 执行一次待重试的任务体。
     */
    void run();
    
}
