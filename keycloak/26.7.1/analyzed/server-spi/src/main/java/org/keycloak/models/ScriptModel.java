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
package org.keycloak.models;

/**
 * 脚本模型：带元数据的脚本表示（如 JavaScript 认证脚本）。
 * A representation of a Script with some additional meta-data.
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public interface ScriptModel {

    /**
     * JavaScript 的 MIME 类型常量
     * MIME-Type for JavaScript
     */
    String TEXT_JAVASCRIPT = "text/javascript";

    /**
     * 返回脚本唯一 ID；临时脚本为 {@literal null}。
     * Returns the unique id of the script. {@literal null} for ad-hoc created scripts.
     */
    String getId();

    /**
     * 返回定义此脚本的 Realm ID。
     * Returns the realm id in which the script was defined.
     */
    String getRealmId();

    /**
     * 返回脚本名称。
     * Returns the name of the script.
     */
    String getName();

    /**
     * 返回脚本代码的 MIME 类型（如 JavaScript 为 {@code text/javascript}）。
     * Returns the MIME-type if the script code, e.g. for Java Script the MIME-type, {@code text/javascript} is used.
     */
    String getMimeType();

    /**
     * 返回脚本源代码。
     * Returns the actual source code of the script.
     */
    String getCode();

    /**
     * 返回脚本描述。
     * Returns the description of the script.
     */
    String getDescription();
}
