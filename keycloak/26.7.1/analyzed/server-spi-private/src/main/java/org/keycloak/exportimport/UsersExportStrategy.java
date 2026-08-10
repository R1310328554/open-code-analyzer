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
 * 导出用户数据时的文件布局策略。
 * <p>控制用户 JSON 是否与领域文件合并、分文件或按数量拆分。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum UsersExportStrategy {
    /** 完全不导出用户。 */
    SKIP,            // Exporting of users will be skipped completely
    /** 用户与领域写入同一文件（如 {@code foo-realm.json}）。 */
    REALM_FILE,      // All users will be exported to same file with realm (So file like "foo-realm.json" with both realm data and users)
    /** 用户写入独立文件（如 {@code foo-users.json}），与领域文件分离。 */
    SAME_FILE,       // All users will be exported to same file but different than realm (So file like "foo-realm.json" with realm data and "foo-users.json" with users)
    /** 按每文件最大用户数拆分为多个用户文件。 */
    DIFFERENT_FILES  // Users will be exported into more different files according to maximum number of users per file
}
