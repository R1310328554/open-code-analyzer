/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.keys;

import java.security.cert.X509Certificate;
import java.util.Date;

import org.keycloak.common.util.Time;
import org.keycloak.component.ComponentModel;
import org.keycloak.crypto.KeyStatus;
import org.keycloak.crypto.KeyWrapper;

import org.jboss.logging.Logger;

/**
 * 密钥组件 model note 工具类：缓存已加载 {@link KeyWrapper} 并跟踪 X509 证书过期状态。
 * <p>过期时将活跃密钥降级为 {@link KeyStatus#PASSIVE} 并禁用组件 active 标志。</p>
 *
 * @author rmartinc
 */
public class KeyNoteUtils {

    private static final Logger logger = Logger.getLogger(KeyNoteUtils.class);

    private KeyNoteUtils() {
    }

    /**
     * 在组件 model 中写入密钥缓存 note 及证书过期时间 note。
     * <p>第一个 note（{@code name}）保存 {@link KeyWrapper}；若存在证书则追加 {@code name.notAfter} 记录最早过期时间。</p>
     *
     * @param model 目标组件模型
     * @param name note 名称
     * @param key 待缓存的密钥
     */
    public static void attachKeyNotes(ComponentModel model, String name, KeyWrapper key) {
        model.setNote(name, key);
        Date notAfter = null;
        if (key.getCertificateChain() != null && !key.getCertificateChain().isEmpty()) {
            notAfter = key.getCertificateChain().stream().map(X509Certificate::getNotAfter).min(Date::compareTo).get();
        }
        if (key.getCertificate() != null) {
            if (notAfter == null) {
                notAfter = key.getCertificate().getNotAfter();
            } else {
                notAfter = notAfter.compareTo(key.getCertificate().getNotAfter()) < 0
                        ? notAfter
                        : key.getCertificate().getNotAfter();
            }
        }
        if (notAfter != null) {
            model.setNote(name + ".notAfter", notAfter);
            if (KeyStatus.ACTIVE.equals(key.getStatus())) {
                checkNotAfter(model, key, notAfter);
            }
        }
    }

    /**
     * 从 model note 检索缓存密钥；若证书已过期则将状态降为 PASSIVE。
     *
     * @param model 含 note 的组件模型
     * @param name note 名称
     * @return 缓存的 {@link KeyWrapper}，不存在时返回 null
     */
    public static KeyWrapper retrieveKeyFromNotes(ComponentModel model, String name) {
        KeyWrapper key = model.getNote(name);
        if (key != null && KeyStatus.ACTIVE.equals(key.getStatus()) && model.hasNote(name + ".notAfter")) {
            Date notAfter = model.getNote(name + ".notAfter");
            checkNotAfter(model, key, notAfter);
        }
        return key;
    }

    /** 检查证书是否过期；过期时记录警告、降级密钥并禁用 active。 */
    private static void checkNotAfter(ComponentModel model, KeyWrapper key, Date notAfter) {
        if (new Date(Time.currentTimeMillis()).compareTo(notAfter) > 0) {
            logger.warnf("Certificate chain for kid '%s' (%s) is not valid anymore, disabling it (certificate expired on %s)",
                    key.getKid(), model.getName(), notAfter);
            key.setStatus(KeyStatus.PASSIVE);
            model.put(Attributes.ACTIVE_KEY, false);
        }
    }
}
