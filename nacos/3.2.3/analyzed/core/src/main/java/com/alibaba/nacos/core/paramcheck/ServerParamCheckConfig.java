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

package com.alibaba.nacos.core.paramcheck;

import com.alibaba.nacos.core.config.AbstractDynamicConfig;
import com.alibaba.nacos.sys.env.EnvUtil;

/**
 * 服务端参数校验动态配置：控制是否启用校验及当前激活的校验器类型（如 default）。
 * The type Server param check config.
 *
 * @author zhuoguang
 */
public class ServerParamCheckConfig extends AbstractDynamicConfig {
    
    /** 动态配置模块名。 */
    private static final String PARAM_CHECK = "ParamCheck";
    
    /** 全局单例。 */
    private static final ServerParamCheckConfig INSTANCE = new ServerParamCheckConfig();
    
    /** 是否启用参数校验，对应 {@code nacos.core.param.check.enabled}。 */
    private boolean paramCheckEnabled = true;
    
    /** 当前使用的校验器类型，对应 {@code nacos.core.param.check.checker}。 */
    private String activeParamChecker = "default";
    
    /** 注册动态配置并加载环境变量默认值。 */
    protected ServerParamCheckConfig() {
        super(PARAM_CHECK);
        resetConfig();
    }
    
    /** 返回全局配置单例。 */
    public static ServerParamCheckConfig getInstance() {
        return INSTANCE;
    }
    
    /** 从环境变量/系统属性刷新开关与校验器类型。 */
    @Override
    protected void getConfigFromEnv() {
        paramCheckEnabled =
            EnvUtil.getProperty("nacos.core.param.check.enabled", Boolean.class, true);
        activeParamChecker =
            EnvUtil.getProperty("nacos.core.param.check.checker", String.class, "default");
    }
    
    /** 参数校验是否已启用。 */
    public boolean isParamCheckEnabled() {
        return paramCheckEnabled;
    }
    
    /** 动态更新校验开关。 */
    public void setParamCheckEnabled(boolean paramCheckEnabled) {
        this.paramCheckEnabled = paramCheckEnabled;
    }
    
    /** 返回当前激活的校验器类型名。 */
    public String getActiveParamChecker() {
        return activeParamChecker;
    }
    
    /** 动态切换校验器实现。 */
    public void setActiveParamChecker(String activeParamChecker) {
        this.activeParamChecker = activeParamChecker;
    }
    
    /** 输出当前配置快照字符串。 */
    @Override
    protected String printConfig() {
        return "ParamCheckConfig{" + "paramCheckEnabled=" + paramCheckEnabled
            + ", activeParamChecker=" + activeParamChecker + "}";
    }
    
}
