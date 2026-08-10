package org.keycloak.testframework.injection;

/**
 * 可在测试间复用、支持清理与脏标记的托管资源基类。
 * <p>
 * 子类实现 {@link #runCleanup()} 做轻量重置；调用 {@link #dirty()} 后框架将在测试结束后重建实例。
 */
public abstract class ManagedTestResource {

    /** 是否已标记为脏，需要销毁并重建。 */
    private boolean dirty = false;

    /** 每个测试方法结束后执行的非破坏性清理逻辑。 */
    public abstract void runCleanup();

    /** @return 资源是否已被标记为脏 */
    boolean isDirty() {
        return dirty;
    }

    /**
     * 将资源标记为脏；测试执行结束后框架将销毁并重新创建该资源。
     */
    public void dirty() {
        this.dirty = true;
    }

}
