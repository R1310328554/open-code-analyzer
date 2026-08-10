package org.keycloak.dom.saml.v2.mdui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.keycloak.dom.saml.v2.metadata.LocalizedNameType;
import org.keycloak.dom.saml.v2.metadata.LocalizedURIType;

/**
 * *
 * <p>
 * Java class for UIInfoType complex type.
 * SAML MDUI 界面信息：聚合显示名、描述、关键词、徽标及信息/隐私声明 URL。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 *   &lt;element name="UIInfo" type="mdui:UIInfoType"/>
 *   &lt;complexType name="UIInfoType">
 *       &lt;choice minOccurs="0" maxOccurs="unbounded">
 *           &lt;element ref="mdui:DisplayName"/>
 *           &lt;element ref="mdui:Description"/>
 *           &lt;element ref="mdui:Keywords"/>
 *           &lt;element ref="mdui:Logo"/>
 *           &lt;element ref="mdui:InformationURL"/>
 *           &lt;element ref="mdui:PrivacyStatementURL"/>
 *           &lt;any namespace="##other" processContents="lax"/>
 *       &lt;/choice>
 * &lt;/complexType>
 *
 * </pre>
 */

public class UIInfoType implements Serializable {

    protected List<LocalizedNameType> displayName = new ArrayList<>();
    protected List<LocalizedNameType> description = new ArrayList<>();
    protected List<KeywordsType> keywords = new ArrayList<>();
    protected List<LocalizedURIType> informationURL = new ArrayList<>();
    protected List<LocalizedURIType> privacyStatementURL = new ArrayList<>();
    protected List<LogoType> logo = new ArrayList<>();

    /** 添加本地化显示名称。 */
    public void addDisplayName(LocalizedNameType displayName) {
        this.displayName.add(displayName);
    }

    /** 添加本地化描述。 */
    public void addDescription(LocalizedNameType description) {
        this.description.add(description);
    }

    /** 添加本地化关键词组。 */
    public void addKeywords(KeywordsType keywords) {
        this.keywords.add(keywords);
    }

    /** 添加本地化信息页 URL。 */
    public void addInformationURL(LocalizedURIType informationURL) {
        this.informationURL.add(informationURL);
    }

    /** 添加本地化隐私声明 URL。 */
    public void addPrivacyStatementURL(LocalizedURIType privacyStatementURL) {
        this.privacyStatementURL.add(privacyStatementURL);
    }

    /** 添加徽标。 */
    public void addLogo(LogoType logo) {
        this.logo.add(logo);
    }

    /** 获取显示名称列表。 */
    public List<LocalizedNameType> getDisplayName() {
        return displayName;
    }

    /** 获取描述列表。 */
    public List<LocalizedNameType> getDescription() {
        return description;
    }

    /** 获取关键词列表。 */
    public List<KeywordsType> getKeywords() {
        return keywords;
    }

    /** 获取信息页 URL 列表。 */
    public List<LocalizedURIType> getInformationURL() {
        return informationURL;
    }

    /** 获取隐私声明 URL 列表。 */
    public List<LocalizedURIType> getPrivacyStatementURL() {
        return privacyStatementURL;
    }

    /** 获取徽标列表。 */
    public List<LogoType> getLogo() {
        return logo;
    }

}