/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.proxy.service.cert;

import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.utils.StartAndShutdown;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.proxy.config.ConfigurationManager;
import org.apache.rocketmq.remoting.netty.TlsSystemConfig;
import org.apache.rocketmq.srvutil.FileWatchService;
import java.util.ArrayList;
import java.util.List;

/**
 * TLS 证书热更新管理器：监听证书/私钥文件变更并重载 SslContext。
 */
public class TlsCertificateManager implements StartAndShutdown {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);

    /** 证书与私钥文件的监视服务。 */
    private final FileWatchService fileWatchService;
    /** 证书重载事件监听器列表。 */
    private final List<TlsContextReloadListener> reloadListeners = new ArrayList<>();

    /** 按 Proxy 配置初始化证书/私钥路径的文件监视。 */
    public TlsCertificateManager() {
        try {
            this.fileWatchService = new FileWatchService(
                new String[] {
                    ConfigurationManager.getProxyConfig().getTlsCertPath(),
                    ConfigurationManager.getProxyConfig().getTlsKeyPath()
                },
                new CertKeyFileWatchListener(),
                ConfigurationManager.getProxyConfig().getTlsCertWatchIntervalMs()
            );
        } catch (Exception e) {
            log.error("Failed to initialize TLS certificate watch service", e);
            throw new RuntimeException("Failed to initialize TLS certificate manager", e);
        }
    }

    public FileWatchService getFileWatchService() {
        return this.fileWatchService;
    }

    /** 注册 TLS 上下文重载监听器。 */
    public void registerReloadListener(TlsContextReloadListener listener) {
        if (listener != null) {
            this.reloadListeners.add(listener);
        }
    }

    /** 移除已注册的 TLS 重载监听器。 */
    public void unregisterReloadListener(TlsContextReloadListener listener) {
        if (listener != null) {
            this.reloadListeners.remove(listener);
        }
    }

    public List<TlsContextReloadListener> getReloadListeners() {
        return this.reloadListeners;
    }

    @Override
    /** 启动文件监视并记录监听路径。 */
    public void start() throws Exception {
        this.fileWatchService.start();
        log.info("TLS certificate manager started successfully, start watching: {} {}",
            ConfigurationManager.getProxyConfig().getTlsCertPath(),
            ConfigurationManager.getProxyConfig().getTlsKeyPath()
        );
    }

    @Override
    /** 停止文件监视服务。 */
    public void shutdown() throws Exception {
        this.fileWatchService.shutdown();
        log.info("TLS certificate manager shutdown successfully");
    }

    private class CertKeyFileWatchListener implements FileWatchService.Listener {
        private boolean certChanged = false;
        private boolean keyChanged = false;

        @Override
        public void onChanged(String path) {
            log.info("File changed: {}", path);
            if (path.equals(TlsSystemConfig.tlsServerCertPath)) {
                certChanged = true;
            } else if (path.equals(TlsSystemConfig.tlsServerKeyPath)) {
                keyChanged = true;
            }

            if (certChanged && keyChanged) {
                log.info("The certificate and private key changed, reload the ssl context");
                notifyContextReload();
                certChanged = false;
                keyChanged = false;
            }
        }

        private void notifyContextReload() {
            for (TlsContextReloadListener listener : reloadListeners) {
                try {
                    listener.onTlsContextReload();
                } catch (Throwable e) {
                    log.error("Failed to notify TLS context reload to listener: " + listener, e);
                }
            }
        }
    }

    // TLS 上下文重载事件监听接口
    /** 证书与私钥均更新后触发 SslContext 重载。 */
    public interface TlsContextReloadListener {
        /** 执行 TLS 上下文热重载回调。 */
        void onTlsContextReload();
    }
}
