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
package org.keycloak.protocol.oid4vc.model;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 授权详情（authorization details）中使用的声明描述对象。
 * <p>定义钱包请求纳入凭证的声明路径及是否必填，用于 OID4VCI 授权流程中的 claims 约束。</p>
 *
 * @author <a href="mailto:Forkim.Akwichek@adorsys.com">Forkim Akwichek</a>
 */
public class ClaimsDescription {

    /** JSON 字段名：声明路径 {@code path}。 */
    public static final String PATH = "path";
    /** JSON 字段名：是否必填 {@code mandatory}。 */
    public static final String MANDATORY = "mandatory";

    /** 声明路径（可为字符串或索引混合路径）。 */
    @JsonProperty(PATH)
    private List<Object> path;

    /** 钱包是否必须请求该声明。 */
    @JsonProperty(MANDATORY)
    private Boolean mandatory;

    /** 无参构造，供 Jackson 反序列化使用。 */
    public ClaimsDescription() {
    }

    /**
     * @param path 声明路径
     * @param mandatory 是否必填
     */
    public ClaimsDescription(List<Object> path, Boolean mandatory) {
        this.path = path;
        this.mandatory = mandatory;
    }

    /** @return 声明路径 */
    public List<Object> getPath() {
        return path;
    }

    /** @param path 声明路径 */
    public void setPath(List<Object> path) {
        this.path = path;
    }

    /** @return 是否必填（原始布尔值，可能为 {@code null}） */
    public Boolean getMandatory() {
        return mandatory;
    }

    /** @param mandatory 是否必填 */
    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    /** @return 是否必填，未设置时默认为 {@code false} */
    public boolean isMandatory() {
        return mandatory != null ? mandatory : false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimsDescription that = (ClaimsDescription) o;
        return Objects.equals(path, that.path) && Objects.equals(mandatory, that.mandatory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, mandatory);
    }
}
