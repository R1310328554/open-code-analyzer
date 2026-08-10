package org.keycloak.protocol.saml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.saml.util.ArtifactBindingUtils;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.utils.StringUtil;

import org.jboss.logging.Logger;

import static org.keycloak.protocol.saml.DefaultSamlArtifactResolverFactory.TYPE_CODE;
import static org.keycloak.protocol.saml.SamlConfigAttributes.SAML_ARTIFACT_BINDING_IDENTIFIER;

/**
 * SAML 2.0 Artifact-04 格式解析器默认实现。
 * <p>标准允许其他 artifact 类型，但 SAML 2.0 规范仅定义 TypeCode {@code 0x0004}；本类负责构建、解析 artifact 并从客户端会话取回 SAML 响应。</p>
 */
public class DefaultSamlArtifactResolver implements ArtifactResolver {


    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(SamlService.class);

    /** 从客户端会话备注读取 artifact 对应的 SAML 响应并移除备注 @return Base64/XML 响应字符串 */
    @Override
    public String resolveArtifact(AuthenticatedClientSessionModel clientSessionModel, String artifact) throws ArtifactResolverProcessingException {
        String artifactResponseString = clientSessionModel.getNote(GeneralConstants.SAML_ARTIFACT_KEY + "=" + artifact);
        clientSessionModel.removeNote(GeneralConstants.SAML_ARTIFACT_KEY + "=" + artifact);

        logger.tracef("Artifact response for artifact %s, is %s", artifact, artifactResponseString);

        if (StringUtil.isNullOrEmpty(artifactResponseString)) {
            throw new ArtifactResolverProcessingException("Artifact not present in ClientSession.");
        }

        return artifactResponseString;
    }

    /** 从 artifact SourceID 查找发行方 SAML 客户端 @return 匹配的客户端 @throws ArtifactResolverProcessingException 未找到时 */
    @Override
    public ClientModel selectSourceClient(KeycloakSession session, String artifact) throws ArtifactResolverProcessingException {
        byte[] source = extractSourceFromArtifact(artifact);
        String identifier = ArtifactBindingUtils.getArtifactBindingIdentifierString(source);

        return session.clients().searchClientsByAttributes(session.getContext().getRealm(),
                Collections.singletonMap(SAML_ARTIFACT_BINDING_IDENTIFIER, identifier), 0, 1)
                .findFirst().orElseThrow(() -> new ArtifactResolverProcessingException("No client matching the artifact source found"));
    }

    /** 创建 artifact 并将 SAML 响应存入客户端会话 @param entityId 实体 ID @return Base64 artifact */
    @Override
    public String buildArtifact(AuthenticatedClientSessionModel clientSessionModel, String entityId, String artifactResponse) throws ArtifactResolverProcessingException {
        String artifact = createArtifact(entityId);

        clientSessionModel.setNote(GeneralConstants.SAML_ARTIFACT_KEY + "=" + artifact, artifactResponse);

        return artifact;
    }

    /** 校验 artifact 长度为 44 字节且 TypeCode 为 0x0004 */
    private void assertSupportedArtifactFormat(String artifactString) throws ArtifactResolverProcessingException {
        byte[] artifact = Base64.getDecoder().decode(artifactString);

        if (artifact.length != 44) {
            throw new ArtifactResolverProcessingException("Artifact " + artifactString + " has a length of " + artifact.length + ". It should be 44");
        }
        if (artifact[0] != TYPE_CODE[0] || artifact[1] != TYPE_CODE[1]) {
            throw new ArtifactResolverProcessingException("Artifact " + artifactString + " does not start with 0x0004");
        }
    }

    /** 从 artifact 提取 20 字节 SourceID @return SourceID 字节数组 */
    private byte[] extractSourceFromArtifact(String artifactString) throws ArtifactResolverProcessingException {
        assertSupportedArtifactFormat(artifactString);

        byte[] artifact = Base64.getDecoder().decode(artifactString);

        byte[] source = new byte[20];
        System.arraycopy(artifact, 4, source, 0, source.length);

        return source;
    }

    /**
     * 创建 SAML Artifact（TypeCode 0x0004）。
     * <p>格式：B64(TypeCode + EndpointIndex + SourceID + MessageHandle)。</p>
     * @param entityId 编码进 SourceID 的实体 ID
     * @return Base64 编码的 artifact
     * @throws ArtifactResolverProcessingException 构建失败时
     */
    public String createArtifact(String entityId) throws ArtifactResolverProcessingException {
        try {
            SecureRandom handleGenerator = new SecureRandom();
            byte[] trimmedIndex = new byte[2];

            byte[] source = ArtifactBindingUtils.computeArtifactBindingIdentifier(entityId);

            byte[] assertionHandle = new byte[20];
            handleGenerator.nextBytes(assertionHandle);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(TYPE_CODE);
            bos.write(trimmedIndex);
            bos.write(source);
            bos.write(assertionHandle);

            byte[] artifact = bos.toByteArray();

            return Base64.getEncoder().encodeToString(artifact);
        } catch (IOException e) {
            throw new ArtifactResolverProcessingException(e);
        }

    }

    /** 关闭资源（无操作） */
    @Override
    public void close() {

    }

}
