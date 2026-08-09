package com.taobao.arthas.core.command.view;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 终端侧 {@link ResultView} 注册表与解析器。
 * <p>
 * 启动时按命令模块批量注册 Model→View 映射；命令执行完成后根据
 * {@link ResultModel} 运行时类型选取对应渲染器。映射表线程安全，View 实例无状态可共享。
 *
 * @author gongdewei 2020/3/27
 */
public class ResultViewResolver {
    private static final Logger logger = LoggerFactory.getLogger(ResultViewResolver.class);

    /** ResultModel 子类 → 对应 ResultView 实例 */
    private Map<Class, ResultView> resultViewMap = new ConcurrentHashMap<Class, ResultView>();

    public ResultViewResolver() {
        initResultViews();
    }

    /**
     * 注册全部内置 ResultView；构造器内调用，失败时记录日志但不抛出。
     */
    private void initResultViews() {
        try {
            registerView(RowAffectView.class);

            //basic1000
            registerView(StatusView.class);
            registerView(VersionView.class);
            registerView(MessageView.class);
            registerView(HelpView.class);
            //registerView(HistoryView.class);
            registerView(EchoView.class);
            registerView(CatView.class);
            registerView(Base64View.class);
            registerView(OptionsView.class);
            registerView(SystemPropertyView.class);
            registerView(SystemEnvView.class);
            registerView(PwdView.class);
            registerView(VMOptionView.class);
            registerView(SessionView.class);
            registerView(ResetView.class);
            registerView(ShutdownView.class);

            //klass100
            registerView(ClassLoaderView.class);
            registerView(ClassLoaderMetaspaceView.class);
            registerView(DumpClassView.class);
            registerView(GetStaticView.class);
            registerView(JadView.class);
            registerView(MemoryCompilerView.class);
            registerView(OgnlView.class);
            registerView(RedefineView.class);
            registerView(RetransformView.class);
            registerView(SearchClassView.class);
            registerView(SearchMethodView.class);

            //logger
            registerView(LoggerView.class);

            //monitor2000
            registerView(DashboardView.class);
            registerView(JvmView.class);
            registerView(MemoryView.class);
            registerView(MBeanView.class);
            registerView(PerfCounterView.class);
            registerView(ThreadView.class);
            registerView(ProfilerView.class);
            registerView(EnhancerView.class);
            registerView(MonitorView.class);
            registerView(StackView.class);
            registerView(TimeTunnelView.class);
            registerView(TraceView.class);
            registerView(WatchView.class);
            registerView(LineView.class);
            registerView(LineListView.class);
            registerView(VmToolView.class);
            registerView(JFRView.class);

        } catch (Throwable e) {
            logger.error("register result view failed", e);
        }
    }

    /** 按模型运行时 Class 查找已注册的 View；未注册时返回 null */
    public ResultView getResultView(ResultModel model) {
        return resultViewMap.get(model.getClass());
    }

    public ResultViewResolver registerView(Class modelClass, ResultView view) {
        //TODO 检查model的type是否重复，避免复制代码带来的bug
        this.resultViewMap.put(modelClass, view);
        return this;
    }

    /** 从 View 实例反射推断 Model 类型并注册 */
    public ResultViewResolver registerView(ResultView view) {
        Class modelClass = getModelClass(view);
        if (modelClass == null) {
            throw new NullPointerException("model class is null");
        }
        return this.registerView(modelClass, view);
    }

    /** 通过无参构造实例化 View 类并完成注册 */
    public void registerView(Class<? extends ResultView> viewClass) {
        ResultView view = null;
        try {
            view = viewClass.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException("create view instance failure, viewClass:" + viewClass, e);
        }
        this.registerView(view);
    }

    /**
     * 反射解析 View 的 {@code draw(CommandProcess, T)} 第二个参数类型作为 Model Class。
     *
     * @return Model 类型；找不到符合签名的 draw 方法时返回 null
     */
    public static <V extends ResultView> Class getModelClass(V view) {
        //类反射获取子类的draw方法第二个参数的ResultModel具体类型
        Class<? extends ResultView> viewClass = view.getClass();
        Method[] declaredMethods = viewClass.getDeclaredMethods();
        for (int i = 0; i < declaredMethods.length; i++) {
            Method method = declaredMethods[i];
            if (method.getName().equals("draw")) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 2
                        && parameterTypes[0] == CommandProcess.class
                        && parameterTypes[1] != ResultModel.class
                        && ResultModel.class.isAssignableFrom(parameterTypes[1])) {
                    return parameterTypes[1];
                }
            }
        }
        return null;
    }
}
