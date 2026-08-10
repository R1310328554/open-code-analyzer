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

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参数校验器管理单例：通过 {@link NacosServiceLoader} 加载 {@link AbstractParamChecker} 实现，按 checkerType 路由，未知类型回退 {@link DefaultParamChecker}。
 * The type Param checker manager.
 *
 * @author zhuoguang
 */
public class ParamCheckerManager {
    
    /** 全局单例实例 */
    private static final ParamCheckerManager INSTANCE = new ParamCheckerManager();
    
    /** 默认校验器，checkerType 为空或未注册时使用 */
    private static final AbstractParamChecker DEFAULT_PARAM_CHECKER = new DefaultParamChecker();
    
    /** checkerType → 校验器实例的并发映射 */
    private final Map<String, AbstractParamChecker> paramCheckerMap = new ConcurrentHashMap<>();
    
    private ParamCheckerManager() {
        Collection<AbstractParamChecker> paramCheckers =
            NacosServiceLoader.load(AbstractParamChecker.class);
        for (AbstractParamChecker paramChecker : paramCheckers) {
            String checkerType = paramChecker.getCheckerType();
            paramCheckerMap.put(checkerType, paramChecker);
        }
    }
    
    public static ParamCheckerManager getInstance() {
        return INSTANCE;
    }
    
    /** 按类型获取校验器，blank 或未命中时返回默认实现 */
    public AbstractParamChecker getParamChecker(String checkerType) {
        if (StringUtils.isBlank(checkerType)) {
            return DEFAULT_PARAM_CHECKER;
        }
        AbstractParamChecker paramChecker = paramCheckerMap.get(checkerType);
        if (paramChecker == null) {
            paramChecker = DEFAULT_PARAM_CHECKER;
        }
        return paramChecker;
    }
}
