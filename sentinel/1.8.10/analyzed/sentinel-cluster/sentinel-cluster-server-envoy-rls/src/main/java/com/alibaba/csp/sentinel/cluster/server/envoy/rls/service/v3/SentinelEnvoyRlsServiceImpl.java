package com.alibaba.csp.sentinel.cluster.server.envoy.rls.service.v3;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.envoy.rls.flow.SimpleClusterFlowChecker;
import com.alibaba.csp.sentinel.cluster.server.envoy.rls.log.RlsAccessLogger;
import com.alibaba.csp.sentinel.cluster.server.envoy.rls.rule.EnvoySentinelRuleConverter;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.function.Tuple2;
import com.google.protobuf.TextFormat;
import io.envoyproxy.envoy.extensions.common.ratelimit.v3.RateLimitDescriptor;
import io.envoyproxy.envoy.service.ratelimit.v3.RateLimitRequest;
import io.envoyproxy.envoy.service.ratelimit.v3.RateLimitResponse;
import io.envoyproxy.envoy.service.ratelimit.v3.RateLimitResponse.Code;
import io.envoyproxy.envoy.service.ratelimit.v3.RateLimitResponse.RateLimit;
import io.envoyproxy.envoy.service.ratelimit.v3.RateLimitResponse.DescriptorStatus;
import io.envoyproxy.envoy.service.ratelimit.v3.RateLimitServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.csp.sentinel.cluster.server.envoy.rls.rule.EnvoySentinelRuleConverter.SEPARATOR;

/**
 * Sentinel Envoy RLS v3 gRPC 限流服务实现。
 * <p>接收 {@link io.envoyproxy.envoy.service.ratelimit.v3.RateLimitRequest}，
 * 按 descriptor 向集群令牌服务端申请配额并返回 {@link io.envoyproxy.envoy.service.ratelimit.v3.RateLimitResponse}。</p>
 *
 * @author Winjay chan
 * @date 2021/8/4
 */
public class SentinelEnvoyRlsServiceImpl extends RateLimitServiceGrpc.RateLimitServiceImplBase {
    @Override
    public void shouldRateLimit(RateLimitRequest request, StreamObserver<RateLimitResponse> responseObserver) {
        int acquireCount = request.getHitsAddend();
        if (acquireCount < 0) {
            responseObserver.onError(new IllegalArgumentException(
                    "acquireCount should be positive, but actual: " + acquireCount));
            return;
        }
        if (acquireCount == 0) {
            // 未指定 hitsAddend 时默认按 1 次请求计数。
            acquireCount = 1;
        }

        String domain = request.getDomain();
        boolean blocked = false;
        List<DescriptorStatus> statusList = new ArrayList<>(request.getDescriptorsCount());
        for (RateLimitDescriptor descriptor : request.getDescriptorsList()) {
            Tuple2<FlowRule, TokenResult> t = checkToken(domain, descriptor, acquireCount);
            TokenResult r = t.r2;

            printAccessLogIfNecessary(domain, descriptor, r);

            if (r.getStatus() == TokenResultStatus.NO_RULE_EXISTS) {
                // 若 descriptor 无对应规则，则直接放行。
                r.setStatus(TokenResultStatus.OK);
            }

            if (!blocked && r.getStatus() != TokenResultStatus.OK) {
                blocked = true;
            }

            Code statusCode = r.getStatus() == TokenResultStatus.OK ? Code.OK : Code.OVER_LIMIT;
            DescriptorStatus.Builder descriptorStatusBuilder = DescriptorStatus.newBuilder()
                    .setCode(statusCode);
            if (t.r1 != null) {
                descriptorStatusBuilder
                        .setCurrentLimit(RateLimit.newBuilder().setUnit(RateLimit.Unit.SECOND)
                                .setRequestsPerUnit((int)t.r1.getCount())
                                .build())
                        .setLimitRemaining(r.getRemaining());
            }
            statusList.add(descriptorStatusBuilder.build());
        }

        Code overallStatus = blocked ? Code.OVER_LIMIT :Code.OK;
        RateLimitResponse response = RateLimitResponse.newBuilder()
                .setOverallCode(overallStatus)
                .addAllStatuses(statusList)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void printAccessLogIfNecessary(String domain, RateLimitDescriptor descriptor, TokenResult result) {
        if (!RlsAccessLogger.isEnabled()) {
            return;
        }
        String message = new StringBuilder("[RlsAccessLog] domain=").append(domain)
                .append(", descriptor=").append(TextFormat.shortDebugString(descriptor))
                .append(", checkStatus=").append(result.getStatus())
                .append(", remaining=").append(result.getRemaining())
                .toString();
        RlsAccessLogger.log(message);
    }

    protected Tuple2<FlowRule, TokenResult> checkToken(String domain, RateLimitDescriptor descriptor, int acquireCount) {
        long ruleId = EnvoySentinelRuleConverter.generateFlowId(generateKey(domain, descriptor));

        FlowRule rule = ClusterFlowRuleManager.getFlowRuleById(ruleId);
        if (rule == null) {
            // 目标规则不存在时直接放行。
            return Tuple2.of(null, new TokenResult(TokenResultStatus.NO_RULE_EXISTS));
        }
        // 规则存在时应已通过合法性校验。
        return Tuple2.of(rule, SimpleClusterFlowChecker.acquireClusterToken(rule, acquireCount));
    }

    private String generateKey(String domain, RateLimitDescriptor descriptor) {
        StringBuilder sb = new StringBuilder(domain);
        for (RateLimitDescriptor.Entry resource : descriptor.getEntriesList()) {
            sb.append(SEPARATOR).append(resource.getKey()).append(SEPARATOR).append(resource.getValue());
        }
        return sb.toString();
    }
}
