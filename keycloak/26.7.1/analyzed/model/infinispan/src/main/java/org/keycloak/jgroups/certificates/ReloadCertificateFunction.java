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

import java.util.function.Function;

import org.keycloak.marshalling.Marshalling;

import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 集群广播函数：通知各节点重新加载 JGroups mTLS 证书。
 * <p>
 * 通过 Infinispan Protobuf 序列化，在 {@link EmbeddedCacheManager} 上查找
 * {@link CertificateReloadManager} 并触发 {@link CertificateReloadManager#reloadCertificate()}。
 */
@ProtoTypeId(Marshalling.RELOAD_CERTIFICATE_FUNCTION)
public final class ReloadCertificateFunction implements Function<EmbeddedCacheManager, Void> {

    /** 单例实例，供 Protobuf 反序列化使用。 */
    private static final ReloadCertificateFunction INSTANCE = new ReloadCertificateFunction();

    private ReloadCertificateFunction() {}

    @ProtoFactory
    public static ReloadCertificateFunction getInstance() {
        return INSTANCE;
    }

    @Override
    /** 在目标节点上触发证书重载；管理器不存在时静默跳过。 */
    public Void apply(EmbeddedCacheManager embeddedCacheManager) {
        var crm = GlobalComponentRegistry.componentOf(embeddedCacheManager, CertificateReloadManager.class);
        if (crm != null) {
            crm.reloadCertificate();
        }
        return null;
    }
}
