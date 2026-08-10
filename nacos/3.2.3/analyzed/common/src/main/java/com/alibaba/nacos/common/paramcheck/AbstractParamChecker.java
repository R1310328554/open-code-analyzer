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

package com.alibaba.nacos.common.paramcheck;

import java.util.List;

/**
 * 参数校验器抽象基类：构造时调用 {@link #initParamCheckRule()} 初始化规则，子类实现 {@link #checkParamInfoList} 完成具体校验逻辑。
 * The type Abstract param checker.
 *
 * @author zhuoguang
 */
public abstract class AbstractParamChecker {
    
    /** 当前校验器使用的长度与正则规则配置 */
    protected ParamCheckRule paramCheckRule;
    
    public AbstractParamChecker() {
        initParamCheckRule();
    }
    
    /**
     * Gets checker type.
     *
     * @return the checker type
      * <p>参数校验器基类；详见类级说明。</p>
     */
    /** 返回校验器类型标识，供 {@link ParamCheckerManager} 路由 */
    public abstract String getCheckerType();
    
    /**
     * Check param info list param check response.
     *
     * @param paramInfos the param infos
     * @return the param check response
      * <p>参数校验器基类；详见类级说明。</p>
     */
    /** 批量校验参数列表，返回首个失败或全部成功 */
    public abstract ParamCheckResponse checkParamInfoList(List<ParamInfo> paramInfos);
    
    /**
     * Init param check rule.
      * <p>参数校验器基类；详见类级说明。</p>
     */
    public abstract void initParamCheckRule();
}
