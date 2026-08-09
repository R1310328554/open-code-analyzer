/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.motan;

import com.alibaba.csp.sentinel.adapter.motan.config.MotanAdapterGlobalConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.util.ReflectUtil;

/**
 * Motan 适配器工具类，用于构建接口与方法级 Sentinel 资源名。
 *
 * @author zhangxn8
 */
public class MotanUtils {

    private MotanUtils() {}

    /**
     * 获取 Motan RPC 方法资源名（不使用 group/version）。
     *
     * @param caller Motan 调用方
     * @param request Motan 请求
     * @return 方法资源名
     */
    public static String getMethodResourceName(Caller<?> caller, Request request){
        return getMethodResourceName(caller, request, false);
    }

    /**
     * 获取 Motan RPC 方法资源名。
     *
     * @param caller Motan 调用方
     * @param request Motan 请求
     * @param useGroupAndVersion 是否在接口名中使用 group 与 version
     * @return 方法资源名
     */
    public static String getMethodResourceName(Caller<?> caller, Request request, Boolean useGroupAndVersion) {
        StringBuilder buf = new StringBuilder(64);
        String interfaceResource = useGroupAndVersion ? caller.getUrl().getPath(): caller.getInterface().getName();
        buf.append(interfaceResource)
                .append(":")
                .append(request.getMethodName())
                .append("(");
        boolean isFirst = true;
        try {
            Class<?>[] classTypes = ReflectUtil.forNames(request.getParamtersDesc());
            for (Class<?> clazz : classTypes) {
                if (!isFirst) {
                    buf.append(",");
                }
                buf.append(clazz.getName());
                isFirst = false;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        buf.append(")");
        return buf.toString();
    }

    /**
     * 获取带前缀的 Motan RPC 方法资源名。
     *
     * @param caller Motan 调用方
     * @param request Motan 请求
     * @param prefix 资源名前缀
     * @return 方法资源名
     */
    public static String getMethodResourceName(Caller<?> caller, Request request, String prefix) {
        if (StringUtil.isNotBlank(prefix)) {
            return new StringBuilder(64)
                    .append(prefix)
                    .append(getMethodResourceName(caller, request,MotanAdapterGlobalConfig.getMotanInterfaceGroupAndVersionEnabled()))
                    .toString();
        } else {
            return getMethodResourceName(caller, request,MotanAdapterGlobalConfig.getMotanInterfaceGroupAndVersionEnabled());
        }
    }

    /**
     * 获取 Motan RPC 接口资源名（不使用 group/version）。
     *
     * @param caller Motan 调用方
     * @return 接口资源名
     */
    public static String getInterfaceName(Caller<?> caller) {
        return getInterfaceName(caller, false);
    }

    /**
     * 获取 Motan RPC 接口资源名。
     *
     * @param caller Motan 调用方
     * @param useGroupAndVersion 是否在接口名中使用 group 与 version
     * @return 接口资源名
     */
    public static String getInterfaceName(Caller<?> caller, Boolean useGroupAndVersion) {
        return useGroupAndVersion ? caller.getUrl().getApplication() : caller.getInterface().getName();
    }

    /**
     * 获取带前缀的 Motan RPC 接口资源名。
     *
     * @param caller Motan 调用方
     * @param prefix 资源名前缀
     * @return 接口资源名
     */
    public static String getInterfaceName(Caller<?> caller, String prefix) {
        if (StringUtil.isNotBlank(prefix)) {
            return new StringBuilder(64)
                    .append(prefix)
                    .append(getInterfaceName(caller, MotanAdapterGlobalConfig.getMotanInterfaceGroupAndVersionEnabled()))
                    .toString();
        } else {
            return getInterfaceName(caller, MotanAdapterGlobalConfig.getMotanInterfaceGroupAndVersionEnabled());
        }
    }

}
