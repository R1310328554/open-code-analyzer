package org.keycloak.testframework.ui.webdriver;


import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.ui.annotations.InjectWebDriver;

import org.openqa.selenium.WebDriver;

/**
 * {@link InjectWebDriver} 注解的 WebDriver 供应器抽象基类。
 * <p>
 * 子类实现 {@link #getWebDriver()} 提供具体浏览器实例，并包装为 {@link ManagedWebDriver}。
 */
public abstract class AbstractWebDriverSupplier implements Supplier<ManagedWebDriver, InjectWebDriver> {

    /** {@inheritDoc} 创建并返回包装后的 {@link ManagedWebDriver}。 */
    @Override
    public ManagedWebDriver getValue(InstanceContext<ManagedWebDriver, InjectWebDriver> instanceContext) {
        return new ManagedWebDriver(getWebDriver());
    }

    /** {@inheritDoc} 仅当 {@link InjectWebDriver#ref()} 相同时视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<ManagedWebDriver, InjectWebDriver> a, RequestedInstance<ManagedWebDriver, InjectWebDriver> b) {
        return a.getAnnotation().ref().equals(b.getAnnotation().ref());
    }

    /** {@inheritDoc} 退出底层 WebDriver 会话。 */
    @Override
    public void close(InstanceContext<ManagedWebDriver, InjectWebDriver> instanceContext) {
        instanceContext.getValue().driver().quit();
    }

    /**
     * 由子类提供具体 {@link WebDriver} 实例。
     *
     * @return 底层 Selenium WebDriver
     */
    public abstract WebDriver getWebDriver();

}
