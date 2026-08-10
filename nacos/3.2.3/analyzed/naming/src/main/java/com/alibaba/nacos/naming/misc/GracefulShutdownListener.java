/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.misc;

import com.alibaba.nacos.core.listener.NacosApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Naming 模块优雅关闭监听器。
 *
 * <p>在 Spring 应用启动失败时销毁 {@link NamingExecuteTaskDispatcher}，释放异步任务调度资源，避免残留线程。</p>
 *
 * @author Weizhan▪Yun
 * @date 2022/11/2 14:40
 */
public class GracefulShutdownListener implements NacosApplicationListener {
    
    /** 应用启动失败回调，销毁 Naming 任务分发器。 */
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        try {
            NamingExecuteTaskDispatcher.getInstance().destroy();
        } catch (Exception ignore) {
        }
    }
}
