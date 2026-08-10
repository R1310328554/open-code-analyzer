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

package com.alibaba.nacos.naming.push.v2;

import com.alibaba.nacos.core.config.AbstractDynamicConfig;
import com.alibaba.nacos.naming.constants.PushConstants;
import com.alibaba.nacos.sys.env.EnvUtil;

/**
 * 推送模块动态配置（单例）。
 *
 * <p>从环境变量读取推送任务延迟、超时与重试间隔，支持运行时热更新。</p>
 *
 * @author xiweng.yy
 */
public class PushConfig extends AbstractDynamicConfig {
    
    private static final String PUSH = "Push";
    
    private static final PushConfig INSTANCE = new PushConfig();
    
    /** 推送任务首次执行延迟（毫秒）。 */
    private long pushTaskDelay = PushConstants.DEFAULT_PUSH_TASK_DELAY;
    
    /** 单次推送超时时间（毫秒）。 */
    private long pushTaskTimeout = PushConstants.DEFAULT_PUSH_TASK_TIMEOUT;
    
    /** 推送失败后重试间隔（毫秒）。 */
    private long pushTaskRetryDelay = PushConstants.DEFAULT_PUSH_TASK_RETRY_DELAY;
    
    private PushConfig() {
        super(PUSH);
        resetConfig();
    }
    
    @Override
    protected void getConfigFromEnv() {
        pushTaskDelay = EnvUtil
            .getProperty(PushConstants.PUSH_TASK_DELAY, Long.class,
                PushConstants.DEFAULT_PUSH_TASK_DELAY);
        pushTaskTimeout = EnvUtil
            .getProperty(PushConstants.PUSH_TASK_TIMEOUT, Long.class,
                PushConstants.DEFAULT_PUSH_TASK_TIMEOUT);
        pushTaskRetryDelay = EnvUtil.getProperty(PushConstants.PUSH_TASK_RETRY_DELAY, Long.class,
            PushConstants.DEFAULT_PUSH_TASK_RETRY_DELAY);
    }
    
    @Override
    protected String printConfig() {
        return "PushConfig{" + "pushTaskDelay=" + pushTaskDelay + ", pushTaskTimeout="
            + pushTaskTimeout
            + ", pushTaskRetryDelay=" + pushTaskRetryDelay + '}';
    }
    
    /** 返回推送配置单例。 */
    public static PushConfig getInstance() {
        return INSTANCE;
    }
    
    /** 获取推送任务延迟毫秒数。 */
    public long getPushTaskDelay() {
        return pushTaskDelay;
    }
    
    /** 获取推送任务超时毫秒数。 */
    public long getPushTaskTimeout() {
        return pushTaskTimeout;
    }
    
    /** 获取推送重试间隔毫秒数。 */
    public long getPushTaskRetryDelay() {
        return pushTaskRetryDelay;
    }
}
