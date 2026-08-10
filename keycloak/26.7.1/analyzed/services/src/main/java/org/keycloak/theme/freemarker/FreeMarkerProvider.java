package org.keycloak.theme.freemarker;

import org.keycloak.provider.Provider;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.Theme;

/**
 * FreeMarker 模板渲染提供者接口。
 * <p>将数据模型与主题模板结合，输出最终 HTML 或文本字符串。</p>
 */
public interface FreeMarkerProvider extends Provider {

    /** 使用 data 模型渲染 theme 下的 templateName 模板。 */
    public String processTemplate(Object data, String templateName, Theme theme) throws FreeMarkerException;

}
