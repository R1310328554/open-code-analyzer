/*
 *
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.core.control.remote;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.control.TpsControl;
import com.alibaba.nacos.core.control.TpsControlConfig;
import com.alibaba.nacos.core.remote.AbstractRequestFilter;
import com.alibaba.nacos.plugin.control.ControlManagerCenter;
import com.alibaba.nacos.plugin.control.tps.TpsControlManager;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;
import com.alibaba.nacos.plugin.control.tps.response.TpsCheckResponse;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * RPC 请求 TPS 限流过滤器：在 {@link AbstractRequestFilter} 链路中识别 {@link TpsControl} 注解，解析并校验 TPS，超限时返回 {@link NacosException#OVER_THRESHOLD} 错误响应。
 * tps control point.
 *
 * @author liuzunfei
 * @version $Id: TpsControlRequestFilter.java, v 0.1 2021年01月09日 12:38 PM liuzunfei Exp $
 */
@Service
public class TpsControlRequestFilter extends AbstractRequestFilter {
    
    /** TPS 控制管理器，懒加载自 {@link ControlManagerCenter}。 */
    private TpsControlManager tpsControlManager;
    
    /**
     * 对带 {@link TpsControl} 的 RPC 处理器执行 TPS 校验；通过则返回 null 继续后续 Filter，失败则构造错误响应。
     *
     * @param request RPC 请求
     * @param meta 请求元数据
     * @param handlerClazz 处理器类
     * @return 限流失败时的错误响应，或 null 表示放行
     */
    @Override
    protected Response filter(Request request, RequestMeta meta, Class handlerClazz) {
        
        Method method;
        try {
            method = getHandleMethod(handlerClazz);
        } catch (NacosException e) {
            return null;
        }
        
        if (method.isAnnotationPresent(TpsControl.class)
            && TpsControlConfig.isTpsControlEnabled()) {
            
            try {
                TpsControl tpsControl = method.getAnnotation(TpsControl.class);
                String pointName = tpsControl.pointName();
                TpsCheckRequest tpsCheckRequest = null;
                String parseName =
                    StringUtils.isBlank(tpsControl.name()) ? pointName : tpsControl.name();
                RemoteTpsCheckRequestParser parser =
                    RemoteTpsCheckRequestParserRegistry.getParser(parseName);
                if (parser != null) {
                    tpsCheckRequest = parser.parse(request, meta);
                }
                if (tpsCheckRequest == null) {
                    tpsCheckRequest = new TpsCheckRequest();
                }
                if (StringUtils.isBlank(tpsCheckRequest.getPointName())) {
                    tpsCheckRequest.setPointName(pointName);
                }
                
                initTpsControlManager();
                
                TpsCheckResponse check = tpsControlManager.check(tpsCheckRequest);
                
                if (!check.isSuccess()) {
                    Response response;
                    try {
                        response = super.getDefaultResponseInstance(handlerClazz);
                        response.setErrorInfo(NacosException.OVER_THRESHOLD,
                            "Tps Flow restricted:" + check.getMessage());
                        return response;
                    } catch (Exception e) {
                        com.alibaba.nacos.plugin.control.Loggers.TPS.warn(
                            "Tps check fail , request: {},exception:{}",
                            request.getClass().getSimpleName(), e);
                        return null;
                    }
                    
                }
            } catch (Throwable throwable) {
                com.alibaba.nacos.plugin.control.Loggers.TPS.warn(
                    "Tps check exception , request: {},exception:{}",
                    request.getClass().getSimpleName(), throwable);
            }
        }
        
        return null;
    }
    
    /** 懒加载 TPS 控制管理器单例。 */
    private void initTpsControlManager() {
        if (tpsControlManager == null) {
            tpsControlManager = ControlManagerCenter.getInstance().getTpsControlManager();
        }
    }
}
