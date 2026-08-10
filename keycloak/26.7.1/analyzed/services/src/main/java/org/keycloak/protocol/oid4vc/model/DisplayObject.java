/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.StringUtil;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.jboss.logging.Logger;

/**
 * OID4VCI 凭证签发者元数据中的展示对象（DisplayObject）。
 * <p>描述凭证在钱包/UI 中的名称、语言、Logo、背景色等本地化展示信息。</p>
 * {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-issuer-metadata}
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class DisplayObject {

    /** 日志记录器。 */
    private static final Logger LOGGER = Logger.getLogger(DisplayObject.class);

    @JsonIgnore
    private static final String NAME_KEY = "name";
    @JsonIgnore
    private static final String LOCALE_KEY = "locale";
    @JsonIgnore
    private static final String LOGO_KEY = "logo";
    @JsonIgnore
    private static final String DESCRIPTION_KEY = "description";
    @JsonIgnore
    private static final String BG_COLOR_KEY = "background_color";
    @JsonIgnore
    private static final String TEXT_COLOR_KEY = "text_color";
    @JsonIgnore
    private static final String BG_IMAGE_KEY = "background_image";

    /** 凭证展示名称。 */
    @JsonProperty(DisplayObject.NAME_KEY)
    private String name;

    /** 语言区域（BCP 47）。 */
    @JsonProperty(DisplayObject.LOCALE_KEY)
    private String locale;

    /** Logo 图像对象。 */
    @JsonProperty(DisplayObject.LOGO_KEY)
    private LogoObject logo;

    /** 凭证描述文本。 */
    @JsonProperty(DisplayObject.DESCRIPTION_KEY)
    private String description;

    /** 背景色（十六进制）。 */
    @JsonProperty(DisplayObject.BG_COLOR_KEY)
    private String backgroundColor;

    /** 文字颜色（十六进制）。 */
    @JsonProperty(DisplayObject.TEXT_COLOR_KEY)
    private String textColor;

    /** 背景图像对象。 */
    @JsonProperty(DisplayObject.BG_IMAGE_KEY)
    private BackgroundImageObject backgroundImage;

    /**
     * 从凭证范围的 vc.display 属性解析展示对象列表。
     * <p>解析失败时记录警告并返回 null，不中断整体流程。</p>
     *
     * @param credentialScope 凭证范围模型
     * @return 展示对象列表，无配置或解析失败时为 null
     */
        String display = credentialScope.getVcDisplay();
        if (StringUtil.isBlank(display)) {
            return null;
        }
        TypeReference<List<DisplayObject>> typeReference = new TypeReference<>() {};
        try {
            return JsonSerialization.mapper.readValue(display, typeReference);
        } catch (JsonProcessingException e) {
            // 展示元数据无效时不应中断整体签发流程
            LOGGER.debug(e.getMessage(), e);
            LOGGER.warnf("Failed to parse display-metadata for credential '%s': %s", credentialScope.getName(), e.getMessage());
            return null;
        }
    }

    /** @return 展示名称 */
    public String getName() {
        return name;
    }

    /** @param name 展示名称 */
    public DisplayObject setName(String name) {
        this.name = name;
        return this;
    }

    /** @return 语言区域 */
    public String getLocale() {
        return locale;
    }

    /** @param locale 语言区域 */
    public DisplayObject setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    /** @return Logo 对象 */
    public LogoObject getLogo() {
        return logo;
    }

    /** @param logo Logo 对象 */
    public DisplayObject setLogo(LogoObject logo) {
        this.logo = logo;
        return this;
    }

    /** @return 描述文本 */
    public String getDescription() {
        return description;
    }

    /** @param description 描述文本 */
    public DisplayObject setDescription(String description) {
        this.description = description;
        return this;
    }

    /** @return 背景色 */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /** @param backgroundColor 背景色 */
    public DisplayObject setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    /** @return 文字颜色 */
    public String getTextColor() {
        return textColor;
    }

    /** @param textColor 文字颜色 */
    public DisplayObject setTextColor(String textColor) {
        this.textColor = textColor;
        return this;
    }

    /** @return 背景图像 */
    public BackgroundImageObject getBackgroundImage() {
        return backgroundImage;
    }

    /** @param backgroundImage 背景图像 */
    public DisplayObject setBackgroundImage(BackgroundImageObject backgroundImage) {
        this.backgroundImage = backgroundImage;
        return this;
    }

    /** @return JSON 字符串 */
    public String toJsonString(){
        try {
            return JsonSerialization.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从 JSON 字符串反序列化。
     *
     * @param jsonString JSON 文本
     * @return DisplayObject 实例
     */
        try {
            return JsonSerialization.readValue(jsonString, DisplayObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DisplayObject that)) return false;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getLocale() != null ? !getLocale().equals(that.getLocale()) : that.getLocale() != null) return false;
        if (getLogo() != null ? !getLogo().equals(that.getLogo()) : that.getLogo() != null) return false;
        if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
            return false;
        if (getBackgroundColor() != null ? !getBackgroundColor().equals(that.getBackgroundColor()) : that.getBackgroundColor() != null)
            return false;
        if (getBackgroundImage() != null ? !getBackgroundImage().equals(that.getBackgroundImage()) : that.getBackgroundImage() != null)
            return false;
        return getTextColor() != null ? getTextColor().equals(that.getTextColor()) : that.getTextColor() == null;
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getLocale() != null ? getLocale().hashCode() : 0);
        result = 31 * result + (getLogo() != null ? getLogo().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getBackgroundColor() != null ? getBackgroundColor().hashCode() : 0);
        result = 31 * result + (getBackgroundImage() != null ? getBackgroundImage().hashCode() : 0);
        result = 31 * result + (getTextColor() != null ? getTextColor().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        try {
            return JsonSerialization.mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * OID4VCI 规范中的 Logo 展示对象。
     * {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-issuer-metadata-p}
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LogoObject {
        /** Logo 图像 URI。 */
        @JsonProperty("uri")
        private String uri;

        /** 图像替代文本（无障碍）。 */
        @JsonProperty("alt_text")
        private String altText;

        /** @return Logo URI */
        /** @return 背景图 URI */
        public String getUri() {
            return uri;
        }

        /** @param uri Logo URI */
        public LogoObject setUri(String uri) {
            this.uri = uri;
            return this;
        }

        /** @return 替代文本 */
        public String getAltText() {
            return altText;
        }

        /** @param altText 替代文本 */
        public LogoObject setAltText(String altText) {
            this.altText = altText;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LogoObject that)) return false;
            return Objects.equals(uri, that.uri) && Objects.equals(altText, that.altText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri, altText);
        }
    }

    /**
     * OID4VCI 规范中的背景图像对象。
     * {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-issuer-metadata-p}
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BackgroundImageObject {
        @JsonProperty("uri")
        private String uri;

        public String getUri() {
            return uri;
        }

        /** @param uri 背景图 URI */
        public BackgroundImageObject setUri(String uri) {
            this.uri = uri;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BackgroundImageObject that)) return false;
            return Objects.equals(uri, that.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri);
        }
    }
}
