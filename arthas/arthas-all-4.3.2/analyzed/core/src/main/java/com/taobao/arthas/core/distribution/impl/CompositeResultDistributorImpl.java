package com.taobao.arthas.core.distribution.impl;

import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.core.distribution.CompositeResultDistributor;
import com.taobao.arthas.core.distribution.ResultDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link CompositeResultDistributor} 的默认实现：维护子分发器列表，
 * 将每条命令结果依次转发给所有已注册的子分发器。
 * <p>
 * 典型组合为 {@link TermResultDistributorImpl}（终端输出）+
 * {@link SharingResultDistributorImpl}（远程推送）。
 *
 * @author gongdewei 2020/4/30
 */
public class CompositeResultDistributorImpl implements CompositeResultDistributor {

    /** 线程安全的子分发器列表 */
    private List<ResultDistributor> distributors = Collections.synchronizedList(new ArrayList<ResultDistributor>());

    public CompositeResultDistributorImpl() {
    }

    /** 构造时批量注册子分发器 */
    public CompositeResultDistributorImpl(ResultDistributor ... distributors) {
        for (ResultDistributor distributor : distributors) {
            this.addDistributor(distributor);
        }
    }

    @Override
    public void addDistributor(ResultDistributor distributor) {
        distributors.add(distributor);
    }

    @Override
    public void removeDistributor(ResultDistributor distributor) {
        distributors.remove(distributor);
    }

    @Override
    public void appendResult(ResultModel result) {
        // 广播：每个子分发器各自处理同一条结果
        for (ResultDistributor distributor : distributors) {
            distributor.appendResult(result);
        }
    }

    @Override
    public void close() {
        // 关闭时逐个释放子分发器资源
        for (ResultDistributor distributor : distributors) {
            distributor.close();
        }
    }
}
