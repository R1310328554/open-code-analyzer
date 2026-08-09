package com.taobao.arthas.grpcweb.grpc.service;


import com.taobao.arthas.core.advisor.TransformerManager;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;
import com.taobao.arthas.grpcweb.grpc.view.GrpcResultViewResolver;

import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * gRPC 后台任务（job）注册中心：为每个双向流分配 jobId 并持有
 * {@link ArthasStreamObserver} 引用，供 watch 等长连接命令管理与查询。
 */
public class GrpcJobController{

    /** jobId → 流观察者映射，支持并发注册 */
    private Map<Long/*JOB_ID*/, ArthasStreamObserver> jobs
            = new ConcurrentHashMap<Long, ArthasStreamObserver>();
//    private Map<Long/*JOB_ID*/, ArthasStreamObserver> jobs
//            = new HashMap<>();
    /** 自增 jobId 生成器 */
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    private GrpcResultViewResolver resultViewResolver;

    private Instrumentation instrumentation;

    private TransformerManager transformerManager;

    public GrpcJobController(Instrumentation instrumentation, TransformerManager transformerManager, GrpcResultViewResolver resultViewResolver){
        this.instrumentation = instrumentation;
        this.transformerManager = transformerManager;
        this.resultViewResolver = resultViewResolver;
    }

    /** @return 当前所有活跃 jobId 集合 */
    public Set<Long> getJobIds(){
        return jobs.keySet();
    }

    /**
     * 注册一条 gRPC 流任务。
     *
     * @param jobId                分配的任务 ID
     * @param arthasStreamObserver 对应的流观察者
     */
    public void registerGrpcJob(long jobId,ArthasStreamObserver arthasStreamObserver){
        jobs.put(jobId, arthasStreamObserver);
    }

    /** 从注册表移除指定 job */
    public void unRegisterGrpcJob(long jobId){
        if(jobs.containsKey(jobId)){
            jobs.remove(jobId);
        }
    }

    /** @return 是否仍存在该 jobId */
    public boolean containsJob(long jobId){
        return jobs.containsKey(jobId);
    }

    /**
     * 按 jobId 查找流观察者。
     *
     * @return 观察者实例；不存在时返回 null
     */
    public ArthasStreamObserver getGrpcJob(long jobId){
        if(this.containsJob(jobId)){
            return jobs.get(jobId);
        }else {
            return null;
        }
    }

    /** 生成全局唯一的递增 jobId */
    public int generateGrpcJobId(){
        int jobId = idGenerator.incrementAndGet();
        return jobId;
    }

    public GrpcResultViewResolver getResultViewResolver() {
        return resultViewResolver;
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public void setInstrumentation(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    public TransformerManager getTransformerManager() {
        return transformerManager;
    }

    public void setTransformerManager(TransformerManager transformerManager) {
        this.transformerManager = transformerManager;
    }
}
