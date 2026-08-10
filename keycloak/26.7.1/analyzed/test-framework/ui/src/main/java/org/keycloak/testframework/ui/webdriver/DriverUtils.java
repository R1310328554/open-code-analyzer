package org.keycloak.testframework.ui.webdriver;

import java.io.File;

import org.keycloak.testframework.config.Config;

import org.htmlunit.WebClientOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverService;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * WebDriver 实例创建工具类，负责解析驱动路径并组装浏览器实例。
 * <p>
 * 由各类 {@link AbstractWebDriverSupplier} 子类调用；测试代码通过供应器间接使用。
 * </p>
 */
class DriverUtils {

    /**
     * 创建 Chrome WebDriver。
     *
     * @param headless 为 {@code true} 时使用无头模式
     * @return 配置完成的 {@link ChromeDriver}
     */
    static ChromeDriver createChromeDriver(boolean headless) {
        ChromeDriverService.Builder builder = new ChromeDriverService.Builder();

        File driver = resolveDriver("CHROMEWEBDRIVER", "chromedriver");
        if (driver != null) {
            builder.usingDriverExecutable(driver);
        }

        ChromeDriverService driverService = builder.build();
        return new ChromeDriver(driverService, DriverOptions.createChromeOptions(headless));
    }

    /**
     * 创建 Firefox WebDriver。
     *
     * @param headless 为 {@code true} 时使用无头模式
     * @return 配置完成的 {@link FirefoxDriver}
     */
    static FirefoxDriver  createFirefoxDriver(boolean headless) {
        GeckoDriverService.Builder builder = new GeckoDriverService.Builder();

        File driver = resolveDriver("GECKOWEBDRIVER", "geckodriver");
        if (driver != null) {
            builder.usingDriverExecutable(driver);
        }

        FirefoxDriverService driverService = builder.build();
        return new FirefoxDriver(driverService, DriverOptions.createFirefoxOptions(headless));
    }

    /**
     * 创建 HtmlUnit WebDriver，并调整 WebClient 以适配 UI 测试场景。
     *
     * @return 配置完成的 {@link HtmlUnitDriver}
     */
    static HtmlUnitDriver createHtmlUnitDriver() {
        HtmlUnitDriver driver = new HtmlUnitDriver(DriverOptions.createHtmlUnitOptions());
        WebClientOptions options = driver.getWebClient().getOptions();
        options.setCssEnabled(false);

        // HtmlUnit 与 JavaScript 兼容性有限；关闭脚本错误与失败状态码异常，避免误报
        // HtmlUnit validates all scripts and then fails. It turned off the validation.
        options.setThrowExceptionOnScriptError(false);
        options.setThrowExceptionOnFailingStatusCode(false);

        return driver;
    }

    /**
     * 解析浏览器驱动可执行文件路径。
     * <p>
     * 优先使用测试配置中的 {@code driver} 项，其次读取环境变量（可指向目录或驱动文件本身）。
     * </p>
     *
     * @param envName 环境变量名（如 {@code CHROMEWEBDRIVER}）
     * @param driverName 驱动文件名（不含扩展名）
     * @return 驱动文件，未配置时返回 {@code null}
     */
    private static File resolveDriver(String envName, String driverName) {
        File driver = Config.getValueTypeConfig(ManagedWebDriver.class, "driver", null, File.class);
        if (driver != null) {
            return driver;
        }

        // 环境变量可指向驱动所在目录，或直接指向驱动可执行文件
        // Environment variable can point to directory where the driver is located, or the driver directly
        String driverPathFromEnv = System.getenv(envName);
        if (driverPathFromEnv != null) {
            driver = new File(driverPathFromEnv);
            if (driver.isFile()) {
                return driver;
            } else {
                return new File(driver, driverName  + (isWindows() ? ".exe" : ""));
            }
        }

        return null;
    }

    /** 判断当前操作系统是否为 Windows。 */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

}
