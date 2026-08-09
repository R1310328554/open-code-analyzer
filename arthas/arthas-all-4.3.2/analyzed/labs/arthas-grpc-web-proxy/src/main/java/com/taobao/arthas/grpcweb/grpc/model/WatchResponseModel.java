package com.taobao.arthas.grpcweb.grpc.model;

import com.taobao.arthas.core.command.model.WatchModel;

/**
 * watch 命令的 gRPC 响应模型，在 {@link WatchModel} 基础上增加结果序号。
 * <p>
 * {@code resultId} 用于客户端区分同一次 watch 任务中的多条流式输出。
 */
public class WatchResponseModel extends WatchModel {

    /** 本条 watch 结果在任务内的序号 */
    private long resultId;

    /** @return 结果序号 */
    public long getResultId() {
        return resultId;
    }

    public void setResultId(long resultId) {
        this.resultId = resultId;
    }
}
