package org.keycloak.theme.freemarker;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.KeycloakSanitizerMethod;
import org.keycloak.theme.Theme;

import freemarker.cache.URLTemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 默认 FreeMarker 模板提供者。
 * <p>加载主题模板、注入 {@code kcSanitize} 方法并渲染为 HTML 字符串；支持可选的模板缓存以提升性能。</p>
 */
public class DefaultFreeMarkerProvider implements FreeMarkerProvider {
    private final ConcurrentHashMap<String, Template> cache;
    private final KeycloakSanitizerMethod kcSanitizeMethod;

    /** 注入模板缓存与 HTML  sanitizer 方法（cache 可为 null 禁用缓存）。 */
    public DefaultFreeMarkerProvider(ConcurrentHashMap<String, Template> cache, KeycloakSanitizerMethod kcSanitizeMethod) {
        this.cache = cache;
        this.kcSanitizeMethod = kcSanitizeMethod;
    }

    /** 渲染指定主题模板；data 为 Map 时自动注入 kcSanitize。 */
    @Override
    public String processTemplate(Object data, String templateName, Theme theme) throws FreeMarkerException {
        if (data instanceof Map) {
            ((Map)data).put("kcSanitize", kcSanitizeMethod);
        }

        try {
            Template template;
            if (cache != null) {
                String key = theme.getType().toString().toLowerCase() + "/" + theme.getName() + "/" + templateName;
                template = cache.get(key);
                if (template == null) {
                    template = getTemplate(templateName, theme);
                    if (cache.putIfAbsent(key, template) != null) {
                        template = cache.get(key);
                    }
                }
            } else {
                template = getTemplate(templateName, theme);
            }

            Writer out = new StringWriter();
            template.process(data, out);
            return out.toString();
        } catch (Exception e) {
            throw new FreeMarkerException("Failed to process template " + templateName, e);
        }
    }

    /** 创建 FreeMarker 配置并加载主题模板；.ftl 文件启用 HTML 输出格式防 XSS。 */
    private Template getTemplate(String templateName, Theme theme) throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);

        // 假定 *.ftl 为 HTML，启用 HTMLOutputFormat 以 sanitize 输出、防止 XSS
        if (templateName.toLowerCase().endsWith(".ftl")) {
            cfg.setOutputFormat(HTMLOutputFormat.INSTANCE);
        }

        cfg.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
        cfg.setTemplateLoader(new ThemeTemplateLoader(theme));
        return cfg.getTemplate(templateName, "UTF-8");
    }

    /** 从 {@link Theme} 加载模板 URL 的 FreeMarker TemplateLoader。 */
    static class ThemeTemplateLoader extends URLTemplateLoader {

        private Theme theme;

        public ThemeTemplateLoader(Theme theme) {
            this.theme = theme;
        }

        /** 委托 theme.getTemplate 获取模板资源 URL。 */
        @Override
        protected URL getURL(String name) {
            try {
                return theme.getTemplate(name);
            } catch (IOException e) {
                return null;
            }
        }

    }

    /** 无资源需释放。 */
    @Override
    public void close() {

    }
}
