package com.taobao.arthas.core.command.model;

/**
 * {@code EnhancerCommand} 的执行结果数据模型。
 * <p>
 * 封装增强影响统计 {@link EnhancerAffectVO}、成功与否标志及可选消息。
 *
 * @author gongdewei 2020/7/20
 */
public class EnhancerModel extends ResultModel {

    /** 增强操作的影响统计详情。 */
    private EnhancerAffectVO effect;
    /** 增强是否成功完成。 */
    private boolean success;
    /** 结果附加说明或错误消息。 */
    private String message;

    public EnhancerModel() {
    }

    public EnhancerModel(EnhancerAffectVO effect, boolean success) {
        this.effect = effect;
        this.success = success;
    }

    public EnhancerModel(EnhancerAffectVO effect, boolean success, String message) {
        this.effect = effect;
        this.success = success;
        this.message = message;
    }

    @Override
    public String getType() {
        return "enhancer";
    }

    public EnhancerAffectVO getEffect() {
        return effect;
    }

    public void setEffect(EnhancerAffectVO effect) {
        this.effect = effect;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
