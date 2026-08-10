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

package com.alibaba.nacos.naming.utils;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.context.RequestContextHolder;
import com.alibaba.nacos.core.context.addition.AddressContext;
import com.alibaba.nacos.core.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

/**
 * 命名服务请求工具类。
 *
 * <p>从 {@link RequestContextHolder} 或 HTTP/gRPC 请求中提取客户端源 IP，并校验实例注册权重是否在合法区间内。</p>
 *
 * @author xiweng.yy
 */
public class NamingRequestUtil {
    
    /**
     * 从请求上下文中获取客户端源 IP。
     *
     * <p>优先取 {@link AddressContext#getSourceIp()}，为空时回退到 remoteIp。</p>
     *
     * @return source ip, null if not found
     */
    public static String getSourceIp() {
        AddressContext addressContext =
            RequestContextHolder.getContext().getBasicContext().getAddressContext();
        String sourceIp = addressContext.getSourceIp();
        if (StringUtils.isBlank(sourceIp)) {
            sourceIp = addressContext.getRemoteIp();
        }
        return sourceIp;
    }
    
    /**
     * 获取 HTTP 请求的客户端源 IP。
     *
     * <p>上下文无 IP 时通过 {@link WebUtils#getRemoteIp} 从 Servlet 请求解析。</p>
     *
     * @param httpServletRequest http request
     * @return source ip, null if not found
     */
    public static String getSourceIpForHttpRequest(HttpServletRequest httpServletRequest) {
        String sourceIp = getSourceIp();
        // 上下文未携带 IP 时，从 HTTP 请求头/连接信息解析。
        if (StringUtils.isBlank(sourceIp)) {
            sourceIp = WebUtils.getRemoteIp(httpServletRequest);
        }
        return sourceIp;
    }
    
    /**
     * 获取 gRPC 请求的客户端源 IP。
     *
     * <p>上下文无 IP 时使用 {@link RequestMeta#getClientIp()}。</p>
     *
     * @param meta grpc request meta
     * @return source ip, null if not found
     */
    public static String getSourceIpForGrpcRequest(RequestMeta meta) {
        String sourceIp = getSourceIp();
        // 上下文未携带 IP 时，从 gRPC RequestMeta 读取 clientIp。
        if (StringUtils.isBlank(sourceIp)) {
            sourceIp = meta.getClientIp();
        }
        return sourceIp;
    }
    
    /**
     * 校验实例权重是否在允许范围内。
     *
     * <p>超出 {@link com.alibaba.nacos.naming.constants.Constants} 定义的最小/最大权重时抛出 {@link NacosApiException}。</p>
     *
     * @param weight weight from request
     * @throws NacosException if weight is invalid
     */
    public static void checkWeight(Double weight) throws NacosException {
        if (weight > com.alibaba.nacos.naming.constants.Constants.MAX_WEIGHT_VALUE
            || weight < com.alibaba.nacos.naming.constants.Constants.MIN_WEIGHT_VALUE) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.WEIGHT_ERROR,
                "instance format invalid: The weights range from "
                    + com.alibaba.nacos.naming.constants.Constants.MIN_WEIGHT_VALUE + " to "
                    + com.alibaba.nacos.naming.constants.Constants.MAX_WEIGHT_VALUE);
        }
    }
}
