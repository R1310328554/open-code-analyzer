/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.endpoints.request;

/**
 * 授权请求 URI 类型：区分 JWT 请求对象与 PAR（推送授权请求）。
 */
public enum RequestUriType {

    /** 通过 request 或 request_uri 传递的 JWT 请求对象 */
    REQUEST_OBJECT,
    /** 推送授权请求（Pushed Authorization Request） */
    PAR

}
