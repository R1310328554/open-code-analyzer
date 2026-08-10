/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.utils;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.plugin.auth.impl.configuration.AuthConfigs;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.file.FileChangeEvent;
import com.alibaba.nacos.sys.file.FileWatcher;
import com.alibaba.nacos.sys.file.WatchFileCenter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 远程 Nacos 集群地址与 HTTP 辅助工具。
 *
 * <p>从 {@code cluster.conf} 读取节点列表并监听变更， 提供轮询选取、上下文路径及 {@link AuthConfigs} 身份头构造。</p>
 *
 * @author xiweng.yy
 */
public class RemoteServerUtil {
    
    /** 远程控制台默认上下文路径。 */
    private static final String DEFAULT_REMOTE_SERVER_CONTEXT_PATH = "/nacos";
    
    /** 集群节点地址列表（可变，由文件监听刷新）。 */
    private static List<String> serverAddresses = new LinkedList<>();
    
    /** 轮询下标，用于 {@link #getOneNacosServerAddress()}。 */
    private static AtomicInteger index = new AtomicInteger();
    
    private static String remoteServerContextPath = DEFAULT_REMOTE_SERVER_CONTEXT_PATH;
    
    static {
        readRemoteServerAddress();
        registerWatcher();
        initRemoteServerContextPath();
    }
    
    /** 从环境配置读取远程控制台 context-path。 */
    private static void initRemoteServerContextPath() {
        if (EnvUtil.getEnvironment() == null) {
            return;
        }
        remoteServerContextPath =
            EnvUtil.getProperty("nacos.console.remote.server.context-path",
                DEFAULT_REMOTE_SERVER_CONTEXT_PATH);
    }
    
    /** 注册 cluster.conf 文件变更监听器。 */
    private static void registerWatcher() {
        try {
            WatchFileCenter.registerWatcher(EnvUtil.getClusterConfFilePath(), new FileWatcher() {
                
                @Override
                public void onChange(FileChangeEvent event) {
                    readRemoteServerAddress();
                }
                
                @Override
                public boolean interest(String context) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }
    
    /** 从 cluster.conf 重新加载集群节点地址。 */
    public static void readRemoteServerAddress() {
        try {
            serverAddresses = EnvUtil.readClusterConf();
        } catch (IOException ignored) {
        }
    }
    
    /** 返回集群地址列表的副本。 */
    public static List<String> getServerAddresses() {
        return new LinkedList<>(serverAddresses);
    }
    
    /** 轮询返回一个集群节点地址。 */
    public static String getOneNacosServerAddress() {
        int actual = index.getAndUpdate(operand -> (operand + 1) % serverAddresses.size());
        return serverAddresses.get(actual);
    }
    
    /** 获取远程控制台上下文路径。 */
    public static String getRemoteServerContextPath() {
        return remoteServerContextPath;
    }
    
    /**
     * 校验 HTTP 响应，失败时封装为 {@link NacosException} 抛出。
     *
     * @param result http execute result
     * @throws NacosException wrapper result as NacosException
     */
    public static void singleCheckResult(HttpRestResult<String> result) throws NacosException {
        if (result.ok()) {
            return;
        }
        throw new NacosException(result.getCode(), result.getMessage());
    }
    
    /**
     * 根据 {@link AuthConfigs} 构造远程服务端身份认证请求头。
     *
     * @param authConfigs authConfigs
     * @return remote server identity header
     */
    public static Header buildServerRemoteHeader(AuthConfigs authConfigs) {
        Header header = Header.newInstance();
        if (StringUtils.isNotBlank(authConfigs.getServerIdentityKey())) {
            header.addParam(authConfigs.getServerIdentityKey(),
                authConfigs.getServerIdentityValue());
        }
        return header;
    }
}
