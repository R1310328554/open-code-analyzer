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

package org.keycloak.operator.crds.v2beta1.realmimport;

import org.keycloak.operator.crds.v2beta1.StatusCondition;

/**
 * 领域导入 CR 的状态条件，继承 {@link StatusCondition} 并定义标准条件类型常量。
 */
public class KeycloakRealmImportStatusCondition extends StatusCondition {
    /** 导入已成功完成。 */
    public static final String DONE = "Done";
    /** 导入 Job 已启动。 */
    public static final String STARTED = "Started";
    /** 导入过程中发生错误。 */
    public static final String HAS_ERRORS = "HasErrors";
}
