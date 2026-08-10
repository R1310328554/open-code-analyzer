package org.keycloak.testsuite.arquillian.undertow;

import org.keycloak.testsuite.arquillian.undertow.lb.SimpleUndertowLoadBalancerContainer;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * Keycloak Undertow Arquillian 扩展：注册 {@link KeycloakOnUndertow} 与
 * {@link SimpleUndertowLoadBalancerContainer} 两个可部署容器实现。
 *
 * @author tkyjovsk
 */
public class KeycloakOnUndertowArquillianExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, KeycloakOnUndertow.class);
        builder.service(DeployableContainer.class, SimpleUndertowLoadBalancerContainer.class);
    }

}
