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
package org.keycloak.authorization.policy.provider;

import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.provider.Provider;

/**
 * 策略提供者 SPI：各策略类型（角色、规则、聚合等）实现此接口以执行 {@link Evaluation}。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface PolicyProvider extends Provider {

    /** 对给定 {@link Evaluation} 上下文执行策略逻辑并调用 grant/deny。 */
    void evaluate(Evaluation evaluation);
}
