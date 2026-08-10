package org.keycloak.testframework.ui.page;

import java.util.List;

import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.ui.annotations.InjectPage;
import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

/**
 * 为 {@link InjectPage} 注解提供 {@link AbstractPage} 实例的供应器。
 * <p>
 * 通过 {@link ManagedWebDriver#page()} 按请求类型创建页面对象，并声明对 WebDriver 的依赖。
 */
public class PageSupplier  implements Supplier<AbstractPage, InjectPage> {

    /** {@inheritDoc} 声明对注解中 {@code webDriverRef} 所指定 {@link ManagedWebDriver} 的依赖。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<AbstractPage, InjectPage> instanceContext) {
        return DependenciesBuilder.create(ManagedWebDriver.class, instanceContext.getAnnotation().webDriverRef()).build();
    }

    /** {@inheritDoc} 根据请求类型实例化对应页面对象。 */
    @Override
    public AbstractPage getValue(InstanceContext<AbstractPage, InjectPage> instanceContext) {
        ManagedWebDriver webDriver = instanceContext.getDependency(ManagedWebDriver.class, instanceContext.getAnnotation().webDriverRef());
        return webDriver.page().createPage(instanceContext.getRequestedValueType());
    }

    /** {@inheritDoc} 仅当 {@link InjectPage#ref()} 相同时视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<AbstractPage, InjectPage> a, RequestedInstance<AbstractPage, InjectPage> b) {
        return a.getAnnotation().ref().equals(b.getAnnotation().ref());
    }

}
