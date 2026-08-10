package org.keycloak.protocol.oidc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.representations.idm.ClientRepresentation;

/**
 * 客户端配置属性读写抽象包装器。
 * <p>统一 {@link ClientModel} 与 {@link ClientRepresentation} 上的字符串及多值属性访问。</p>
 */
public abstract class AbstractClientConfigWrapper {
    /** 持久化客户端模型（可为 null，此时使用 representation）。 */
    protected final ClientModel clientModel;
    /** 客户端表示 DTO（可为 null）。 */
    protected final ClientRepresentation clientRep;

    /**
     * @param clientModel 客户端模型
     * @param clientRep 客户端表示
     */
    protected AbstractClientConfigWrapper(ClientModel clientModel,
                                          ClientRepresentation clientRep) {
        this.clientModel = clientModel;
        this.clientRep = clientRep;
    }

    /** @param attrKey 属性键 @return 属性值或 null */
    protected String getAttribute(String attrKey) {
        if (clientModel != null) {
            return clientModel.getAttribute(attrKey);
        } else {
            return clientRep.getAttributes() == null ? null : clientRep.getAttributes().get(attrKey);
        }
    }

    /** @param attrKey 属性键 @param defaultValue 默认值 @return 属性值或默认值 */
    protected String getAttribute(String attrKey, String defaultValue) {
        String value = getAttribute(attrKey);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /** @return 全部属性映射 */
    protected Object getAttributes() {
        if (clientModel != null) return clientModel.getAttributes();
        else
            return clientRep.getAttributes();
    }

    /** @param attrKey 属性键 @param attrValue 属性值，null 表示移除 */
    protected void setAttribute(String attrKey, String attrValue) {
        if (clientModel != null) {
            if (attrValue != null) {
                clientModel.setAttribute(attrKey, attrValue);
            } else {
                clientModel.removeAttribute(attrKey);
            }
        } else {
            if (attrValue != null) {
                if (clientRep.getAttributes() == null) {
                    clientRep.setAttributes(new HashMap<>());
                }
                clientRep.getAttributes().put(attrKey, attrValue);
            } else {
                if (clientRep.getAttributes() != null) {
                    clientRep.getAttributes().put(attrKey, null);
                }
            }
        }
    }

    /** @param attrKey 多值属性键 @return 分隔解析后的值列表 */
    public List<String> getAttributeMultivalued(String attrKey) {
        String attrValue = getAttribute(attrKey);
        if (attrValue == null) return Collections.emptyList();
        return Arrays.asList(Constants.CFG_DELIMITER_PATTERN.split(attrValue));
    }

    /** @param attrKey 多值属性键 @param attrValues 值列表，空则移除属性 */
    public void setAttributeMultivalued(String attrKey, List<String> attrValues) {
        if (attrValues == null || attrValues.size() == 0) {
            // 空列表时移除属性
            setAttribute(attrKey, null);
        } else {
            String attrValueFull = String.join(Constants.CFG_DELIMITER, attrValues);
            setAttribute(attrKey, attrValueFull);
        }
    }
}
