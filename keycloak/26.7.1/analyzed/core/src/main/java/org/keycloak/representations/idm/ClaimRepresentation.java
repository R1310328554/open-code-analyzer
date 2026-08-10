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
 * OpenID Connect 标准声明（claim）的启用开关集合，用于控制 token/userinfo 中包含哪些 profile 字段。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClaimRepresentation {
    /** 是否包含 name 声明。 */
    protected boolean name;
    /** 是否包含 preferred_username 声明。 */
    protected boolean username;
    /** 是否包含 profile 声明。 */
    protected boolean profile;
    /** 是否包含 picture 声明。 */
    protected boolean picture;
    /** 是否包含 website 声明。 */
    protected boolean website;
    /** 是否包含 email 声明。 */
    protected boolean email;
    /** 是否包含 gender 声明。 */
    protected boolean gender;
    /** 是否包含 locale 声明。 */
    protected boolean locale;
    /** 是否包含 address 声明。 */
    protected boolean address;
    /** 是否包含 phone_number 声明。 */
    protected boolean phone;

    /** @return 是否启用 name */
    public boolean getName() {
        return name;
    }

    /** @param name 是否启用 name */
    public void setName(boolean name) {
        this.name = name;
    }

    /** @return 是否启用 username */
    public boolean getUsername() {
        return username;
    }

    /** @param username 是否启用 username */
    public void setUsername(boolean username) {
        this.username = username;
    }

    /** @return 是否启用 profile */
    public boolean getProfile() {
        return profile;
    }

    /** @param profile 是否启用 profile */
    public void setProfile(boolean profile) {
        this.profile = profile;
    }

    /** @return 是否启用 picture */
    public boolean getPicture() {
        return picture;
    }

    /** @param picture 是否启用 picture */
    public void setPicture(boolean picture) {
        this.picture = picture;
    }

    /** @return 是否启用 website */
    public boolean getWebsite() {
        return website;
    }

    /** @param website 是否启用 website */
    public void setWebsite(boolean website) {
        this.website = website;
    }

    /** @return 是否启用 email */
    public boolean getEmail() {
        return email;
    }

    /** @param email 是否启用 email */
    public void setEmail(boolean email) {
        this.email = email;
    }

    /** @return 是否启用 gender */
    public boolean getGender() {
        return gender;
    }

    /** @param gender 是否启用 gender */
    public void setGender(boolean gender) {
        this.gender = gender;
    }

    /** @return 是否启用 locale */
    public boolean getLocale() {
        return locale;
    }

    /** @param locale 是否启用 locale */
    public void setLocale(boolean locale) {
        this.locale = locale;
    }

    /** @return 是否启用 address */
    public boolean getAddress() {
        return address;
    }

    /** @param address 是否启用 address */
    public void setAddress(boolean address) {
        this.address = address;
    }

    /** @return 是否启用 phone */
    public boolean getPhone() {
        return phone;
    }

    /** @param phone 是否启用 phone */
    public void setPhone(boolean phone) {
        this.phone = phone;
    }

    /** 按各声明开关逐字段比较相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClaimRepresentation that = (ClaimRepresentation) o;

        if (address != that.address) return false;
        if (email != that.email) return false;
        if (gender != that.gender) return false;
        if (locale != that.locale) return false;
        if (name != that.name) return false;
        if (phone != that.phone) return false;
        if (picture != that.picture) return false;
        if (profile != that.profile) return false;
        if (username != that.username) return false;
        if (website != that.website) return false;

        return true;
    }

    /** 基于各声明开关计算哈希值。 */
    @Override
    public int hashCode() {
        int result = (name ? 1 : 0);
        result = 31 * result + (username ? 1 : 0);
        result = 31 * result + (profile ? 1 : 0);
        result = 31 * result + (picture ? 1 : 0);
        result = 31 * result + (website ? 1 : 0);
        result = 31 * result + (email ? 1 : 0);
        result = 31 * result + (gender ? 1 : 0);
        result = 31 * result + (locale ? 1 : 0);
        result = 31 * result + (address ? 1 : 0);
        result = 31 * result + (phone ? 1 : 0);
        return result;
    }
}
