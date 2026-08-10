package org.keycloak.ssf.transmitter;

import org.keycloak.models.KeycloakSession;
import org.keycloak.ssf.transmitter.delivery.SecurityEventTokenDispatcher;
import org.keycloak.ssf.transmitter.delivery.poll.PollDeliveryService;
import org.keycloak.ssf.transmitter.delivery.push.PushDeliveryService;
import org.keycloak.ssf.transmitter.event.SecurityEventTokenEncoder;
import org.keycloak.ssf.transmitter.event.SecurityEventTokenMapper;
import org.keycloak.ssf.transmitter.metadata.TransmitterMetadataService;
import org.keycloak.ssf.transmitter.stream.StreamVerificationService;
import org.keycloak.ssf.transmitter.stream.storage.client.ClientStreamStore;
import org.keycloak.ssf.transmitter.subject.SsfSubjectInclusionResolver;
import org.keycloak.ssf.transmitter.subject.SubjectManagementService;
import org.keycloak.ssf.transmitter.support.SsfPushUrlValidator;

/**
 * 构建 {@link SsfTransmitterProvider} 各 per-session 服务的扩展接缝。
 * 默认实现位于 {@link DefaultSsfTransmitterProviderFactory}；部署只需替换单个服务
 *（例如自定义 {@link SecurityEventTokenEncoder}）时，覆盖对应方法即可，无需子类化整个 provider。
 *
 * <h3>两类工厂方法</h3>
 *
 * <ul>
 *     <li><b>叶子服务</b> — {@code createMapper}、{@code createEncoder}、
 *         {@code createPushDelivery}、{@code createStreamStore}、
 *         {@code createMetadataService}、{@code createSubjectManagement} —
 *         仅接受 {@code (session, context)}，不依赖其他懒加载缓存服务。</li>
 *     <li><b>组合服务</b> — {@code createDispatcher}、{@code createVerification}、
 *         {@code createPollDelivery} — 接受 {@link SsfTransmitterProvider}，
 *         通过 provider 访问器复用已缓存的 mapper/encoder/push 等依赖，避免重复实例化。
 *         provider 在接口上暴露 {@link SsfTransmitterProvider#session() session()} 与
 *         {@link SsfTransmitterProvider#context() context()}，组合构建器无需向下转型。</li>
 * </ul>
 */
public interface SsfTransmitterServiceBuilder {

    SecurityEventTokenEncoder createEncoder(KeycloakSession session, SsfTransmitterContext ctx);

    SecurityEventTokenMapper createMapper(KeycloakSession session, SsfTransmitterContext ctx);

    PushDeliveryService createPushDelivery(KeycloakSession session, SsfTransmitterContext ctx);

    ClientStreamStore createStreamStore(KeycloakSession session, SsfTransmitterContext ctx);

    TransmitterMetadataService createMetadataService(KeycloakSession session, SsfTransmitterContext ctx);

    SubjectManagementService createSubjectManagement(KeycloakSession session, SsfTransmitterContext ctx);

    SsfSubjectInclusionResolver createSubjectInclusionResolver(KeycloakSession session, SsfTransmitterContext ctx);

    SecurityEventTokenDispatcher createDispatcher(SsfTransmitterProvider provider);

    StreamVerificationService createVerification(SsfTransmitterProvider provider);

    PollDeliveryService createPollDelivery(SsfTransmitterProvider provider);

    default SsfPushUrlValidator createPushUrlValidator(SsfTransmitterConfig config) {
        return new SsfPushUrlValidator(config.isAllowInsecurePushTargets());
    }
}
