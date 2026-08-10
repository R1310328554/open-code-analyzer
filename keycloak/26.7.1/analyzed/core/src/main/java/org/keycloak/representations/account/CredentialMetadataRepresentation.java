package org.keycloak.representations.account;

import java.util.List;

import org.keycloak.representations.idm.CredentialRepresentation;

/**
 * 账户控制台中凭据条目的元数据包装，含本地化提示/警告消息及明暗主题图标。
 */
public class CredentialMetadataRepresentation {

    /** 信息性提示消息（本地化）。 */
    LocalizedMessage infoMessage;
    /** 信息性属性列表（每项为本地化消息）。 */
    List<LocalizedMessage> infoProperties;
    /** 警告标题（本地化）。 */
    LocalizedMessage warningMessageTitle;
    /** 警告详细描述（本地化）。 */
    LocalizedMessage warningMessageDescription;

    /** 底层凭据数据。 */
    private CredentialRepresentation credential;
    /** 浅色主题图标 URI 或 CSS 类名。 */
    private String iconLight;
    /** 深色主题图标 URI 或 CSS 类名。 */
    private String iconDark;


    /** @return 凭据详情 */
    public CredentialRepresentation getCredential() {
        return credential;
    }

    /** @param credential 凭据详情 */
    public void setCredential(CredentialRepresentation credential) {
        this.credential = credential;
    }

    /** @return 信息提示消息 */
    public LocalizedMessage getInfoMessage() {
        return infoMessage;
    }

    /** @param infoMessage 信息提示消息 */
    public void setInfoMessage(LocalizedMessage infoMessage) {
        this.infoMessage = infoMessage;
    }

    /** @return 信息属性列表 */
    public List<LocalizedMessage> getInfoProperties() {
        return infoProperties;
    }

    /** @param infoProperties 信息属性列表 */
    public void setInfoProperties(List<LocalizedMessage> infoProperties) {
        this.infoProperties = infoProperties;
    }

    /** @return 警告标题 */
    public LocalizedMessage getWarningMessageTitle() {
        return warningMessageTitle;
    }

    /** @param warningMessageTitle 警告标题 */
    public void setWarningMessageTitle(LocalizedMessage warningMessageTitle) {
        this.warningMessageTitle = warningMessageTitle;
    }

    /** @return 警告描述 */
    public LocalizedMessage getWarningMessageDescription() {
        return warningMessageDescription;
    }

    /** @param warningMessageDescription 警告描述 */
    public void setWarningMessageDescription(LocalizedMessage warningMessageDescription) {
        this.warningMessageDescription = warningMessageDescription;
    }

    /** @return 浅色主题图标 */
    public String getIconLight() {
        return iconLight;
    }

    /** @param iconLight 浅色主题图标 */
    public void setIconLight(String iconLight) {
        this.iconLight = iconLight;
    }

    /** @return 深色主题图标 */
    public String getIconDark() {
        return iconDark;
    }

    /** @param iconDark 深色主题图标 */
    public void setIconDark(String iconDark) {
        this.iconDark = iconDark;
    }
}
