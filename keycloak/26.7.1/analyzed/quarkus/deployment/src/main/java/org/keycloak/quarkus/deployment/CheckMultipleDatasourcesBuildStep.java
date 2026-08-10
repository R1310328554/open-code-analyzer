/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.deployment;


/**
 * 多数据源 XA 事务配置校验完成后的屏障构建项。
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
import io.quarkus.builder.item.EmptyBuildItem;

/**
 * 多数据源 XA 事务配置校验完成后的屏障构建项。
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class CheckMultipleDatasourcesBuildStep extends EmptyBuildItem {}
