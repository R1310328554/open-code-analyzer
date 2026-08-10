/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authentication.authenticators.access;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;

/**
 * Authenticator will always successfully authenticate.
 * Useful for example in the conditional flows to be used after satisfying the previous conditions.
 *
 * @author <a href="mailto:mabartos@redhat.com">Martin Bartos</a>
 */
public class AllowAccessAuthenticator implements Authenticator {
    private final Logger log = Logger.getLogger(AllowAccessAuthenticator.class);

    @Override
    /** 直接标记认证成功。 */
    public void authenticate(AuthenticationFlowContext context) {
        log.trace("Explicitly allowed access to the resource.");
        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    @Override
    /** 不依赖已认证用户。 */
    public boolean requiresUser() {
        return false;
    }

    @Override
    /** 始终返回 false（无需用户级配置）。 */
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}
