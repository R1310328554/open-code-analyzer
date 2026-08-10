package org.keycloak.testframework.ui.webdriver;

import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * WebDriver 启动选项工厂，集中配置各浏览器的通用与特定能力项。
 * <p>
 * 由 {@link DriverUtils} 在创建驱动实例时调用，测试代码通常不直接使用。
 * </p>
 */
class DriverOptions {

    /**
     * 构建 Chrome 浏览器选项。
     *
     * @param headless 为 {@code true} 时启用无头模式及相关启动参数
     * @return 配置完成的 {@link ChromeOptions}
     */
    static ChromeOptions createChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        setCommonCapabilities(options);

        if (headless) {
            options.addArguments(
                    "--headless=new",
                    "--disable-gpu",
                    "--window-size=1920,1200",
                    "--ignore-certificate-errors",
                    "--disable-dev-shm-usage",
                    "--remote-allow-origins=*",
                    "--no-sandbox"
            );
        }

        return options;
    }

    /**
     * 构建 Firefox 浏览器选项。
     *
     * @param headless 为 {@code true} 时启用无头模式
     * @return 配置完成的 {@link FirefoxOptions}
     */
    static FirefoxOptions createFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        setCommonCapabilities(options);

        if (headless) {
            options.addArguments("-headless");
        }

        // 禁用扩展与浏览器自动更新，避免 CI 环境弹窗干扰
        options.addPreference("extensions.update.enabled", "false");
        options.addPreference("app.update.enabled", "false");
        options.addPreference("app.update.auto", "false");

        return options;
    }

    /**
     * 构建 HtmlUnit 浏览器能力配置。
     *
     * @return 适用于 {@link HtmlUnitDriver} 的 {@link DesiredCapabilities}
     */
    static DesiredCapabilities createHtmlUnitOptions() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        setCommonCapabilities(capabilities);

        capabilities.setBrowserName("htmlunit");
        capabilities.setCapability(HtmlUnitDriver.DOWNLOAD_IMAGES_CAPABILITY, false);
        capabilities.setCapability(HtmlUnitDriver.JAVASCRIPT_ENABLED, true);

        return capabilities;
    }

    /** 设置各浏览器共用的页面加载策略与隐式等待超时。 */
    private static void setCommonCapabilities(MutableCapabilities capabilities) {
        capabilities.setCapability(CapabilityType.PAGE_LOAD_STRATEGY, PageLoadStrategy.NORMAL.toString());
        capabilities.setCapability("timeouts", Map.of("implicit", Duration.ofSeconds(5).toMillis()));
    }

}
