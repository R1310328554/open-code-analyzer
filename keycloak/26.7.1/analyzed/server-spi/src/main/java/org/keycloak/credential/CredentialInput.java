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
package org.keycloak.credential;

/**
 * 凭据输入 SPI：封装认证流程中提交的凭据标识、类型与挑战响应。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface CredentialInput {
    /** @return 凭据 ID（可为 null） */
    String getCredentialId();
    /** @return 凭据类型（如 password、otp） */
    String getType();
    /** @return 用户提交的凭据值或挑战响应 */
    String getChallengeResponse();
}
