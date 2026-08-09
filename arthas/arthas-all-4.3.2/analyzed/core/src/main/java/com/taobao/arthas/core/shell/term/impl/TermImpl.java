package com.taobao.arthas.core.shell.term.impl;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.handlers.term.CloseHandlerWrapper;
import com.taobao.arthas.core.shell.handlers.term.DefaultTermStdinHandler;
import com.taobao.arthas.core.shell.handlers.term.EventHandler;
import com.taobao.arthas.core.shell.handlers.term.RequestHandler;
import com.taobao.arthas.core.shell.handlers.term.SizeHandlerWrapper;
import com.taobao.arthas.core.shell.handlers.term.StdinHandlerWrapper;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.term.SignalHandler;
import com.taobao.arthas.core.shell.term.Term;
import com.taobao.arthas.core.util.Constants;
import com.taobao.arthas.core.util.FileUtils;

import io.termd.core.function.Consumer;
import io.termd.core.readline.Function;
import io.termd.core.readline.Keymap;
import io.termd.core.readline.Readline;
import io.termd.core.readline.functions.HistorySearchForward;
import io.termd.core.tty.TtyConnection;
import io.termd.core.util.Helper;

/**
 * {@link Term} 的 termd 实现：readline、stdin/stdout、信号与终端尺寸。
 * <p>
 * 桥接 {@link TtyConnection} 与 Arthas Shell，管理命令行编辑、历史、补全及
 * Ctrl-C/Ctrl-Z 等控制字符；HTTP/Telnet 终端均通过本类与 Shell 交互。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class TermImpl implements Term {

    /** SPI 加载的 readline 扩展函数列表（含 history 搜索等） */
    private static final List<Function> readlineFunctions = Helper.loadServices(Function.class.getClassLoader(), Function.class);

    /** termd readline 引擎，负责行编辑与历史 */
    private Readline readline;
    /** 默认 stdin 处理器：echo 模式下的字符回显 */
    private Consumer<int[]> echoHandler;
    /** 底层 TTY 连接（Telnet 或 WebSocket） */
    private TtyConnection conn;
    /** 非 readline 模式下 Shell 注册的 stdin 回调 */
    private volatile Handler<String> stdinHandler;
    private List<io.termd.core.function.Function<String, String>> stdoutHandlerChain;
    private SignalHandler interruptHandler;
    private SignalHandler suspendHandler;
    private Session session;
    /** 是否处于 readline 阻塞读行状态，防止重入 */
    private boolean inReadline;

    /** 使用默认 keymap 构造 Term */
    public TermImpl(TtyConnection conn) {
        this(com.taobao.arthas.core.shell.term.impl.Helper.loadKeymap(), conn);
    }

    /** @param keymap 键位映射；@param conn TTY 连接 */
    public TermImpl(Keymap keymap, TtyConnection conn) {
        this.conn = conn;
        readline = new Readline(keymap);
        readline.setHistory(FileUtils.loadCommandHistory(new File(Constants.CMD_HISTORY_FILE)));
        for (Function function : readlineFunctions) {
            /**
             * 防止没有鉴权时，查看历史命令
             * 
             * @see io.termd.core.readline.functions.HistorySearchForward
             */
            if (function.name().contains("history")) {
                FunctionInvocationHandler funcHandler = new FunctionInvocationHandler(this, function);
                function = (Function) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                        HistorySearchForward.class.getInterfaces(), funcHandler);

            }

            readline.addFunction(function);
        }

        echoHandler = new DefaultTermStdinHandler(this);
        conn.setStdinHandler(echoHandler);
        conn.setEventHandler(new EventHandler(this));
    }

    @Override
    public Term setSession(Session session) {
        this.session = session;
        return this;
    }

    public Session getSession() {
        return session;
    }

    @Override
    /** 启动 readline 读一行命令（无 Tab 补全） */
    public void readline(String prompt, Handler<String> lineHandler) {
        if (conn.getStdinHandler() != echoHandler) {
            throw new IllegalStateException();
        }
        if (inReadline) {
            throw new IllegalStateException();
        }
        inReadline = true;
        readline.readline(conn, prompt, new RequestHandler(this, lineHandler));
    }

    /** 启动 readline 并注册 Tab 补全处理器 */
    public void readline(String prompt, Handler<String> lineHandler, Handler<Completion> completionHandler) {
        if (conn.getStdinHandler() != echoHandler) {
            throw new IllegalStateException();
        }
        if (inReadline) {
            throw new IllegalStateException();
        }
        inReadline = true;
        readline.readline(conn, prompt, new RequestHandler(this, lineHandler), new CompletionHandler(completionHandler, session));
    }

    @Override
    public Term closeHandler(final Handler<Void> handler) {
        if (handler != null) {
            conn.setCloseHandler(new CloseHandlerWrapper(handler));
        } else {
            conn.setCloseHandler(null);
        }
        return this;
    }

    /** @return 底层连接最后访问时间戳 */
    public long lastAccessedTime() {
        return conn.lastAccessedTime();
    }

    @Override
    public String type() {
        return conn.terminalType();
    }

    @Override
    public int width() {
        return conn.size() != null ? conn.size().x() : -1;
    }

    @Override
    public int height() {
        return conn.size() != null ? conn.size().y() : -1;
    }

    /** 递归消费 readline 队列中待处理的 stdin 事件 */
    void checkPending() {
        if (stdinHandler != null && readline.hasEvent()) {
            stdinHandler.handle(Helper.fromCodePoints(readline.nextEvent().buffer().array()));
            checkPending();
        }
    }

    @Override
    public TermImpl resizehandler(Handler<Void> handler) {
        if (inReadline) {
            throw new IllegalStateException();
        }
        if (handler != null) {
            conn.setSizeHandler(new SizeHandlerWrapper(handler));
        } else {
            conn.setSizeHandler(null);
        }
        return this;
    }

    @Override
    public Term stdinHandler(final Handler<String> handler) {
        if (inReadline) {
            throw new IllegalStateException();
        }
        stdinHandler = handler;
        if (handler != null) {
            conn.setStdinHandler(new StdinHandlerWrapper(handler));
            checkPending();
        } else {
            conn.setStdinHandler(echoHandler);
        }
        return this;
    }

    @Override
    public Term stdoutHandler(io.termd.core.function.Function<String, String>  handler) {
        if (stdoutHandlerChain == null) {
            stdoutHandlerChain = new ArrayList<io.termd.core.function.Function<String, String>>();
        }
        stdoutHandlerChain.add(handler);
        return this;
    }

    @Override
    public Term write(String data) {
        if (stdoutHandlerChain != null) {
            for (io.termd.core.function.Function<String, String> function : stdoutHandlerChain) {
                data = function.apply(data);
            }
        }
        conn.write(data);
        return this;
    }

    /** 注册 Ctrl-C（INTR）信号处理器 */
    public TermImpl interruptHandler(SignalHandler handler) {
        interruptHandler = handler;
        return this;
    }

    /** 注册 Ctrl-Z（SUSP）信号处理器 */
    public TermImpl suspendHandler(SignalHandler handler) {
        suspendHandler = handler;
        return this;
    }

    /** 关闭 TTY 连接并将 readline 历史持久化到磁盘 */
    public void close() {
        conn.close();
        FileUtils.saveCommandHistory(readline.getHistory(), new File(Constants.CMD_HISTORY_FILE));
    }

    public TermImpl echo(String text) {
        echo(Helper.toCodePoints(text));
        return this;
    }

    public void setInReadline(boolean inReadline) {
        this.inReadline = inReadline;
    }

    public Readline getReadline() {
        return readline;
    }

    /** 处理中断键：优先交给 interruptHandler，否则 echo ^C */
    public void handleIntr(Integer key) {
        if (interruptHandler == null || !interruptHandler.deliver(key)) {
            echo(key, '\n');
        }
    }

    /** 处理 EOF（Ctrl-D）：转发 stdin 或入队 readline 事件 */
    public void handleEof(Integer key) {
        // 伪信号：EOF 在无 stdinHandler 时入队 readline
        if (stdinHandler != null) {
            stdinHandler.handle(Helper.fromCodePoints(new int[]{key}));
        } else {
            echo(key);
            readline.queueEvent(new int[]{key});
        }
    }

    /** 处理挂起键：优先 suspendHandler，否则 echo ^Z */
    public void handleSusp(Integer key) {
        if (suspendHandler == null || !suspendHandler.deliver(key)) {
            echo(key, 'Z' - 64);
        }
    }

    public TtyConnection getConn() {
        return conn;
    }

    /** 将 Unicode 码点按 TTY 规则渲染到 stdout（控制字符转义显示） */
    public void echo(int... codePoints) {
        Consumer<int[]> out = conn.stdoutHandler();
        for (int codePoint : codePoints) {
            if (codePoint < 32) {
                if (codePoint == '\t') {
                    out.accept(new int[]{'\t'});
                } else if (codePoint == '\b') {
                    out.accept(new int[]{'\b', ' ', '\b'});
                } else if (codePoint == '\r' || codePoint == '\n') {
                    out.accept(new int[]{'\n'});
                } else {
                    out.accept(new int[]{'^', codePoint + 64});
                }
            } else {
                if (codePoint == 127) {
                    out.accept(new int[]{'\b', ' ', '\b'});
                } else {
                    out.accept(new int[]{codePoint});
                }
            }
        }
    }
}
