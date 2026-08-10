package org.keycloak.protocol.oidc.mappers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperContainerModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.ProtocolMapperConfigException;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;

import org.jboss.logging.Logger;

/**
 * SHA-256 成对 subject 映射器：使用加盐 SHA-256 哈希计算 {@code sub} 声明。
 * <p>符合 OpenID Connect 成对标识符规范，按 sector 隔离用户标识。</p>
 */
public class SHA256PairwiseSubMapper extends AbstractPairwiseSubMapper {
    /** Provider ID 前缀（完整 ID 为 sha256-pairwise-sub-mapper） */
    public static final String PROVIDER_ID = "sha256";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final Logger logger = Logger.getLogger(SHA256PairwiseSubMapper.class);
    private final Charset charset;

    /** 默认使用 UTF-8 字符集 */
    public SHA256PairwiseSubMapper() {
        charset = StandardCharsets.UTF_8;
    }

    /**
     * 工厂方法：创建 SHA-256 成对 subject 映射器表示。
     * @param sectorIdentifierUri sector identifier URI
     * @param salt 算法盐值（null 时自动生成）
     */
        Map<String, String> config;
        ProtocolMapperRepresentation pairwise = new ProtocolMapperRepresentation();
        pairwise.setName("pairwise subject identifier");
        pairwise.setProtocolMapper(new SHA256PairwiseSubMapper().getId());
        pairwise.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        config = new HashMap<>();
        config.put(PairwiseSubMapperHelper.SECTOR_IDENTIFIER_URI, sectorIdentifierUri);
        if (salt == null) {
            salt = KeycloakModelUtils.generateId();
        }
        config.put(PairwiseSubMapperHelper.PAIRWISE_SUB_ALGORITHM_SALT, salt);
        pairwise.setConfig(config);
        return pairwise;
    }

    /** {@inheritDoc} 盐值为空时自动生成随机盐 */
    @Override
    public void validateAdditionalConfig(KeycloakSession session, RealmModel realm, ProtocolMapperContainerModel mapperContainer, ProtocolMapperModel mapperModel) throws ProtocolMapperConfigException {
        // 需要时生成随机盐值
        String salt = PairwiseSubMapperHelper.getSalt(mapperModel);
        if (salt == null || salt.trim().isEmpty()) {
            salt = generateSalt();
            PairwiseSubMapperHelper.setSalt(mapperModel, salt);
        }
    }

    /** {@inheritDoc} 使用加盐 SHA-256 计算成对 sub 并写入声明 */
    @Override
    public String getHelpText() {
        return "Calculates a pairwise subject identifier using a salted sha-256 hash and adds it to the 'sub' claim. See OpenID Connect specification for more info about pairwise subject identifiers.";
    }

    /** {@inheritDoc} 追加盐值配置项 */
    @Override
    public List<ProviderConfigProperty> getAdditionalConfigProperties() {
        List<ProviderConfigProperty> configProperties = new LinkedList<>();
        configProperties.add(PairwiseSubMapperHelper.createSaltConfig());
        return configProperties;
    }

    /** {@inheritDoc} 基于 sector、本地 sub 与配置盐值生成成对标识符 */
    @Override
    public String generateSub(ProtocolMapperModel mappingModel, String sectorIdentifier, String localSub) {
        String saltStr = PairwiseSubMapperHelper.getSalt(mappingModel);
        if (saltStr == null) {
            throw new IllegalStateException("Salt not available on mappingModel. Please update protocol mapper");
        }

        Charset charset = StandardCharsets.UTF_8;
        byte[] salt = saltStr.getBytes(charset);
        String pairwiseSub = generateSub(sectorIdentifier, localSub, salt);
        logger.tracef("local sub = '%s', pairwise sub = '%s'", localSub, pairwiseSub);
        return pairwiseSub;
    }

    /** 对 sector + localSub 做 SHA-256 摘要后转为 UUID 字符串 */
    private String generateSub(String sectorIdentifier, String localSub, byte[] salt) {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        sha256.update(sectorIdentifier.getBytes(charset));
        sha256.update(localSub.getBytes(charset));
        byte[] hash = sha256.digest(salt);
        return UUID.nameUUIDFromBytes(hash).toString();
    }

    /** 生成随机盐值字符串 */
    private static String generateSalt() {
        return KeycloakModelUtils.generateId();
    }

    /** {@inheritDoc} 控制台显示名：Pairwise subject identifier */
    @Override
    public String getDisplayType() {
        return "Pairwise subject identifier";
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getIdPrefix() {
        return PROVIDER_ID;
    }
}
