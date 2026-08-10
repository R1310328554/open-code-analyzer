package org.keycloak.common.crypto;

import java.io.IOException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

/**
 * ECDSA 签名格式转换与 EC 密钥推导 SPI。
 *
 * <p>在 JWS/JWT 使用的 concatenated R||S 与 ASN.1 DER 编码之间互转，并支持由私钥推导公钥。</p>
 */
public interface ECDSACryptoProvider {
    
    /** 将 concatenated R||S 签名转换为 ASN.1 DER 编码。 */
    public byte[] concatenatedRSToASN1DER(final byte[] signature, int signLength) throws IOException;

    /** 将 ASN.1 DER 编码签名转换为 concatenated R||S 格式。 */
    public byte[] asn1derToConcatenatedRS(final byte[] derEncodedSignatureValue, int signLength) throws IOException;

    /** 由 EC 私钥推导对应公钥。 */
    public ECPublicKey getPublicFromPrivate(ECPrivateKey ecPrivateKey);
}
