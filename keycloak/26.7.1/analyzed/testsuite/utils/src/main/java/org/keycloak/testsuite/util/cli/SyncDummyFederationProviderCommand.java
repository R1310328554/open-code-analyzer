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

package org.keycloak.testsuite.util.cli;

import java.util.Objects;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.StorageProviderRealmModel;
import org.keycloak.storage.StoreSyncEvent;
import org.keycloak.storage.UserStorageProviderModel;

/**
 * 创建或更新虚拟用户联邦提供者并触发同步的测试命令，用于集群同步场景。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SyncDummyFederationProviderCommand extends AbstractCommand {

    /**
     * 在 master 领域中配置 {@code cluster-dummy} 联邦组件并触发 {@link StoreSyncEvent}。
     * 参数：同步提交前等待秒数、变更同步周期（秒）。
     */
    @Override
    protected void doRunCommand(KeycloakSession session) {
        int waitTime = getIntArg(0);
        int changedSyncPeriod = getIntArg(1);

        RealmModel realm = session.realms().getRealmByName("master");
        UserStorageProviderModel fedProviderModel = findUserStorageProviderByName(session, "cluster-dummy", realm);
        if (fedProviderModel == null) {
            MultivaluedHashMap<String, String> cfg = fedProviderModel.getConfig();
            updateConfig(cfg, waitTime);

            UserStorageProviderModel model = new UserStorageProviderModel();
            model.setProviderId("sync-dummy");
            model.setPriority(1);
            model.setName("cluster-dummy");
            model.setFullSyncPeriod(-1);
            model.setChangedSyncPeriod(changedSyncPeriod);
            model.setLastSync(-1);
            fedProviderModel = new UserStorageProviderModel(realm.addComponentModel(model));
        } else {
            MultivaluedHashMap<String, String> cfg = fedProviderModel.getConfig();
            updateConfig(cfg, waitTime);
            fedProviderModel.setChangedSyncPeriod(changedSyncPeriod);
            realm.updateComponent(fedProviderModel);
        }

        StoreSyncEvent.fire(session, realm, fedProviderModel, false);

        log.infof("User federation provider created and sync was started", waitTime);
    }

    /** 将 wait-time 配置项写入联邦提供者配置。 */
    private void updateConfig(MultivaluedHashMap<String, String> cfg, int waitTime) {
        cfg.putSingle("wait-time", String.valueOf(waitTime));
    }

    /**
     * 按显示名称在领域中查找用户存储提供者模型。
     *
     * @param session Keycloak 会话
     * @param displayName 提供者显示名
     * @param realm 目标领域
     * @return 匹配的提供者模型，未找到时返回 {@code null}
     */
    public static UserStorageProviderModel findUserStorageProviderByName(KeycloakSession session, String displayName, RealmModel realm) {
        if (displayName == null) {
            return null;
        }

        return ((StorageProviderRealmModel) realm).getUserStorageProvidersStream()
                .filter(fedProvider -> Objects.equals(fedProvider.getName(), displayName))
                .findFirst()
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "startSyncDummy";
    }

    /** {@inheritDoc} */
    @Override
    public String printUsage() {
        return super.printUsage() + " <wait-time-before-sync-commit-in-seconds> <changed-sync-period-in-seconds>";
    }
}
