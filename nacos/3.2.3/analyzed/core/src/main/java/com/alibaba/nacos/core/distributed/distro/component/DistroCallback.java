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

package com.alibaba.nacos.core.distributed.distro.component;

/**
 * Distro 异步任务回调：在同步/加载等任务成功或失败时通知调用方。
 * Distro callback.
 *
 * @author xiweng.yy
 */
public interface DistroCallback {
    
    /** 任务执行成功时回调。 */
    void onSuccess();
    
    /**
     * 任务执行失败时回调。
     *
     * @param throwable throwable if execute failed caused by exception
     */
    void onFailed(Throwable throwable);
}
