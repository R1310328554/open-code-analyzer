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

import java.util.ArrayList;
import java.util.List;

/**
 * 领域导入状态的流式构建器，维护 Done/Started/HasErrors 三类条件及其消息。
 */
public class KeycloakRealmImportStatusBuilder {
    private final KeycloakRealmImportStatusCondition readyCondition;
    private final KeycloakRealmImportStatusCondition startedCondition;
    private final KeycloakRealmImportStatusCondition hasErrorsCondition;

    private final List<String> notReadyMessages = new ArrayList<>();
    private final List<String> startedMessages = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();

    /** 初始化三种条件，默认均为 False。 */
    public KeycloakRealmImportStatusBuilder() {
        readyCondition = new KeycloakRealmImportStatusCondition();
        readyCondition.setType(KeycloakRealmImportStatusCondition.DONE);
        readyCondition.setStatus(false);

        startedCondition = new KeycloakRealmImportStatusCondition();
        startedCondition.setType(KeycloakRealmImportStatusCondition.STARTED);
        startedCondition.setStatus(false);

        hasErrorsCondition = new KeycloakRealmImportStatusCondition();
        hasErrorsCondition.setType(KeycloakRealmImportStatusCondition.HAS_ERRORS);
        hasErrorsCondition.setStatus(false);
    }

    /** 记录导入已启动的消息，并清除 Done/HasErrors 标志。 */
    public KeycloakRealmImportStatusBuilder addStartedMessage(String message) {
        startedCondition.setStatus(true);
        readyCondition.setStatus(false);
        hasErrorsCondition.setStatus(false);
        startedMessages.add(message);
        return this;
    }

    /** 标记导入成功完成。 */
    public KeycloakRealmImportStatusBuilder addDone() {
        startedCondition.setStatus(false);
        readyCondition.setStatus(true);
        hasErrorsCondition.setStatus(false);
        return this;
    }

    /** 记录尚未就绪的原因（如等待 Job 调度）。 */
    public KeycloakRealmImportStatusBuilder addNotReadyMessage(String message) {
        startedCondition.setStatus(false);
        readyCondition.setStatus(false);
        hasErrorsCondition.setStatus(false);
        notReadyMessages.add(message);
        return this;
    }

    /** 记录导入过程中的错误消息。 */
    public KeycloakRealmImportStatusBuilder addErrorMessage(String message) {
        startedCondition.setStatus(false);
        readyCondition.setStatus(false);
        hasErrorsCondition.setStatus(true);
        errorMessages.add(message);
        return this;
    }

    /** 将累积的消息写入各条件并组装最终 {@link KeycloakRealmImportStatus}。 */
    public KeycloakRealmImportStatus build() {
        readyCondition.setMessage(String.join("\n", notReadyMessages));
        startedCondition.setMessage(String.join("\n", startedMessages));
        hasErrorsCondition.setMessage(String.join("\n", errorMessages));

        KeycloakRealmImportStatus status = new KeycloakRealmImportStatus();
        status.setConditions(List.of(readyCondition, startedCondition, hasErrorsCondition));
        return status;
    }
}
