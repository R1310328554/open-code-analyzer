/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.adapters.saml.rotation;

import java.security.Key;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.security.auth.x500.X500Principal;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyName;

import org.keycloak.adapters.cloned.HttpAdapterUtils;
import org.keycloak.adapters.cloned.HttpClientAdapterException;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.dom.saml.v2.metadata.KeyTypes;
import org.keycloak.rotation.KeyLocator;
import org.keycloak.saml.processing.api.util.KeyInfoTools;

import org.apache.http.client.HttpClient;
import org.jboss.logging.Logger;

/**
 * 基于 IdP SAML 元数据描述符的公钥定位器（{@link KeyLocator}）。
 *
 * <p>从 IdP 的 SAML descriptor URL（如
 * {@code http://{host}/auth/realms/{realm}/protocol/saml/descriptor}）
 * 拉取并缓存签名证书，用于校验 SAML 请求/响应签名。</p>
 *
 * <p>实现思路参考 {@code JWKPublicKeyLocator}。</p>
 *
 * @author hmlnarik
 */
public class SamlDescriptorPublicKeyLocator implements KeyLocator {

    /** 本类日志记录器。 */
    private static final Logger LOG = Logger.getLogger(SamlDescriptorPublicKeyLocator.class);

    /**
     * 两次描述符请求之间的最小间隔（秒）。
     */
    private final int minTimeBetweenDescriptorRequests;

    /**
     * 缓存条目的生存时间（秒）。
     */
    private final int cacheEntryTtl;

    /**
     * SAML 描述符 URL。
     */
    private final String descriptorUrl;

    /** 按 KeyName 索引的公钥缓存。 */
    private final Map<String, Key> publicKeyCacheByName = new ConcurrentHashMap<>();
    /** 按密钥哈希索引的公钥缓存。 */
    private final Map<KeyHash, Key> publicKeyCacheByKey = new ConcurrentHashMap<>();

    /** 用于下载描述符的 HTTP 客户端。 */
    private final HttpClient client;

    /** 上次请求描述符的时间戳（秒）。 */
    private volatile int lastRequestTime = 0;

    /**
     * 构造描述符公钥定位器。
     *
     * @param descriptorUrl                    SAML 描述符 URL
     * @param minTimeBetweenDescriptorRequests 两次请求最小间隔（秒），≤0 时默认 20
     * @param cacheEntryTtl                      缓存 TTL（秒）
     * @param httpClient                         HTTP 客户端
     */
    public SamlDescriptorPublicKeyLocator(String descriptorUrl, int minTimeBetweenDescriptorRequests, int cacheEntryTtl, HttpClient httpClient) {
        this.minTimeBetweenDescriptorRequests = minTimeBetweenDescriptorRequests <= 0
          ? 20
          : minTimeBetweenDescriptorRequests;

        this.descriptorUrl = descriptorUrl;
        this.cacheEntryTtl = cacheEntryTtl;

        this.client = httpClient;
    }

    /**
     * 按密钥 ID（kid）查找公钥。
     *
     * @param kid 密钥标识
     * @return 匹配的公钥；无效 kid 时返回 {@code null}
     */
    @Override
    public Key getKey(String kid) throws KeyManagementException {
        if (kid == null) {
            LOG.debugf("Invalid key id: %s", kid);
            return null;
        }
        return getKey(kid, publicKeyCacheByName);
    }

    /**
     * 按已有密钥对象查找对应的缓存公钥。
     *
     * @param key 参考密钥
     * @return 缓存中的公钥；{@code key} 为 {@code null} 时返回 {@code null}
     */
    @Override
    public Key getKey(Key key) throws KeyManagementException {
        if (key == null) {
            return null;
        }
        return getKey(new KeyHash(key), publicKeyCacheByKey);
    }

    /** 通用缓存查找：必要时刷新描述符并返回密钥。 */
    private <T> Key getKey(T key, Map<T, Key> cache) throws KeyManagementException {
        LOG.tracef("Requested key: %s", key);

        int currentTime = Time.currentTime();

        Key res;
        if (currentTime > this.lastRequestTime + this.cacheEntryTtl) {
            LOG.debugf("Performing regular cache cleanup.");
            res = refreshCertificateCacheAndGet(key, cache, currentTime);
        } else {
            res = cache.get(key);

            if (res == null) {
                if (currentTime > this.lastRequestTime + this.minTimeBetweenDescriptorRequests) {
                    res = refreshCertificateCacheAndGet(key, cache, currentTime);
                } else {
                    LOG.debugf("Won't send request to realm SAML descriptor url, timeout not expired. Last request time was %d", lastRequestTime);
                }
            }
        }

        return res;
    }

    /** 强制清空缓存并从描述符重新拉取证书。 */
    @Override
    public synchronized void refreshKeyCache() {
        LOG.info("Forcing key cache cleanup and refresh.");
        this.publicKeyCacheByName.clear();
        this.publicKeyCacheByKey.clear();
        refreshCertificateCacheAndGet(null, this.publicKeyCacheByKey, Time.currentTime());
    }

    /** 从 SAML 描述符刷新证书缓存并返回指定键对应的公钥。 */
    private synchronized <T> Key refreshCertificateCacheAndGet(T key, Map<T, Key> cache, int currentTime) {
        if (this.descriptorUrl == null || currentTime <= this.lastRequestTime + this.minTimeBetweenDescriptorRequests) {
            // 无描述符 URL 或距上次请求间隔过短
            return key == null ? null : cache.get(key);
        }

        this.lastRequestTime = Time.currentTime();

        LOG.debugf("Refreshing public key cache from %s", this.descriptorUrl);
        List<KeyInfo> signingCerts;
        try {
            MultivaluedHashMap<String, KeyInfo> certs = HttpAdapterUtils.downloadKeysFromSamlDescriptor(client, this.descriptorUrl);
            signingCerts = certs.get(KeyTypes.SIGNING.value());
        } catch (HttpClientAdapterException ex) {
            LOG.error("Could not refresh certificates from the server", ex);
            return null;
        }

        if (signingCerts == null) {
            return null;
        }

        LOG.debugf("Certificates retrieved from server, filling public key cache");

        // 确认描述符读取成功后再清空旧缓存
        this.publicKeyCacheByName.clear();
        this.publicKeyCacheByKey.clear();

        for (KeyInfo ki : signingCerts) {
            KeyName keyName = KeyInfoTools.getKeyName(ki);
            X509Certificate x509certificate = KeyInfoTools.getX509Certificate(ki);
            if (x509certificate == null) {
                continue;
            }
            try {
                x509certificate.checkValidity();
            } catch (CertificateException ex) {
                continue;
            }

            if (keyName != null) {
                LOG.tracef("Registering signing certificate %s", keyName.getName());
                this.publicKeyCacheByName.put(keyName.getName(), x509certificate.getPublicKey());
                this.publicKeyCacheByKey.put(new KeyHash(x509certificate.getPublicKey()), x509certificate.getPublicKey());
            } else {
                final X500Principal principal = x509certificate.getSubjectX500Principal();
                String name = (principal == null ? "unnamed" : principal.getName()) + "@" + x509certificate.getSerialNumber() + "$" + SecretGenerator.getInstance().generateSecureID();
                this.publicKeyCacheByName.put(name, x509certificate.getPublicKey());
                this.publicKeyCacheByKey.put(new KeyHash(x509certificate.getPublicKey()), x509certificate.getPublicKey());
                LOG.tracef("Adding certificate %s without a specific key name: %s", name, x509certificate);
            }
        }

        return key == null ? null : cache.get(key);
    }

    @Override
    public String toString() {
        return "Keys retrieved from SAML descriptor at " + descriptorUrl;
    }

    @Override
    public Iterator<Key> iterator() {
        int currentTime = Time.currentTime();
        if (currentTime > this.lastRequestTime + this.cacheEntryTtl) {
            LOG.debugf("Performing regular cache cleanup.");
            refreshCertificateCacheAndGet(null, publicKeyCacheByName, currentTime);
        }

        return this.publicKeyCacheByKey.values().iterator();
    }
}
