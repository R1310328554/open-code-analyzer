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

package com.alibaba.nacos.common.task;

/**
 * Nacos 通用任务接口：所有延迟任务与即时任务均实现此接口，
 * 由任务执行引擎在调度前调用 {@link #shouldProcess()} 判断是否到达执行条件。
 * Nacos task.
 *
 * @author xiweng.yy
 */
public interface NacosTask {
    
    /**
     * 判断当前任务是否满足执行条件（如延迟间隔已到）。
     *
     * @return true 表示应执行，false 表示继续等待
     */
    boolean shouldProcess();
}
