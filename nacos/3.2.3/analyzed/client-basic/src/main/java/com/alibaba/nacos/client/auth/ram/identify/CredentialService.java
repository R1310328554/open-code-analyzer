/*
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
 */

package com.alibaba.nacos.client.auth.ram.identify;

import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Credential Service.
 * <p>按应用名隔离的 SPAS/RAM 凭证服务：后台 {@link CredentialWatcher} 轮询凭证文件或环境变量，变更时通知 {@link CredentialListener}。</p>
 *
 * @author Nacos
 */
public final class CredentialService implements SpasCredentialLoader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialService.class);
    
    /** appName 键到单例服务的映射（空串表示默认应用） */
    private static final ConcurrentHashMap<String, CredentialService> INSTANCES =
        new ConcurrentHashMap<>();
    
    /** 逻辑应用名，用于凭证文件路径与日志 */
    private final String appName;
    
    /** 当前持有的凭证快照 */
    private Credentials credentials = new Credentials();
    
    /** 定时检测凭证文件变更的监视器 */
    private final CredentialWatcher watcher;
    
    /** 可选的凭证更新回调 */
    private CredentialListener listener;
    
    private CredentialService(String appName) {
        if (appName == null) {
            String value = NacosClientProperties.PROTOTYPE
                .getProperty(IdentifyConstants.PROJECT_NAME_PROPERTY);
            if (StringUtils.isNotEmpty(value)) {
                appName = value;
            }
        }
        this.appName = appName;
        watcher = new CredentialWatcher(appName, this);
    }
    
    /** @return 默认应用名的凭证服务单例 */
    public static CredentialService getInstance() {
        return getInstance(null);
    }
    
    /** @param appName 应用名；null 时使用 {@link IdentifyConstants#NO_APP_NAME} 键 */
    public static CredentialService getInstance(String appName) {
        String key = appName != null ? appName : IdentifyConstants.NO_APP_NAME;
        return INSTANCES.computeIfAbsent(key, k -> new CredentialService(appName));
    }
    
    public static CredentialService freeInstance() {
        return freeInstance(null);
    }
    
    /**
     * Free instance.
     *
     * @param appName app name
     * @return {@link CredentialService}
     *         被移除的实例，不存在时为 null
     */
    public static CredentialService freeInstance(String appName) {
        String key = appName != null ? appName : IdentifyConstants.NO_APP_NAME;
        CredentialService instance = INSTANCES.remove(key);
        if (instance != null) {
            instance.free();
        }
        return instance;
    }
    
    /**
     * Free service.
     * <p>停止 Watcher 并记录释放日志。</p>
     */
    public void free() {
        if (watcher != null) {
            watcher.stop();
        }
        LOGGER.info("[{}] {} is freed", appName, this.getClass().getSimpleName());
    }
    
    @Override
    public Credentials getCredential() {
        return credentials;
    }
    
    /** 更新凭证并在内容变化时通知 listener。 */
    public void setCredential(Credentials credential) {
        boolean changed = !(credentials == credential
            || (credentials != null && credentials.identical(credential)));
        credentials = credential;
        if (changed && listener != null) {
            listener.onUpdateCredential();
        }
    }
    
    /** 设置静态凭证并停止文件监视（不再热更新）。 */
    public void setStaticCredential(Credentials credential) {
        if (watcher != null) {
            watcher.stop();
        }
        setCredential(credential);
    }
    
    /** 注册凭证变更监听器（覆盖式单 listener）。 */
    public void registerCredentialListener(CredentialListener listener) {
        this.listener = listener;
    }
}
