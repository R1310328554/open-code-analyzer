/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.constants;

/**
 * 命名推送任务相关配置常量。
 *
 * <p>定义推送延迟、超时与重试间隔的配置键及默认值。</p>
 *
 * @author xiweng.yy
 */
public class PushConstants {
    
    /** 推送任务延迟时间配置键，单位毫秒。 */
    public static final String PUSH_TASK_DELAY = "nacos.naming.push.pushTaskDelay";
    
    /** 默认推送任务延迟：500 毫秒。 */
    public static final long DEFAULT_PUSH_TASK_DELAY = 500L;
    
    /** 推送任务执行超时配置键，单位毫秒。 */
    public static final String PUSH_TASK_TIMEOUT = "nacos.naming.push.pushTaskTimeout";
    
    /** 默认推送任务超时：5000 毫秒。 */
    public static final long DEFAULT_PUSH_TASK_TIMEOUT = 5000L;
    
    /** 推送任务重试延迟配置键，单位毫秒。 */
    public static final String PUSH_TASK_RETRY_DELAY = "nacos.naming.push.pushTaskRetryDelay";
    
    /** 默认推送重试延迟：1000 毫秒。 */
    public static final long DEFAULT_PUSH_TASK_RETRY_DELAY = 1000L;
}
