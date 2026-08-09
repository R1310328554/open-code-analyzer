/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava4.exceptions;

import java.io.*;
import java.util.*;

import io.reactivex.rxjava4.annotations.NonNull;

/**
 * 表示由一个或多个其它异常组成的复合异常。{@code CompositeException}
 * 不修改其所包装异常的结构，但在打印时会遍历复合体中包含的 Throwable 列表并依次打印。
 *
 * 其不变量为：包含不可变、有序（按插入顺序）、唯一的非复合异常列表。
 * 可通过 {@link #getExceptions()} 获取列表中的各个异常。
 *
 * {@link #printStackTrace()} 实现以定制方式处理 StackTrace，而非使用 {@code getCause()}，
 * 以避免循环引用。
 *
 * 若调用 {@link #getCause()}，将惰性创建因果链，但若在链中发现已见过的 Throwable 则停止。
 */
public final class CompositeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3026362227162912146L;

    private final List<Throwable> exceptions;
    private final String message;
    private Throwable cause;

    /**
     * 使用给定 Throwable 数组作为初始 suppressed 异常列表构造 CompositeException。
     * @param exceptions 作为初始 suppressed 异常的 Throwable
     *
     * @throws IllegalArgumentException 若 <code>exceptions</code> 为空。
     */
    public CompositeException(@NonNull Throwable... exceptions) {
        this(exceptions == null ?
                Collections.singletonList(new NullPointerException("exceptions was null")) : Arrays.asList(exceptions));
    }

    /**
     * 使用给定 Throwable 集合作为初始 suppressed 异常列表构造 CompositeException。
     * @param errors 作为初始 suppressed 异常的 Throwable
     *
     * @throws IllegalArgumentException 若 <code>errors</code> 为空。
     */
    public CompositeException(@NonNull Iterable<? extends Throwable> errors) {
        Set<Throwable> deDupedExceptions = new LinkedHashSet<>();
        if (errors != null) {
            for (Throwable ex : errors) {
                if (ex instanceof CompositeException ce) {
                    deDupedExceptions.addAll(ce.getExceptions());
                } else {
                    deDupedExceptions.add(Objects.requireNonNullElseGet(ex,
                            () -> new NullPointerException("Throwable was null!")));
                }
            }
        } else {
            deDupedExceptions.add(new NullPointerException("errors was null"));
        }
        if (deDupedExceptions.isEmpty()) {
            throw new IllegalArgumentException("errors is empty");
        }
        List<Throwable> localExceptions = new ArrayList<>(deDupedExceptions);
        this.exceptions = Collections.unmodifiableList(localExceptions);
        this.message = exceptions.size() + " exceptions occurred. ";
    }

    /**
     * 获取构成 {@code CompositeException} 的异常列表。
     *
     * @return 构成 {@code CompositeException} 的异常，以 {@link List}{@code <}{@link Throwable}{@code >} 形式返回
     */
    @NonNull
    public List<Throwable> getExceptions() {
        return exceptions;
    }

    @Override
    @NonNull
    public String getMessage() {
        return message;
    }

    @Override
    @NonNull
    public synchronized Throwable getCause() { // NOPMD
        if (cause == null) {
            String separator = System.getProperty("line.separator");
            if (exceptions.size() > 1) {
                Map<Throwable, Boolean> seenCauses = new IdentityHashMap<>();

                StringBuilder aggregateMessage = new StringBuilder();
                aggregateMessage.append("Multiple exceptions (").append(exceptions.size()).append(")").append(separator);

                for (Throwable inner : exceptions) {
                    int depth = 0;
                    while (inner != null) {
                        aggregateMessage.repeat("  ", Math.max(0, depth));
                        aggregateMessage.append("|-- ");
                        aggregateMessage.append(inner.getClass().getCanonicalName()).append(": ");
                        String innerMessage = inner.getMessage();
                        if (innerMessage != null && innerMessage.contains(separator)) {
                            aggregateMessage.append(separator);
                            for (String line : innerMessage.split(separator)) {
                                aggregateMessage.repeat("  ", Math.max(0, depth + 2));
                                aggregateMessage.append(line).append(separator);
                            }
                        } else {
                            aggregateMessage.append(innerMessage);
                            aggregateMessage.append(separator);
                        }

                        aggregateMessage.repeat("  ", Math.max(0, depth + 2));
                        StackTraceElement[] st = inner.getStackTrace();
                        if (st.length > 0) {
                            aggregateMessage.append("at ").append(st[0]).append(separator);
                        }

                        if (!seenCauses.containsKey(inner)) {
                            seenCauses.put(inner, true);

                            inner = inner.getCause();
                            depth++;
                        } else {
                            inner = inner.getCause();
                            if (inner != null) {
                                aggregateMessage.repeat("  ", Math.max(0, depth + 2));
                                aggregateMessage.append("|-- ");
                                aggregateMessage.append("(cause not expanded again) ");
                                aggregateMessage.append(inner.getClass().getCanonicalName()).append(": ");
                                aggregateMessage.append(inner.getMessage());
                                aggregateMessage.append(separator);
                            }
                            break;
                        }
                    }
                }

                cause = new ExceptionOverview(aggregateMessage.toString().trim());
            } else {
                cause = exceptions.getFirst();
            }
        }
        return cause;
    }

    /**
     * 以下 {@code printStackTrace} 功能源自 JDK {@link Throwable} 的 {@code printStackTrace}。
     * 尤其是 {@code PrintStreamOrWriter} 抽象被完整复制。
     *
     * 与官方 JDK 实现的差异：<ul>
     * <li>无无限循环检测</li>
     * <li>持有 {@link PrintStream} 锁的临界区更小</li>
     * <li>显式知晓所遍历的异常 {@link List}</li>
     * </ul>
     */
    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream s) {
        printStackTrace(new WrappedPrintStream(s));
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        printStackTrace(new WrappedPrintWriter(s));
    }

    /**
     * 打印 {@code CompositeException} 的特殊处理。
     * 遍历所有内部异常并打印。
     *
     * @param output
     *            要打印到的流
     */
    private void printStackTrace(PrintStreamOrWriter output) {
        output.append(this).append("\n");
        for (StackTraceElement myStackElement : getStackTrace()) {
            output.append("\tat ").append(myStackElement).append("\n");
        }
        int i = 1;
        for (Throwable ex : exceptions) {
            output.append("  ComposedException ").append(i).append(" :\n");
            appendStackTrace(output, ex, "\t");
            i++;
        }
        output.append("\n");
    }

    private void appendStackTrace(PrintStreamOrWriter output, Throwable ex, String prefix) {
        output.append(prefix).append(ex).append('\n');
        for (StackTraceElement stackElement : ex.getStackTrace()) {
            output.append("\t\tat ").append(stackElement).append('\n');
        }
        if (ex.getCause() != null) {
            output.append("\tCaused by: ");
            appendStackTrace(output, ex.getCause(), "");
        }
    }

    abstract static class PrintStreamOrWriter {
        /**
         * 通过底层 PrintStream 或 PrintWriter 打印对象的字符串表示。
         * @param o 要打印的对象
         * @return this
         */
        abstract PrintStreamOrWriter append(Object o);
    }

    /**
     * 与 JDK 中相同的抽象与实现，使 PrintStream 与 PrintWriter 可共享实现。
     */
    static final class WrappedPrintStream extends PrintStreamOrWriter {
        private final PrintStream printStream;

        WrappedPrintStream(PrintStream printStream) {
            this.printStream = printStream;
        }

        @Override
        WrappedPrintStream append(Object o) {
            printStream.print(o);
            return this;
        }
    }

    /**
     * 与 JDK 中相同的抽象与实现，使 PrintStream 与 PrintWriter 可共享实现。
     */
    static final class WrappedPrintWriter extends PrintStreamOrWriter {
        private final PrintWriter printWriter;

        WrappedPrintWriter(PrintWriter printWriter) {
            this.printWriter = printWriter;
        }

        @Override
        WrappedPrintWriter append(Object o) {
            printWriter.print(o);
            return this;
        }
    }

    /**
     * 包含格式化消息，以简化形式表示 CompositeException 内的异常图。
     */
    static final class ExceptionOverview extends RuntimeException {

        @Serial
        private static final long serialVersionUID = 3875212506787802066L;

        ExceptionOverview(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    /**
     * 返回 suppressed 异常的数量。
     * @return suppressed 异常的数量
     */
    public int size() {
        return exceptions.size();
    }
}
