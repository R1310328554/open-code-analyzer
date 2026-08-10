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

package com.alibaba.nacos.config.server.remote;

import com.alibaba.nacos.config.server.service.ConfigFuzzyWatchContextService;
import com.alibaba.nacos.core.remote.ClientConnectionEventListener;
import com.alibaba.nacos.core.remote.Connection;
import com.alibaba.nacos.core.utils.Loggers;
import org.springframework.stereotype.Component;

/**
 * 配置 RPC 连接事件监听器：客户端断开时清理 v2 精确监听与模糊监听上下文，
 * 防止僵尸 connectionId 占用内存并误触发推送。
 * ConfigConnectionEventListener.
 *
 * @author liuzunfei
 * @version $Id: ConfigConnectionEventListener.java, v 0.1 2020年07月20日 2:27 PM liuzunfei Exp $
 */
@Component
public class ConfigConnectionEventListener extends ClientConnectionEventListener {
    
    /** v2 批量监听上下文 */
    final ConfigChangeListenContext configChangeListenContext;
    
    /** 模糊监听上下文服务 */
    final ConfigFuzzyWatchContextService configFuzzyWatchContextService;
    
    public ConfigConnectionEventListener(ConfigChangeListenContext configChangeListenContext,
        ConfigFuzzyWatchContextService configFuzzyWatchContextService) {
        this.configChangeListenContext = configChangeListenContext;
        this.configFuzzyWatchContextService = configFuzzyWatchContextService;
    }
    
    @Override
    public void clientConnected(Connection connect) {
        // 连接建立时无需预注册，监听由后续 RPC 请求完成
    }
    
    /** 连接断开：清理该 connectionId 下的全部监听与模糊 watch 状态。 */
    @Override
    public void clientDisConnected(Connection connect) {
        String connectionId = connect.getMetaInfo().getConnectionId();
        Loggers.REMOTE_DIGEST.info("[{}]client disconnected,clear config listen context",
            connectionId);
        configChangeListenContext.clearContextForConnectionId(connectionId);
        configFuzzyWatchContextService.clearFuzzyWatchContext(connectionId);
    }
    
}
