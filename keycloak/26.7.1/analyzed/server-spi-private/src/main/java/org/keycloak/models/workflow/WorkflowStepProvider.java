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

package org.keycloak.models.workflow;

import org.keycloak.provider.Provider;

/**
 * 执行工作流步骤的提供者接口。
 * </p>
 * 实现类表示工作流中可执行的单个步骤，可执行特定操作并在需要时提供自定义通知消息。
 * </p>
 * Implementations of this interface represent individual steps that can be executed
 * as part of a workflow. Each step can perform specific actions and optionally provide
 * custom notification messages when users need to be informed about the step's execution.
 */
public interface WorkflowStepProvider extends Provider {

    /** 在给定执行上下文中运行此工作流步骤。 */
    /**
     * Runs this workflow step.
     *
     * @param context the workflow execution context
     */
    void run(WorkflowExecutionContext context);

    /**
     * 返回通知用户时邮件主题文本；null 表示使用默认主题。
     *
     * @return the notification subject, or {@code null} if the default subject should be used
     */
    default String getNotificationSubject() {
        return null;
    }

    /**
     * 返回通知用户时邮件正文文本；null 表示使用默认正文。
     *
     * @return the notification body, or {@code null} if the default subject should be used
     */
    default String getNotificationMessage() {
        return null;
    }
}
