package com.taobao.arthas.core.distribution;

import com.taobao.arthas.core.command.model.ResultModel;

import java.util.List;

/**
 * 打包式结果分发器：将命令执行期间产生的分阶段结果缓存在内存队列中，
 * 待命令结束后一次性取出完整结果列表。
 * <p>
 * 适用于同步命令场景（如 HTTP API 调用），调用方通过 {@link #getResults()} 拉取全部输出。
 *
 * @see PackingResultDistributorImpl
 */
public interface PackingResultDistributor extends ResultDistributor {

    /**
     * 取出并清空队列中缓存的全部命令结果。
     *
     * @return 按产生顺序排列的结果列表；队列为空时返回空列表
     */
    List<ResultModel> getResults();

}
