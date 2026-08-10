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
package org.keycloak.crl.infinispan;

import java.security.cert.X509CRL;

/**
 * Infinispan CRL 缓存条目：封装已加载的 {@link X509CRL} 及其最近一次请求时间戳。
 * <p>
 * 用于 {@link InfinispanCrlStorageProvider} 判断缓存是否仍可复用，以及避免并发重复拉取同一 CRL。
 *
 * @param crl             已解析的 X509 证书吊销列表
 * @param lastRequestTime 最近一次成功加载该 CRL 时的毫秒时间戳
 * @author rmartinc
 */
public record X509CRLEntry(X509CRL crl, long lastRequestTime) {}
