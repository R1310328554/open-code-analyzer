/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.policy.provider.permission;

import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.Evaluation;

import org.jboss.logging.Logger;

/**
 * UMA 权限策略提供者：资源属主访问自身资源时直接授予，否则评估关联策略。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class UMAPolicyProvider extends AbstractPermissionProvider {

    private static final Logger logger = Logger.getLogger(UMAPolicyProvider.class);

    /**
     * 若请求主体为资源所有者则跳过 UMA 评估并授予；否则委托父类处理关联策略。
     *
     * @param evaluation 当前授权评估上下文
     */
    @Override
    public void evaluate(Evaluation evaluation) {
        logger.debugf("UMA policy %s evaluating using parent class", evaluation.getPolicy().getName());
        ResourcePermission permission = evaluation.getPermission();
        Resource resource = permission.getResource();

        if (resource != null) {
            Identity identity = evaluation.getContext().getIdentity();

            // 资源属主访问自己的资源时无需走 UMA 权限评估
            if (resource.getOwner().equals(identity.getId())) {
                logger.debugv("UMA resource is owned by the current user, bypassing evaluation");
                evaluation.grant();
                return;
            }
        }

        super.evaluate(evaluation);
    }
}
