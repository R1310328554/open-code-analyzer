/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.operator.controllers;

import org.keycloak.operator.crds.v2alpha1.client.KeycloakSAMLClient;
import org.keycloak.operator.crds.v2alpha1.client.KeycloakSAMLClientRepresentation;
import org.keycloak.representations.admin.v2.SAMLClientRepresentation;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;

@ControllerConfiguration
/**
 * SAML 客户端控制器：将 {@link KeycloakSAMLClient} CR 同步至 Keycloak Admin API v2。
 */
public class KeycloakSAMLClientController extends KeycloakClientBaseController<KeycloakSAMLClient, SAMLClientRepresentation, KeycloakSAMLClientRepresentation> {

    @Override
    Class<SAMLClientRepresentation> getTargetRepresentation() {
        return SAMLClientRepresentation.class;
    }

    @Override
    /** SAML 客户端无需额外预处理，也不触发轮询。 */
    boolean prepareRepresentation(KeycloakSAMLClientRepresentation crRepresentation,
            SAMLClientRepresentation targetRepresentation, Context<?> context) {
        // 无需额外处理，也不轮询
        return false;
    }

}
