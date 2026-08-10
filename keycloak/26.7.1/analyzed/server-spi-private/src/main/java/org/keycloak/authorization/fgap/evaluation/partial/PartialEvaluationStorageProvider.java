/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2025 Red Hat, Inc., and individual contributors
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

package org.keycloak.authorization.fgap.evaluation.partial;

import java.util.List;

import jakarta.persistence.criteria.Predicate;

/**
 * 部分评估存储扩展：领域资源存储 Provider 可据此向 JPA 查询注入自定义授权过滤谓词。
 * <p>在启用 {@link org.keycloak.common.Profile.Feature#ADMIN_FINE_GRAINED_AUTHZ} 时由 {@link PartialEvaluator} 回调。</p>
 *
 * If a realm has the {@link org.keycloak.common.Profile.Feature#ADMIN_FINE_GRAINED_AUTHZ} feature enabled,
 * realm resources storage providers can implement this interface if they want to apply custom predicates to queries
 * to filter their results based on the permissions available from the realm.
 */
public interface PartialEvaluationStorageProvider {

    /**
     * 构建允许访问资源的查询过滤谓词列表。
     *
     * A callback method that will be called when building queries for realm resources to grant access to resources. It returns a list of
     * {@link Predicate} instances representing the filters that should be applied to queries
     * when querying realm resources.
     *
     * @param evaluationContext the evaluation context.
     * @return the list of predicates
     */
    List<Predicate> getFilters(PartialEvaluationContext evaluationContext);

    /**
     * 构建拒绝访问资源的否定过滤谓词列表。
     *
     * A callback method that will be called when building queries for realm resources to deny access to resources. It returns a list of
     * {@link Predicate} instances representing the filters that should be applied to queries
     * when querying realm resources.
     *
     * @param context the evaluation context.
     * @return the list of predicates
     */
    List<Predicate> getNegateFilters(PartialEvaluationContext context);
}
