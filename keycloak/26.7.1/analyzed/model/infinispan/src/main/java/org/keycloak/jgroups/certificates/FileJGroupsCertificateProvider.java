/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.jgroups.certificates;

import java.util.Objects;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.keycloak.spi.infinispan.JGroupsCertificateProvider;

import org.jgroups.util.FileWatcher;
import org.jgroups.util.SslContextFactory;

/**
 * 基于文件系统的 {@link JGroupsCertificateProvider} 实现。
 * <p>
 * 从 PKCS12 格式的密钥库与信任库文件读取证书，并通过 {@link FileWatcher} 监听文件变更，
 * 变更时自动重新加载密钥/信任管理器。
 */
public class FileJGroupsCertificateProvider implements JGroupsCertificateProvider {

    /** JGroups SslContextFactory 构建的 SSL 上下文（含文件监听）。 */
    private final SslContextFactory.Context context;

    private FileJGroupsCertificateProvider(SslContextFactory.Context context) {
        this.context = Objects.requireNonNull(context);
    }

    /**
     * 创建文件证书提供者，配置 PKCS12 密钥库/信任库及文件变更监听。
     *
     * @param keyStoreFile       密钥库路径
     * @param keyStorePassword   密钥库密码
     * @param trustStoreFile     信任库路径
     * @param trustStorePassword 信任库密码
     */
    public static FileJGroupsCertificateProvider create(String keyStoreFile, String keyStorePassword, String trustStoreFile, String trustStorePassword) {
        var context = new SslContextFactory()
                .sslProtocol("TLS")
                .keyStoreFileName(Objects.requireNonNull(keyStoreFile))
                .keyStorePassword(Objects.requireNonNull(keyStorePassword))
                .keyStoreType("pkcs12")
                .trustStoreFileName(Objects.requireNonNull(trustStoreFile))
                .trustStorePassword(Objects.requireNonNull(trustStorePassword))
                .trustStoreType("pkcs12")
                .watcher(new FileWatcher())
                .build();
        return new FileJGroupsCertificateProvider(context);
    }


    @Override
    public KeyManager keyManager() {
        return context.keyManager();
    }

    @Override
    public TrustManager trustManager() {
        return context.trustManager();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
