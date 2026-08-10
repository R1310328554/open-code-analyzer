package org.keycloak.testframework.server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.testcontainers.containers.output.BaseConsumer;
import org.testcontainers.containers.output.OutputFrame;

/**
 * Testcontainers 日志消费者：当日志行匹配给定正则时递减 {@link CountDownLatch}，
 * 用于等待 Keycloak/Infinispan 集群就绪信号。
 */
class CountdownLatchLoggingConsumer extends BaseConsumer<CountdownLatchLoggingConsumer> {

    private final CountDownLatch latch;
    private final Pattern pattern;

    /**
     * @param count 需匹配的次数（latch 初始计数）
     * @param regex 集群就绪日志行的正则表达式
     */
    public CountdownLatchLoggingConsumer(int count, String regex) {
        this.latch = new CountDownLatch(count);
        this.pattern = Pattern.compile(regex, Pattern.DOTALL);
    }

    /** 解析容器输出帧，匹配则 countDown。 */
    @Override
    public void accept(OutputFrame outputFrame) {
        String log = outputFrame.getUtf8String();
        if (pattern.matcher(log).matches()) {
            latch.countDown();
        }
    }

    /**
     * 等待 latch 归零。
     * @param timeout 超时数值
     * @param unit 时间单位
     * @throws TimeoutException 超时未就绪
     */
    public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (!latch.await(timeout, unit)) {
            throw new TimeoutException(String.format("After the await period %d %s the count down should be 0 and is %d", timeout, unit, latch.getCount()));
        }
    }
}
