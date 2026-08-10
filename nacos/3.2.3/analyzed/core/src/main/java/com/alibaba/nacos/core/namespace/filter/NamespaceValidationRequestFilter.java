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

package com.alibaba.nacos.core.namespace.filter;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.paramcheck.ParamInfo;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.paramcheck.AbstractRpcParamExtractor;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.core.remote.AbstractRequestFilter;
import com.alibaba.nacos.core.service.NamespaceOperationService;

import com.alibaba.nacos.core.utils.Loggers;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * RPC 请求过滤器：对标注 {@link NamespaceValidation} 的处理器方法，校验请求中的 namespace 是否已创建。
 * Namespace validation request filter for NamingRequest.
 *
 * @author FangYuan
 * @since 2025-08-11 21:51:29
 */
@Component
public class NamespaceValidationRequestFilter extends AbstractRequestFilter {
    
    /** 命名空间查询服务，用于判断 namespace 是否存在。 */
    private final NamespaceOperationService namespaceOperationService;
    
    /**
     * 注入命名空间操作服务。
     *
     * @param namespaceOperationService 命名空间 CRUD 服务
     */
    public NamespaceValidationRequestFilter(NamespaceOperationService namespaceOperationService) {
        this.namespaceOperationService = namespaceOperationService;
    }
    
    @Override
    protected Response filter(Request request, RequestMeta meta, Class handlerClazz)
        throws NacosException {
        try {
            // 检查全局命名空间校验开关
            boolean namespaceValidationEnabled =
                NamespaceValidationConfig.getInstance().isNamespaceValidationEnabled();
            if (!namespaceValidationEnabled) {
                return null;
            }
            
            // 检查方法是否标注 @NamespaceValidation 且局部开关开启
            Method method = getHandleMethod(handlerClazz);
            if (method.isAnnotationPresent(NamespaceValidation.class)) {
                NamespaceValidation namespaceValidation =
                    method.getAnnotation(NamespaceValidation.class);
                if (!namespaceValidation.enable()) {
                    return null;
                }
                
                List<ParamInfo> paramInfoList = extractNamespaceParam(request, handlerClazz);
                if (CollectionUtils.isEmpty(paramInfoList)) {
                    return null;
                }
                
                for (ParamInfo paramInfo : paramInfoList) {
                    // namespace 为空或空白时跳过校验（兼容 public/default namespace）
                    String namespaceId = paramInfo.getNamespaceId();
                    if (StringUtils.isBlank(namespaceId)) {
                        continue;
                    }
                    
                    boolean exist = namespaceOperationService.namespaceExists(namespaceId);
                    if (!exist) {
                        Response response = super.getDefaultResponseInstance(handlerClazz);
                        response.setErrorInfo(ErrorCode.NAMESPACE_NOT_EXIST.getCode(),
                            String.format(
                                "Namespace '%s' does not exist. Please create the namespace first.",
                                namespaceId));
                        
                        return response;
                    }
                }
            }
        } catch (Exception e) {
            Loggers.CORE.warn("Namespace validation error for request: {}, exception: {}", request,
                e);
        }
        
        return null;
    }
    
    /**
     * 通过 {@link ExtractorManager} 从请求中提取含 namespaceId 的参数列表。
     *
     * @param request RPC 请求
     * @param handlerClazz 处理器类
     * @return 参数信息列表，无 Extractor 时返回 null
     */
    @SuppressWarnings("unchecked")
    private List<ParamInfo> extractNamespaceParam(Request request, Class handlerClazz)
        throws NacosException {
        ExtractorManager.Extractor extractor =
            getHandleMethod(handlerClazz).getAnnotation(ExtractorManager.Extractor.class);
        if (extractor == null) {
            extractor = (ExtractorManager.Extractor) handlerClazz
                .getAnnotation(ExtractorManager.Extractor.class);
            if (extractor == null) {
                return null;
            }
        }
        AbstractRpcParamExtractor paramExtractor = ExtractorManager.getRpcExtractor(extractor);
        return paramExtractor.extractParam(request);
    }
}
