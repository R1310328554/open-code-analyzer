package org.keycloak.credential;

import java.util.List;

/**
 * 凭据展示元数据：供管理控制台或账户页展示提示、警告与图标等信息。
 * <p>由 {@link CredentialProvider#getCredentialMetadata} 构建，不持久化到数据库。</p>
 */
public class CredentialMetadata {
    LocalizedMessage infoMessage;
    List<LocalizedMessage> infoProperties;
    LocalizedMessage warningMessageTitle;
    LocalizedMessage warningMessageDescription;
    CredentialModel credentialModel;
    private String iconLight;
    private String iconDark;

    /** @return 关联的 {@link CredentialModel} */
    public CredentialModel getCredentialModel() {
        return credentialModel;
    }

    /** 设置关联的 {@link CredentialModel}。 */
    public void setCredentialModel(CredentialModel credentialModel) {
        this.credentialModel = credentialModel;
    }

    /** @return 信息提示消息 */
    public LocalizedMessage getInfoMessage() {
        return infoMessage;
    }

    /** @return 信息属性列表 */
    public List<LocalizedMessage> getInfoProperties() {
        return infoProperties;
    }

    /** @return 警告标题 */
    public LocalizedMessage getWarningMessageTitle() {
        return warningMessageTitle;
    }

    /** @return 警告描述 */
    public LocalizedMessage getWarningMessageDescription() {
        return warningMessageDescription;
    }

    /** 设置警告标题（国际化消息键与参数）。 */
    public void setWarningMessageTitle(String key, String... parameters) {
        LocalizedMessage message = new LocalizedMessage(key, parameters);
        this.warningMessageTitle = message;
    }

    /** 设置警告描述（国际化消息键与参数）。 */
    public void setWarningMessageDescription(String key, String... parameters) {
        LocalizedMessage message = new LocalizedMessage(key, parameters);
        this.warningMessageDescription = message;
    }

    /** 设置信息提示（国际化消息键与参数）。 */
    public void setInfoMessage(String key, String... parameters) {
        LocalizedMessage message = new LocalizedMessage(key, parameters);
        this.infoMessage = message;
    }

    /** 设置信息属性列表。 */
    public void setInfoProperties(List<LocalizedMessage> infoProperties) {
        this.infoProperties = infoProperties;
    }

    /** @return 浅色主题图标 CSS 类或 URL */
    public String getIconLight() {
        return iconLight;
    }

    /** 设置浅色主题图标。 */
    public void setIconLight(String iconLight) {
        this.iconLight = iconLight;
    }

    /** @return 深色主题图标 CSS 类或 URL */
    public String getIconDark() {
        return iconDark;
    }

    /** 设置深色主题图标。 */
    public void setIconDark(String iconDark) {
        this.iconDark = iconDark;
    }

    /** 国际化消息：消息键与占位参数。 */
    public static class LocalizedMessage {
        private final String key;
        private final Object[] parameters;

        /** @param key 消息键 @param parameters 占位参数 */
        public LocalizedMessage(String key, Object[] parameters) {
            this.key = key;
            this.parameters = parameters;
        }

        /** @return 消息键 */
        public String getKey() {
            return key;
        }

        /** @return 占位参数数组 */
        public Object[] getParameters() {
            return parameters;
        }
    }

}
