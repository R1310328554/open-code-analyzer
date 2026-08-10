/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.managers;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.partialimport.PartialImportManager;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.storage.ImportRealmFromRepresentationEvent;
import org.keycloak.storage.PartialImportRealmFromRepresentationEvent;
import org.keycloak.storage.SetDefaultsForNewRealm;

/**
 * 领域管理 Provider 工厂，监听 {@link ImportRealmFromRepresentationEvent} 等领域相关事件。
 * <p>在旧版存储迁移完成后若不再需要可移除。</p>
 *
 * @author Alexander Schwartz
 */
@Deprecated
public class RealmManagerProviderFactory implements ProviderFactory<RealmManagerProviderFactory>, Provider {
    /** {@inheritDoc} 本工厂仅监听事件，不应被实例化 */
    @Override
    public RealmManagerProviderFactory create(KeycloakSession session) {
        throw new ModelException("This shouldn't be instantiated, this should only listen to events");
    }

    @Override
    public void init(Config.Scope config) {
    }

    /** {@inheritDoc} 注册领域导入、部分导入及新领域默认值事件处理器 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        factory.register(event -> {
            if (event instanceof ImportRealmFromRepresentationEvent) {
                ImportRealmFromRepresentationEvent importRealmFromRepresentationEvent = (ImportRealmFromRepresentationEvent) event;
                RealmModel realmModel = new RealmManager(importRealmFromRepresentationEvent.getSession()).importRealm(importRealmFromRepresentationEvent.getRealmRepresentation());
                importRealmFromRepresentationEvent.setRealmModel(realmModel);
            } else if (event instanceof PartialImportRealmFromRepresentationEvent) {
                PartialImportRealmFromRepresentationEvent partialImportRealmFromRepresentationEvent = (PartialImportRealmFromRepresentationEvent) event;
                PartialImportManager partialImportManager = new PartialImportManager(partialImportRealmFromRepresentationEvent.getRep(), partialImportRealmFromRepresentationEvent.getSession(), partialImportRealmFromRepresentationEvent.getRealm());
                partialImportRealmFromRepresentationEvent.setPartialImportResults(partialImportManager.saveResources());
            } else if (event instanceof SetDefaultsForNewRealm) {
                SetDefaultsForNewRealm setDefaultsForNewRealm = (SetDefaultsForNewRealm) event;
                new RealmManager(setDefaultsForNewRealm.getSession()).setDefaultsForNewRealm(setDefaultsForNewRealm.getRealmModel());
            }
        });
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "default";
    }
}
