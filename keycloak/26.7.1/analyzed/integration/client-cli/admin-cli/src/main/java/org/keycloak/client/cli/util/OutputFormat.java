/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.client.cli.util;

/**
 * CLI 命令结果输出格式枚举。
 * <p>
 * 由 {@code --format} 选项选择，对应 JSON 缩进或 CSV 扁平化输出。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public enum OutputFormat {
    /** JSON 格式（Jackson 缩进输出）。 */
    JSON,
    /** CSV 格式（逗号分隔扁平行）。 */
    CSV
}
