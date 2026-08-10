package org.keycloak.protocol.oid4vc.issuance.requiredactions;

import java.io.IOException;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oid4vc.utils.OID4VCUtil;
import org.keycloak.utils.QRCodeUtils;

import com.google.zxing.WriterException;

/**
 * 凭证发放（Credential Offer）展示用数据 Bean。
 * <p>封装 OID4VCI 发放 URI 及对应 QR 码字符串，供登录表单模板渲染。</p>
 */
public class CredentialOfferBean {

    private final String uri;
    private final String qrCode;

    /**
     * 根据 nonce 构建发放 URI 并生成 QR 码。
     *
     * @param session Keycloak 会话
     * @param nonce   凭证发放 nonce
     * @throws WriterException QR 编码失败
     * @throws IOException     URI 构建失败
     */
        this.uri = OID4VCUtil.getOfferAsUri(session, nonce);
        this.qrCode = QRCodeUtils.encodeAsQRString(this.uri, 246, 246);
    }

    /** @return 凭证发放 URI（含 nonce） */
    public String getUri() {
        return uri;
    }

    /** @return Base64 编码的 QR 码图像数据 */
    public String getQrCode() {
        return qrCode;
    }
}
