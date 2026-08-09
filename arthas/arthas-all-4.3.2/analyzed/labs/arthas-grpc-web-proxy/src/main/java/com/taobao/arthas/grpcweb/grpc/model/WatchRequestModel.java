package com.taobao.arthas.grpcweb.grpc.model;

import io.arthas.api.ArthasServices.WatchRequest;
import com.taobao.arthas.core.GlobalOptions;
import com.taobao.arthas.core.advisor.AdviceListener;
import com.taobao.arthas.core.advisor.AdviceWeaver;
import com.taobao.arthas.core.util.SearchUtils;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.util.matcher.Matcher;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;
import com.taobao.arthas.grpcweb.grpc.service.advisor.WatchRpcAdviceListener;

/**
 * watch 命令的 gRPC 请求模型，将 {@link WatchRequest} 解析为增强参数并驱动插桩。
 * <p>
 * 负责类/方法匹配器懒加载、监听器复用或新建，以及 watch 特有的观察点与表达式配置。
 */
public class WatchRequestModel extends EnhancerRequestModel {
    /** 类名匹配模式 */
    private String classPattern;
    /** 方法名匹配模式 */
    private String methodPattern;
    /** 观察表达式，默认 {@code {params, target, returnObj}} */
    private String express;
    /** 条件表达式，满足时才输出 */
    private String conditionExpress;
    /** 是否在方法进入时观察 */
    private boolean isBefore = false;
    /** 是否在方法正常返回时观察 */
    private boolean isFinish = false;
    /** 是否在抛异常时观察 */
    private boolean isException = false;
    /** 是否在调用成功时观察 */
    private boolean isSuccess = false;
    /** 对象展开深度，默认 1 */
    private Integer expand = 1;
    /** 序列化结果大小上限（字节），默认 10MB */
    private Integer sizeLimit = 10 * 1024 * 1024;
    /** 类/方法模式是否按正则匹配 */
    private boolean isRegEx = false;
    /** 最多输出条数，默认 100 */
    private int numberOfLimit = 100;
    /** 对象展开深度上限 */
    private static final int MAX_EXPAND = 4;

    @Override
    public String toString() {
        return "WatchRequestModel{" +
                "classPattern='" + classPattern + '\'' +
                ", methodPattern='" + methodPattern + '\'' +
                ", express='" + express + '\'' +
                ", conditionExpress='" + conditionExpress + '\'' +
                ", isBefore=" + isBefore +
                ", isFinish=" + isFinish +
                ", isException=" + isException +
                ", isSuccess=" + isSuccess +
                ", expand=" + expand +
                ", sizeLimit=" + sizeLimit +
                ", isRegEx=" + isRegEx +
                ", numberOfLimit=" + numberOfLimit +
                ", excludeClassPattern='" + excludeClassPattern + '\'' +
                ", jobId=" + jobId +
                ", listenerId=" + listenerId +
                ", verbose=" + verbose +
                ", maxNumOfMatchedClass=" + maxNumOfMatchedClass +
                '}';
    }

    /**
     * 从 gRPC {@link WatchRequest} 构造模型并解析全部参数字段。
     *
     * @param watchRequest 客户端传入的 watch 请求
     */
    public WatchRequestModel(WatchRequest watchRequest) {
        parseRequestParams(watchRequest);
    }

    @Override
    public Matcher getClassNameMatcher() {
        if (classNameMatcher == null) {
            classNameMatcher = SearchUtils.classNameMatcher(getClassPattern(), isRegEx());
        }
        return classNameMatcher;
    }

    @Override
    public Matcher getMethodNameMatcher() {
        if (methodNameMatcher == null) {
            methodNameMatcher = SearchUtils.classNameMatcher(getMethodPattern(), isRegEx());
        }
        return methodNameMatcher;
    }

    /**
     * 获取或创建 watch 监听器：优先按 listenerId 复用已有 Advice，否则新建
     * {@link WatchRpcAdviceListener}。
     */
    @Override
    protected AdviceListener getAdviceListener(ArthasStreamObserver arthasStreamObserver) {
        WatchRequestModel watchRequestModel = (WatchRequestModel) arthasStreamObserver.getRequestModel();
        if (watchRequestModel.getListenerId()!= 0) {
            AdviceListener listener = AdviceWeaver.listener(watchRequestModel.getListenerId());
            if (listener != null) {
                return listener;
            }
        }
        return new WatchRpcAdviceListener(arthasStreamObserver, GlobalOptions.verbose || watchRequestModel.isVerbose());
    }

    @Override
    public Matcher getClassNameExcludeMatcher() {
        if (classNameExcludeMatcher == null && getExcludeClassPattern() != null) {
            classNameExcludeMatcher = SearchUtils.classNameMatcher(getExcludeClassPattern(), isRegEx());
        }
        return classNameExcludeMatcher;
    }

    /**
     * 将 protobuf 请求字段映射到模型属性，并填充默认值与边界校验。
     *
     * @param watchRequest gRPC watch 请求
     */
    public void parseRequestParams(WatchRequest watchRequest){
        this.classPattern = watchRequest.getClassPattern();
        this.methodPattern = watchRequest.getMethodPattern();
        if(StringUtils.isEmpty(watchRequest.getExpress())){
            this.express = "{params, target, returnObj}";
        }else {
            this.express = watchRequest.getExpress();
        }
        this.conditionExpress = watchRequest.getConditionExpress();
        this.isBefore = watchRequest.getIsBefore();
        this.isFinish = watchRequest.getIsFinish();
        this.isException = watchRequest.getIsException();
        this.isSuccess = watchRequest.getIsSuccess();
        // 未指定任何观察点时默认观察方法结束
        if (!watchRequest.getIsBefore() && !watchRequest.getIsFinish() && !watchRequest.getIsException() && !watchRequest.getIsSuccess()) {
            this.isFinish = true;
        }
        if (watchRequest.getExpand() <= 0) {
            this.expand = 1;
        } else if (watchRequest.getExpand() > MAX_EXPAND){
            this.expand = MAX_EXPAND;
        } else {
            this.expand = watchRequest.getExpand();
        }
        if (watchRequest.getSizeLimit() == 0) {
            this.sizeLimit = 10 * 1024 * 1024;
        } else {
            this.sizeLimit = watchRequest.getSizeLimit();
        }
        this.isRegEx = watchRequest.getIsRegEx();
        if (watchRequest.getNumberOfLimit() == 0) {
            this.numberOfLimit = 100;
        } else {
            this.numberOfLimit = watchRequest.getNumberOfLimit();
        }
        if(watchRequest.getExcludeClassPattern().equals("")){
            this.excludeClassPattern = null;
        }else {
            this.excludeClassPattern = watchRequest.getExcludeClassPattern();
        }
        this.listenerId = watchRequest.getListenerId();
        this.verbose = watchRequest.getVerbose();
        if(watchRequest.getMaxNumOfMatchedClass() == 0){
            this.maxNumOfMatchedClass = 50;
        }else {
            this.maxNumOfMatchedClass = watchRequest.getMaxNumOfMatchedClass();
        }
        this.jobId = watchRequest.getJobId();
    }

    /** @return 类名匹配模式 */
    public String getClassPattern() {
        return classPattern;
    }

    public void setClassPattern(String classPattern) {
        this.classPattern = classPattern;
    }

    /** @return 方法名匹配模式 */
    public String getMethodPattern() {
        return methodPattern;
    }

    public void setMethodPattern(String methodPattern) {
        this.methodPattern = methodPattern;
    }

    /** @return 观察表达式 */
    public String getExpress() {
        return express;
    }

    public void setExpress(String express) {
        this.express = express;
    }

    /** @return 条件表达式 */
    public String getConditionExpress() {
        return conditionExpress;
    }

    public void setConditionExpress(String conditionExpress) {
        this.conditionExpress = conditionExpress;
    }

    /** @return 是否在方法进入时观察 */
    public boolean isBefore() {
        return isBefore;
    }

    public void setBefore(boolean before) {
        isBefore = before;
    }

    /** @return 是否在方法正常返回时观察 */
    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    /** @return 是否在抛异常时观察 */
    public boolean isException() {
        return isException;
    }

    public void setException(boolean exception) {
        isException = exception;
    }

    /** @return 是否在调用成功时观察 */
    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    /** @return 对象展开深度 */
    public Integer getExpand() {
        return expand;
    }

    public void setExpand(Integer expand) {
        this.expand = expand;
    }

    /** @return 结果大小上限（字节） */
    public Integer getSizeLimit() {
        return sizeLimit;
    }

    public void setSizeLimit(Integer sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    /** @return 是否使用正则匹配类/方法名 */
    public boolean isRegEx() {
        return isRegEx;
    }

    public void setRegEx(boolean regEx) {
        isRegEx = regEx;
    }

    /** @return 最多输出条数 */
    public int getNumberOfLimit() {
        return numberOfLimit;
    }

    public void setNumberOfLimit(int numberOfLimit) {
        this.numberOfLimit = numberOfLimit;
    }

    /** @return 排除类名模式 */
    public String getExcludeClassPattern() {
        return excludeClassPattern;
    }

    public void setExcludeClassPattern(String excludeClassPattern) {
        this.excludeClassPattern = excludeClassPattern;
    }

    public void setClassNameMatcher(Matcher classNameMatcher) {
        this.classNameMatcher = classNameMatcher;
    }

    public void setClassNameExcludeMatcher(Matcher classNameExcludeMatcher) {
        this.classNameExcludeMatcher = classNameExcludeMatcher;
    }

    public void setMethodNameMatcher(Matcher methodNameMatcher) {
        this.methodNameMatcher = methodNameMatcher;
    }

    /** @return 复用的监听器 ID */
    public long getListenerId() {
        return listenerId;
    }

    public void setListenerId(long listenerId) {
        this.listenerId = listenerId;
    }

    /** @return 是否 verbose 模式 */
    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /** @return 最多匹配类数量 */
    public int getMaxNumOfMatchedClass() {
        return maxNumOfMatchedClass;
    }

    public void setMaxNumOfMatchedClass(int maxNumOfMatchedClass) {
        this.maxNumOfMatchedClass = maxNumOfMatchedClass;
    }

    /** @return 后台任务 ID */
    public long getJobId() {
        return jobId;
    }
}
