package com.alibaba.csp.sentinel.demo.datasource.apollo;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.apollo.ApolloDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;

/**
 * 演示以 Apollo 作为 Sentinel 规则动态数据源。
 * <p>使用前请在 Apollo 中完成以下配置：</p>
 * <ol>
 *  <li>创建 appId 为 sentinel-demo 的应用</li>
 *  <li>
 *    新增 key 为 flowRules 的配置，值为：
 *    <pre>
 *      [
          {
            "resource": "TestResource",
            "controlBehavior": 0,
            "count": 5.0,
            "grade": 1,
            "limitApp": "default",
            "strategy": 0
          }
        ]
 *    </pre>
 *  </li>
 *  <li>发布 application 命名空间</li>
 * </ol>
 * 启动后可实时修改 Apollo 中的规则，变更会立即生效。
 *
 * @author Jason Song
 */
public class ApolloDataSourceDemo {

    private static final String KEY = "TestResource";

    public static void main(String[] args) {
        loadRules();
        // 假定 Apollo 中 resource=TestResource、初始 QPS 阈值为 5
        FlowQpsRunner runner = new FlowQpsRunner(KEY, 1, 100);
        runner.simulateTraffic();
        runner.tick();
    }

    private static void loadRules() {
        // 演示用 Apollo 连接参数，生产环境请按实际部署调整，详见 https://github.com/ctripcorp/apollo
        String appId = "sentinel-demo";
        String apolloMetaServerAddress = "http://localhost:8080";
        System.setProperty("app.id", appId);
        System.setProperty("apollo.meta", apolloMetaServerAddress);

        String namespaceName = "application";
        String flowRuleKey = "flowRules";
        // 建议提供有意义的默认规则 JSON，避免 Apollo 无配置时解析失败
        String defaultFlowRules = "[]";

        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ApolloDataSource<>(namespaceName,
            flowRuleKey, defaultFlowRules, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
        }));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }
}
