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

package org.keycloak.exportimport;

/**
 * 导入时遇到已存在实体（如用户）的处理策略。
 * <p>通过 {@code --import-realm} 或 SPI 配置指定。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum Strategy {

    /** 跳过已存在的用户条目，保留数据库中的现有数据。 */
    IGNORE_EXISTING,         // Ignore existing user entries
    /** 覆盖已存在的用户条目，以导入文件中的数据为准。 */
    OVERWRITE_EXISTING       // Overwrite existing user entries
}
