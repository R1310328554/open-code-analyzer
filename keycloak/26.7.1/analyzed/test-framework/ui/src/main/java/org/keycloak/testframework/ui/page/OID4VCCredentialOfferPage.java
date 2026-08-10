package org.keycloak.testframework.ui.page;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * OID4VC 凭证要约页面对象，对应 {@code login-oid4vc-credential-offer} 模板。
 * <p>
 * 展示可下载的凭证要约 URI，并支持继续或取消认证操作。
 */
public class OID4VCredentialOfferPage extends AbstractLoginPage {

    @FindBy(id = "credential-offer-uri-link")
    private WebElement credentialOfferUri;

    @FindBy(id = "continue-vc-offer")
    private WebElement continueButton;

    @FindBy(name = "cancel-aia")
    private WebElement cancelAIAButton;

    /**
     * 绑定托管 WebDriver 并初始化页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public OID4VCCredentialOfferPage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 读取页面上凭证要约链接的完整 href。
     * <p>
     * 通常为 {@code openid-credential-offer://?credential_offer_uri=...} 形式。
     *
     * @return 完整要约 URL
     */
    public String getCredentialOffer() {
        return credentialOfferUri.getDomAttribute("href");
    }

    /**
     * 从完整要约 URL 中解析并解码 {@code credential_offer_uri} 参数。
     *
     * @return 解码后的要约 URI；无法解析时返回 {@code null}
     */
    public String getCredentialOfferUri() {
        String fullOffer = getCredentialOffer();
        String[] splits = fullOffer.split("credential_offer_uri=");
        if (splits.length < 2) {
            return null;
        }
        String url = splits[1];
        return URLDecoder.decode(url, StandardCharsets.UTF_8);
    }


    /** 点击继续按钮以推进凭证要约流程。 */
    public void clickContinueButton() {
        continueButton.click();
    }


    /** 点击取消按钮中止认证操作。 */
    public void cancel() {
        cancelAIAButton.click();
    }

    /**
     * 判断取消按钮是否可见。
     *
     * @return 可见返回 {@code true}；元素不存在时返回 {@code false}
     */
    public boolean isCancelDisplayed() {
        try {
            return cancelAIAButton.isDisplayed();
        } catch (NoSuchElementException nsee) {
            return false;
        }
    }

    /** {@inheritDoc} 返回 {@code login-oid4vc-credential-offer}。 */
    @Override
    public String getExpectedPageId() {
        return "login-oid4vc-credential-offer";
    }
}
