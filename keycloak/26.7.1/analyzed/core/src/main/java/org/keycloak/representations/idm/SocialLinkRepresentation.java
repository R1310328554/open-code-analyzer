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

package org.keycloak.representations.idm;

/**
 * 社交登录关联（Social Link）的 REST 表示，描述用户与外部社交身份提供者账户的绑定关系。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SocialLinkRepresentation {

    /** 社交身份提供者标识（如 google、facebook）。 */
    protected String socialProvider;
    /** 社交账户在提供者侧的用户 ID。 */
    protected String socialUserId;
    /** 社交账户用户名。 */
    protected String socialUsername;

    /** @return 社交提供者标识 */
    public String getSocialProvider() {
        return socialProvider;
    }

    /** @param socialProvider 社交提供者标识 */
    public void setSocialProvider(String socialProvider) {
        this.socialProvider = socialProvider;
    }

    /** @return 社交用户 ID */
    public String getSocialUserId() {
        return socialUserId;
    }

    /** @param socialUserId 社交用户 ID */
    public void setSocialUserId(String socialUserId) {
        this.socialUserId = socialUserId;
    }

    /** @return 社交用户名 */
    public String getSocialUsername() {
        return socialUsername;
    }

    /** @param socialUsername 社交用户名 */
    public void setSocialUsername(String socialUsername) {
        this.socialUsername = socialUsername;
    }
}
