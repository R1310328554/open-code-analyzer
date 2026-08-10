package org.keycloak.scim.resource.user;

import java.util.Optional;

import org.keycloak.utils.StringUtil;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SCIM 用户姓名组件（RFC 7643 第 4.1.1 节）。
 * <p>包含格式化全名、名、姓及尊称前缀/后缀等字段。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Name {

    /** 格式化后的完整姓名。 */
    @JsonProperty("formatted")
    private String formatted;

    /** 姓（姓氏）。 */
    @JsonProperty("familyName")
    private String familyName;

    /** 名（名字）。 */
    @JsonProperty("givenName")
    private String givenName;

    /** 中间名。 */
    @JsonProperty("middleName")
    private String middleName;

    /** 尊称前缀（如 Dr.、Mr.）。 */
    @JsonProperty("honorificPrefix")
    private String honorificPrefix;

    /** 尊称后缀（如 Jr.、III）。 */
    @JsonProperty("honorificSuffix")
    private String honorificSuffix;

    /**
     * 获取格式化姓名；若未显式设置则从各组成部分拼接。
     *
     * @return 格式化姓名，全空白时返回 {@code null}
     */
    public String getFormatted() {
        if (formatted == null) {
            formatted = Optional.ofNullable(honorificPrefix).orElse("") +
                    " " +
                    Optional.ofNullable(givenName).orElse("") +
                    " " +
                    Optional.ofNullable(middleName).orElse("") +
                    " " +
                    Optional.ofNullable(familyName).orElse("") +
                    " " +
                    Optional.ofNullable(honorificSuffix).orElse("");
        }

        return StringUtil.isBlank(formatted.trim()) ? null : formatted;
    }

    public void setFormatted(String formatted) {
        this.formatted = formatted;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getHonorificPrefix() {
        return honorificPrefix;
    }

    public void setHonorificPrefix(String honorificPrefix) {
        this.honorificPrefix = honorificPrefix;
    }

    public String getHonorificSuffix() {
        return honorificSuffix;
    }

    public void setHonorificSuffix(String honorificSuffix) {
        this.honorificSuffix = honorificSuffix;
    }
}
