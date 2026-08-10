/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.adapters.saml.elytron;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.keycloak.adapters.saml.AdapterConstants;
import org.keycloak.adapters.saml.DefaultSamlDeployment;
import org.keycloak.adapters.saml.SamlConfigResolver;
import org.keycloak.adapters.saml.SamlDeployment;
import org.keycloak.adapters.saml.SamlDeploymentContext;
import org.keycloak.adapters.saml.config.parsers.DeploymentBuilder;
import org.keycloak.adapters.saml.config.parsers.ResourceLoader;
import org.keycloak.adapters.saml.elytron.infinispan.InfinispanSessionCacheIdMapperUpdater;
import org.keycloak.adapters.spi.InMemorySessionIdMapper;
import org.keycloak.adapters.spi.SessionIdMapper;
import org.keycloak.adapters.spi.SessionIdMapperUpdater;
import org.keycloak.saml.common.exceptions.ParsingException;

import org.jboss.logging.Logger;

/**
 * Keycloak SAML 适配器配置监听器。
 *
 * <p>作为 {@link ServletContextListener}，在 Web 应用启动时解析 Keycloak 适配器配置，
 * 并将 {@link SamlDeploymentContext}、{@link SessionIdMapper} 等对象写入
 * {@link ServletContext} 属性，供 {@link KeycloakHttpServerAuthenticationMechanism}
 * 在处理请求时获取配置。</p>
 *
 * <p>通常由 WildFly 子系统自动注册到部署中。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class KeycloakConfigurationServletListener implements ServletContextListener {

    /** 本类日志记录器。 */
    protected static Logger log = Logger.getLogger(KeycloakConfigurationServletListener.class);

    /** {@link SamlDeploymentContext} 在 ServletContext 中的属性名。 */
    public static final String ADAPTER_DEPLOYMENT_CONTEXT_ATTRIBUTE = SamlDeploymentContext.class.getName();
    /** Elytron 专用的部署上下文属性名。 */
    public static final String ADAPTER_DEPLOYMENT_CONTEXT_ATTRIBUTE_ELYTRON = SamlDeploymentContext.class.getName() + ".elytron";
    /** Elytron 专用的 SessionIdMapper 属性名。 */
    public static final String ADAPTER_SESSION_ID_MAPPER_ATTRIBUTE_ELYTRON = SessionIdMapper.class.getName() + ".elytron";
    /** Elytron 专用的 SessionIdMapperUpdater 属性名。 */
    public static final String ADAPTER_SESSION_ID_MAPPER_UPDATER_ATTRIBUTE_ELYTRON = SessionIdMapperUpdater.class.getName() + ".elytron";

    /** 内存中的会话 ID 映射器。 */
    private final SessionIdMapper idMapper = new InMemorySessionIdMapper();
    /** 会话 ID 映射更新器，默认直接写入。 */
    private SessionIdMapperUpdater idMapperUpdater = SessionIdMapperUpdater.DIRECT;
    /** 应用关闭时需要释放的资源。 */
    private Collection<AutoCloseable> toClose = new LinkedList<>();

    /**
     * Web 应用启动：解析 SAML 配置、注册会话监听器并写入 ServletContext 属性。
     *
     * @param sce Servlet 上下文事件
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        String configResolverClass = servletContext.getInitParameter("keycloak.config.resolver");
        SamlDeploymentContext deploymentContext = (SamlDeploymentContext) servletContext.getAttribute(SamlDeployment.class.getName());

        if (deploymentContext == null) {
            if (configResolverClass != null) {
                try {
                    SamlConfigResolver configResolver = (SamlConfigResolver) servletContext.getClassLoader().loadClass(configResolverClass).newInstance();
                    deploymentContext = new SamlDeploymentContext(configResolver);
                    log.infov("Using {0} to resolve Keycloak configuration on a per-request basis.", configResolverClass);
                } catch (Exception ex) {
                    log.errorv("The specified resolver {0} could NOT be loaded. Keycloak is unconfigured and will deny all requests. Reason: {1}", new Object[] { configResolverClass, ex.getMessage() });
                    deploymentContext = new SamlDeploymentContext(new DefaultSamlDeployment());
                }
            } else {
                InputStream is = getConfigInputStream(servletContext);
                final SamlDeployment deployment;
                if (is == null) {
                    log.warn("No adapter configuration.  Keycloak is unconfigured and will deny all requests.");
                    deployment = new DefaultSamlDeployment();
                } else {
                    try {
                        ResourceLoader loader = new ResourceLoader() {
                            @Override
                            public InputStream getResourceAsStream(String resource) {
                                return servletContext.getResourceAsStream(resource);
                            }
                        };
                        deployment = new DeploymentBuilder().build(is, loader);
                    } catch (ParsingException e) {
                        throw new RuntimeException(e);
                    }
                }
                deploymentContext = new SamlDeploymentContext(deployment);
                log.debug("Keycloak is using a per-deployment configuration.");
            }
        }

        addTokenStoreUpdaters(servletContext);

        servletContext.setAttribute(ADAPTER_DEPLOYMENT_CONTEXT_ATTRIBUTE, deploymentContext);
        servletContext.setAttribute(ADAPTER_DEPLOYMENT_CONTEXT_ATTRIBUTE_ELYTRON, deploymentContext);
        servletContext.setAttribute(ADAPTER_SESSION_ID_MAPPER_ATTRIBUTE_ELYTRON, idMapper);
        servletContext.setAttribute(ADAPTER_SESSION_ID_MAPPER_UPDATER_ATTRIBUTE_ELYTRON, idMapperUpdater);
    }

    /** Web 应用关闭：释放已注册的 AutoCloseable 资源。 */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        for (AutoCloseable c : toClose) {
            try {
                c.close();
            } catch (Exception e) {
                log.warnf(e, "Exception while destroying servlet context");
            }
        }
    }

    /** 从 ServletContext 初始化参数或 WEB-INF 文件获取 SAML 配置输入流。 */
    private static InputStream getConfigInputStream(ServletContext context) {
        InputStream is = getXMLFromServletContext(context);
        if (is == null) {
            String path = context.getInitParameter("keycloak.config.file");
            if (path == null) {
                log.debug("using /WEB-INF/keycloak-saml.xml");
                is = context.getResourceAsStream("/WEB-INF/keycloak-saml.xml");
            } else {
                try {
                    is = new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return is;
    }

    /** 从 ServletContext init-param 读取内嵌 XML 配置。 */
    private static InputStream getXMLFromServletContext(ServletContext servletContext) {
        String json = servletContext.getInitParameter(AdapterConstants.AUTH_DATA_PARAM_NAME);
        if (json == null) {
            return null;
        }
        return new ByteArrayInputStream(json.getBytes());
    }

    /**
     * 注册会话 ID 映射更新器：本地 HTTP 会话监听器及可选的集群扩展类。
     *
     * @param servletContext Servlet 上下文
     */
    public void addTokenStoreUpdaters(ServletContext servletContext) {
        SessionIdMapperUpdater updater = this.idMapperUpdater;

        servletContext.addListener(new IdMapperUpdaterSessionListener(idMapper));    // 处理本地 HTTP 会话变更

        try {
            String idMapperSessionUpdaterClasses = servletContext.getInitParameter("keycloak.sessionIdMapperUpdater.classes");
            if (idMapperSessionUpdaterClasses == null) {
                return;
            }

            updater = SessionIdMapperUpdater.DIRECT;

            for (String clazz : idMapperSessionUpdaterClasses.split("\\s*,\\s*")) {
                if (! clazz.isEmpty()) {
                    if (Objects.equals("org.keycloak.adapters.saml.wildfly.infinispan.InfinispanSessionCacheIdMapperUpdater", clazz)) {
                        clazz = InfinispanSessionCacheIdMapperUpdater.class.getName();  // 将 wildfly/undertow 实现替换为 elytron 版本
                    }
                    updater = invokeAddTokenStoreUpdaterMethod(clazz, servletContext, updater);
                    if (updater instanceof AutoCloseable) {
                        toClose.add((AutoCloseable) updater);
                    }
                }
            }
        } finally {
            setIdMapperUpdater(updater);
        }
    }

    /** 通过反射调用指定类的 {@code addTokenStoreUpdaters} 静态方法。 */
    private SessionIdMapperUpdater invokeAddTokenStoreUpdaterMethod(String idMapperSessionUpdaterClass, ServletContext servletContext,
      SessionIdMapperUpdater previousIdMapperUpdater) {
        try {
            Class<?> clazz = servletContext.getClassLoader().loadClass(idMapperSessionUpdaterClass);
            Method addTokenStoreUpdatersMethod = clazz.getMethod("addTokenStoreUpdaters", ServletContext.class, SessionIdMapper.class, SessionIdMapperUpdater.class);
            if (! Modifier.isStatic(addTokenStoreUpdatersMethod.getModifiers())
              || ! Modifier.isPublic(addTokenStoreUpdatersMethod.getModifiers())
              || ! SessionIdMapperUpdater.class.isAssignableFrom(addTokenStoreUpdatersMethod.getReturnType())) {
                log.errorv("addTokenStoreUpdaters method in class {0} has to be public static. Ignoring class.", idMapperSessionUpdaterClass);
                return previousIdMapperUpdater;
            }

            log.debugv("Initializing sessionIdMapperUpdater class {0}", idMapperSessionUpdaterClass);
            return (SessionIdMapperUpdater) addTokenStoreUpdatersMethod.invoke(null, servletContext, idMapper, previousIdMapperUpdater);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
            log.warnv(ex, "Cannot use sessionIdMapperUpdater class {0}", idMapperSessionUpdaterClass);
            return previousIdMapperUpdater;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            log.warnv(ex, "Cannot use {0}.addTokenStoreUpdaters(DeploymentInfo, SessionIdMapper) method", idMapperSessionUpdaterClass);
            return previousIdMapperUpdater;
        }
    }

    /** @return 当前会话 ID 映射更新器 */
    public SessionIdMapperUpdater getIdMapperUpdater() {
        return idMapperUpdater;
    }

    /** 设置会话 ID 映射更新器（供子类或测试使用）。 */
    protected void setIdMapperUpdater(SessionIdMapperUpdater idMapperUpdater) {
        this.idMapperUpdater = idMapperUpdater;
    }
}
