/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.control.connection;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.plugin.control.Loggers;
import com.alibaba.nacos.plugin.control.connection.request.ConnectionCheckRequest;
import com.alibaba.nacos.plugin.control.connection.response.ConnectionCheckCode;
import com.alibaba.nacos.plugin.control.connection.response.ConnectionCheckResponse;
import com.alibaba.nacos.plugin.control.connection.rule.ConnectionControlRule;

/**
 * 默认连接管控管理器，不做任何连接数限制。
 *
 * <p>未配置管控插件或构建失败时的降级实现：规则变更仅记录日志，
 * 连接校验始终返回 {@link ConnectionCheckCode#CHECK_SKIP} 放行。</p>
 *
 * @author shiyiyue
 */
public class DefaultConnectionControlManager extends ConnectionControlManager {
    
    /**
     * 返回管理器名称 {@code noLimit}。
     *
     * @return 固定标识 {@code noLimit}
     */
    @Override
    public String getName() {
        return "noLimit";
    }
    
    public DefaultConnectionControlManager() {
        super();
    }
    
    /**
     * 更新连接限制规则并记录日志，但不实际执行限连逻辑。
     *
     * @param connectionControlRule 新的连接限制规则
     */
    @Override
    public void applyConnectionLimitRule(ConnectionControlRule connectionControlRule) {
        super.connectionControlRule = connectionControlRule;
        Loggers.CONTROL.info("Connection control rule updated to -> {}",
            (this.connectionControlRule == null ? null
                : JacksonUtils.toJson(this.connectionControlRule)));
        Loggers.CONTROL.warn(
            "Connection control updated, But connection control manager is no limit implementation.");
    }
    
    /**
     * 连接校验始终放行。
     *
     * @param connectionCheckRequest 连接校验请求
     * @return 成功且跳过检查的响应
     */
    @Override
    public ConnectionCheckResponse check(ConnectionCheckRequest connectionCheckRequest) {
        ConnectionCheckResponse connectionCheckResponse = new ConnectionCheckResponse();
        connectionCheckResponse.setSuccess(true);
        connectionCheckResponse.setCode(ConnectionCheckCode.CHECK_SKIP);
        return connectionCheckResponse;
    }
    
}
