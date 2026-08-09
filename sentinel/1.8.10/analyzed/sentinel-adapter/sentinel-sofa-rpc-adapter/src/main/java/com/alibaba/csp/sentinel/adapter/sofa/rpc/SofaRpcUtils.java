/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.sofa.rpc;

import com.alipay.sofa.rpc.common.RemotingConstants;
import com.alipay.sofa.rpc.core.request.SofaRequest;

/**
 * SOFARPC 适配器工具类，用于构建接口与方法级 Sentinel 资源名。
 *
 * @author cdfive
 */
public class SofaRpcUtils {

    /**
     * 从请求头获取调用方应用名。
     *
     * @param request SOFARPC 请求
     * @return 应用名，不存在时返回空字符串
     */
    public static String getApplicationName(SofaRequest request) {
        String appName = (String) request.getRequestProp(RemotingConstants.HEAD_APP_NAME);
        return appName == null ? "" : appName;
    }

    /**
     * 获取接口级 Sentinel 资源名。
     *
     * @param request SOFARPC 请求
     * @return 接口资源名
     */
    public static String getInterfaceResourceName(SofaRequest request) {
        return request.getInterfaceName();
    }

    /**
     * 获取方法级 Sentinel 资源名，格式为 接口名#方法名(参数签名)。
     *
     * @param request SOFARPC 请求
     * @return 方法资源名
     */
    public static String getMethodResourceName(SofaRequest request) {
        StringBuilder buf = new StringBuilder(64);
        buf.append(request.getInterfaceName())
                .append("#")
                .append(request.getMethodName())
                .append("(");

        boolean isFirst = true;
        for (String methodArgSig : request.getMethodArgSigs()) {
            if (!isFirst) {
                buf.append(",");
            } else {
                isFirst = false;
            }

            buf.append(methodArgSig);
        }
        buf.append(")");
        return buf.toString();
    }

    /**
     * 获取方法调用参数数组，用于热点参数流控。
     *
     * @param request SOFARPC 请求
     * @return 方法参数数组
     */
    public static Object[] getMethodArguments(SofaRequest request) {
        return request.getMethodArgs();
    }

    private SofaRpcUtils() {}
}
