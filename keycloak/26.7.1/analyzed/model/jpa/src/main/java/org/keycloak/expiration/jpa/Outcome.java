/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.expiration.jpa;

/**
 * 过期清理任务一次执行的结果枚举。
 * <p>
 * 由 {@link ExpirationListener} 用于向监听器与指标系统报告清理结果。
 * </p>
 */
public enum Outcome {

    /**
     * 所有批次均成功完成。
     */
    OK,

    /**
     * 至少有一个批次成功，随后发生失败；部分过期条目已删除，但清理未完全结束。
     */
    PARTIAL,

    /**
     * 首个批次即失败，未删除任何过期条目。
     */
    FAILED
}
