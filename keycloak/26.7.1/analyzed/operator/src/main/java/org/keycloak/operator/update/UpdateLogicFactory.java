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

package org.keycloak.operator.update;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.keycloak.operator.controllers.KeycloakUpdateJobDependentResource;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.deployment.spec.UpdateSpec;
import org.keycloak.operator.update.impl.AutoUpdateLogic;
import org.keycloak.operator.update.impl.ExplicitUpdateLogic;
import org.keycloak.operator.update.impl.RecreateOnImageChangeUpdateLogic;

import io.javaoperatorsdk.operator.api.reconciler.Context;

/**
 * {@link UpdateLogic} 工厂，根据 Keycloak CR 中配置的 {@link UpdateStrategy} 返回对应实现。
 */
@ApplicationScoped
public class UpdateLogicFactory {
    @Inject
    KeycloakUpdateJobDependentResource updateJobDependentResource;

    /**
     * 按 CR 更新策略实例化更新逻辑。
     *
     * @param keycloak 当前协调的 Keycloak CR
     * @param context 协调上下文
     * @return 与策略匹配的 {@link UpdateLogic} 实现
     */
    public UpdateLogic create(Keycloak keycloak, Context<Keycloak> context) {
        var strategy = UpdateSpec.getUpdateStrategy(keycloak);
        return switch (strategy) {
            case RECREATE_ON_IMAGE_CHANGE -> new RecreateOnImageChangeUpdateLogic(context, keycloak);
            case AUTO -> new AutoUpdateLogic(context, keycloak, updateJobDependentResource);
            case EXPLICIT -> new ExplicitUpdateLogic(context, keycloak);
        };
    }

}
