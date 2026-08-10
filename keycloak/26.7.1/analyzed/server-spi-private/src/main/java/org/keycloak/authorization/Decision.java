/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authorization;

import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.Evaluation;

/**
 * 授权决策回调接口：在策略评估过程中接收许可/拒绝等中间结果。
 * <p>由 {@link org.keycloak.authorization.policy.evaluation.PolicyEvaluator} 驱动，泛型 {@code D} 通常为 {@link Evaluation}。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface Decision<D extends Evaluation> {

    /** 策略效果：允许或拒绝。 */
    enum Effect {
        PERMIT,
        DENY
    }

    /** 单次策略评估产生决策时回调。 */
    void onDecision(D evaluation);

    /** 评估出错时的默认处理（抛出运行时异常）。 */
    default void onError(Throwable cause) {
        throw new RuntimeException("Not implemented.", cause);
    }

    /** 全部评估完成时回调。 */
    default void onComplete() {
    }

    /** 针对单个 {@link ResourcePermission} 的评估完成时回调。 */
    default void onComplete(ResourcePermission permission) {
    }

    /**
     * 检查给定 {@code scope} 是否已在本次决策中被任一策略处理过。
     *
     * Checks if the given {@code scope} is associated with any policy processed in this decision.
     *
     * @param scope the scope name
     * @return {@code true} if the scope is associated with a policy. Otherwise, {@code false}.
     */
    default boolean isEvaluated(String scope) {
        return false;
    }
}
