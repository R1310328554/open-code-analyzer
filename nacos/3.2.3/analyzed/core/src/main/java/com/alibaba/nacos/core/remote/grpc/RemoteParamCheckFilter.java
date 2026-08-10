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

package com.alibaba.nacos.core.remote.grpc;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.common.paramcheck.AbstractParamChecker;
import com.alibaba.nacos.common.paramcheck.ParamCheckResponse;
import com.alibaba.nacos.common.paramcheck.ParamCheckerManager;
import com.alibaba.nacos.common.paramcheck.ParamInfo;
import com.alibaba.nacos.core.paramcheck.AbstractRpcParamExtractor;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.core.paramcheck.ServerParamCheckConfig;
import com.alibaba.nacos.core.remote.AbstractRequestFilter;
import com.alibaba.nacos.plugin.control.Loggers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 远程 RPC 请求参数校验过滤器：在 {@link AbstractRequestFilter} 链中拦截请求，
 * 通过 {@link ExtractorManager} 提取参数并由 {@link ParamCheckerManager} 执行校验。
 * The type Remote param check filter.
 *
 * @author zhuoguang
 */
@Component
public class RemoteParamCheckFilter extends AbstractRequestFilter {
    
    /** 执行参数校验；未启用校验或无 Extractor 注解时返回 null 放行。 */
    @Override
    protected Response filter(Request request, RequestMeta meta, Class handlerClazz)
        throws NacosException {
        // 读取服务端是否开启参数校验开关
        boolean paramCheckEnabled = ServerParamCheckConfig.getInstance().isParamCheckEnabled();
        if (!paramCheckEnabled) {
            return null;
        }
        try {
            // 优先从处理方法获取 @Extractor 注解
            ExtractorManager.Extractor extractor =
                getHandleMethod(handlerClazz).getAnnotation(ExtractorManager.Extractor.class);
            if (extractor == null) {
                extractor = (ExtractorManager.Extractor) handlerClazz
                    .getAnnotation(ExtractorManager.Extractor.class);
                if (extractor == null) {
                    return null;
                }
            }
            // 根据注解获取对应 RPC 参数提取器
            AbstractRpcParamExtractor paramExtractor = ExtractorManager.getRpcExtractor(extractor);
            List<ParamInfo> paramInfoList = paramExtractor.extractParam(request);
            ParamCheckerManager paramCheckerManager = ParamCheckerManager.getInstance();
            AbstractParamChecker paramChecker = paramCheckerManager.getParamChecker(
                ServerParamCheckConfig.getInstance().getActiveParamChecker());
            // 调用活跃校验器检查参数列表
            ParamCheckResponse checkResponse = paramChecker.checkParamInfoList(paramInfoList);
            if (!checkResponse.isSuccess()) {
                return generateFailResponse(request, checkResponse.getMessage(), handlerClazz);
            }
        } catch (Exception e) {
            return generateFailResponse(request, e.getMessage(), handlerClazz);
        }
        return null;
    }
    
    /** 构造校验失败响应并记录 CONTROL 日志。 */
    private Response generateFailResponse(Request request, String message, Class handlerClazz) {
        Response response;
        try {
            response = super.getDefaultResponseInstance(handlerClazz);
            response.setErrorInfo(NacosException.INVALID_PARAM,
                "Param check invalid:" + message);
            Loggers.CONTROL.info("Param check invalid,{},request:{}:", message,
                request.getClass().getSimpleName());
            return response;
        } catch (Exception e) {
            Loggers.CONTROL.error("Param check fail ,request:{}",
                request.getClass().getSimpleName(), e);
            return null;
        }
    }
}
