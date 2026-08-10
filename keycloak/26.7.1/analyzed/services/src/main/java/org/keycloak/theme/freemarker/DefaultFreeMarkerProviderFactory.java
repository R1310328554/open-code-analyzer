package org.keycloak.theme.freemarker;

import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.theme.KeycloakSanitizerMethod;

import freemarker.template.Template;

/**
 * 默认 FreeMarker 提供者工厂。
 * <p>单例创建 {@link DefaultFreeMarkerProvider}，按 {@code theme.cacheTemplates} 配置决定是否启用模板缓存。</p>
 */
public class DefaultFreeMarkerProviderFactory implements FreeMarkerProviderFactory {

    private volatile DefaultFreeMarkerProvider provider;
    private ConcurrentHashMap<String, Template> cache;
    private KeycloakSanitizerMethod kcSanitizeMethod;

    /** 懒加载单例 Provider（双重检查锁定）。 */
    @Override
    public DefaultFreeMarkerProvider create(KeycloakSession session) {
        if (provider == null) {
            synchronized (this) {
                if (provider == null) {
                    if (Config.scope("theme").getBoolean("cacheTemplates", true)) {
                        cache = new ConcurrentHashMap<>();
                    }
                    kcSanitizeMethod = new KeycloakSanitizerMethod();
                    provider = new DefaultFreeMarkerProvider(cache, kcSanitizeMethod);
                }
            }
        }
        return provider;
    }

    /** 工厂初始化（当前无额外配置）。 */
    @Override
    public void init(Config.Scope config) {
    }

    /** 会话工厂就绪回调（当前无操作）。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** 关闭工厂（当前无资源释放）。 */
    @Override
    public void close() {
    }

    /** 返回工厂 id {@code default}。 */
    @Override
    public String getId() {
        return "default";
    }

}
