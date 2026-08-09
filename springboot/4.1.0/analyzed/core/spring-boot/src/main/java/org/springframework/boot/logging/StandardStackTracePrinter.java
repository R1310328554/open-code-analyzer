/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.logging;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 打印标准形式堆栈跟踪的 {@link StackTracePrinter}。
 * 输出形态类似 {@link Throwable#printStackTrace()}，但提供更多定制选项。
 *
 * @author Phillip Webb
 * @since 3.5.0
 */
public final class StandardStackTracePrinter implements StackTracePrinter {

	private static final String DEFAULT_LINE_SEPARATOR = System.lineSeparator();

	private static final ToIntFunction<StackTraceElement> DEFAULT_FRAME_HASHER = (frame) -> Objects
		.hash(frame.getClassName(), frame.getMethodName(), frame.getLineNumber());

	private static final int UNLIMITED = Integer.MAX_VALUE;

	private final EnumSet<Option> options;

	private final int maximumLength;

	private final String lineSeparator;

	private final Predicate<Throwable> filter;

	private final BiPredicate<Integer, StackTraceElement> frameFilter;

	private final Function<Throwable, String> formatter;

	private final Function<StackTraceElement, String> frameFormatter;

	private final @Nullable ToIntFunction<StackTraceElement> frameHasher;

	private StandardStackTracePrinter(EnumSet<Option> options, int maximumLength, @Nullable String lineSeparator,
			@Nullable Predicate<Throwable> filter, @Nullable BiPredicate<Integer, StackTraceElement> frameFilter,
			@Nullable Function<Throwable, String> formatter,
			@Nullable Function<StackTraceElement, String> frameFormatter,
			@Nullable ToIntFunction<StackTraceElement> frameHasher) {
		this.options = options;
		this.maximumLength = maximumLength;
		this.lineSeparator = (lineSeparator != null) ? lineSeparator : DEFAULT_LINE_SEPARATOR;
		this.filter = (filter != null) ? filter : (t) -> true;
		this.frameFilter = (frameFilter != null) ? frameFilter : (i, t) -> true;
		this.formatter = (formatter != null) ? formatter : Object::toString;
		this.frameFormatter = (frameFormatter != null) ? frameFormatter : Object::toString;
		this.frameHasher = frameHasher;
	}

	@Override
	public void printStackTrace(Throwable throwable, Appendable out) throws IOException {
		if (this.filter.test(throwable)) {
			Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
			Output output = new Output(out);
			Print print = new Print("", "", output);
			printFullStackTrace(seen, print, new StackTrace(throwable), null);
		}
	}

	private void printFullStackTrace(Set<Throwable> seen, Print print, @Nullable StackTrace stackTrace,
			@Nullable StackTrace enclosing) throws IOException {
		if (stackTrace == null) {
			return;
		}
		if (!seen.add(stackTrace.throwable())) {
			String hashPrefix = stackTrace.hashPrefix(this.frameHasher);
			String throwable = this.formatter.apply(stackTrace.throwable());
			print.circularReference(hashPrefix, throwable);
			return;
		}
		StackTrace cause = stackTrace.cause();
		if (!hasOption(Option.ROOT_FIRST)) {
			printSingleStackTrace(seen, print, stackTrace, enclosing);
			printFullStackTrace(seen, print.withCausedByCaption(cause), cause, stackTrace);
		}
		else {
			printFullStackTrace(seen, print, cause, stackTrace);
			printSingleStackTrace(seen, print.withWrappedByCaption(cause), stackTrace, enclosing);
		}
	}

	private void printSingleStackTrace(Set<Throwable> seen, Print print, StackTrace stackTrace,
			@Nullable StackTrace enclosing) throws IOException {
		String hashPrefix = stackTrace.hashPrefix(this.frameHasher);
		String throwable = this.formatter.apply(stackTrace.throwable());
		print.thrown(hashPrefix, throwable);
		printFrames(print, stackTrace, enclosing);
		if (!hasOption(Option.HIDE_SUPPRESSED)) {
			StackTrace[] suppressed = stackTrace.suppressed();
			if (suppressed != null) {
				for (StackTrace suppressedStackTrace : suppressed) {
					printFullStackTrace(seen, print.withSuppressedCaption(), suppressedStackTrace, stackTrace);
				}
			}
		}
	}

	private void printFrames(Print print, StackTrace stackTrace, @Nullable StackTrace enclosing) throws IOException {
		int commonFrames = (!hasOption(Option.SHOW_COMMON_FRAMES)) ? stackTrace.commonFramesCount(enclosing) : 0;
		int filteredFrames = 0;
		StackTraceElement[] frames = stackTrace.frames();
		if (frames != null) {
			for (int i = 0; i < frames.length - commonFrames; i++) {
				StackTraceElement element = frames[i];
				if (!this.frameFilter.test(i, element)) {
					filteredFrames++;
					continue;
				}
				print.omittedFilteredFrames(filteredFrames);
				filteredFrames = 0;
				print.at(this.frameFormatter.apply(element));
			}
		}
		print.omittedFilteredFrames(filteredFrames);
		if (commonFrames != 0) {
			print.omittedCommonFrames(commonFrames);
		}
	}

	/**
	 * 返回会打印全部公共帧（而非 {@literal "... N more"} 省略消息）的新实例。
	 *
	 * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例
	 */
	public StandardStackTracePrinter withCommonFrames() {
		return withOption(Option.SHOW_COMMON_FRAMES);
	}

	/**
	 * 返回不打印 {@link Throwable#getSuppressed() 被抑制异常} 的新实例。
	 *
	 * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例
	 */
	public StandardStackTracePrinter withoutSuppressed() {
		return withOption(Option.HIDE_SUPPRESSED);
	}

	/**
	 * 返回会用省略号截断超过指定长度输出的新实例。
	 *
	 * @param maximumLength the maximum length that can be printed 可打印的最大长度
	 * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例
	 */
	public StandardStackTracePrinter withMaximumLength(int maximumLength) {
		Assert.isTrue(maximumLength > 0, "'maximumLength' must be positive");
		return new StandardStackTracePrinter(this.options, maximumLength, this.lineSeparator, this.filter,
				this.frameFilter, this.formatter, this.frameFormatter, this.frameHasher);
	}

	/**
	 * 返回过滤深度超过指定最大值的帧（含 cause 与 suppressed）的新实例。
	 *
	 * @param maximumThrowableDepth the maximum throwable depth 最大异常深度
	 * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例
	 */
	public StandardStackTracePrinter withMaximumThrowableDepth(int maximumThrowableDepth) {
		Assert.isTrue(maximumThrowableDepth > 0, "'maximumThrowableDepth' must be positive");
		return withFrameFilter((index, element) -> index < maximumThrowableDepth);
	}

	/**
	 * 返回仅包含匹配给定谓词的异常（不含 cause 与 suppressed 链过滤逻辑）的新实例。
	 *
	 * @param predicate the predicate used to filter the throwable 过滤异常的谓词
	 * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例
	 */
	public StandardStackTracePrinter withFilter(Predicate<Throwable> predicate) {
		Assert.notNull(predicate, "'predicate' must not be null");
		return new StandardStackTracePrinter(this.options, this.maximumLength, this.lineSeparator,
				this.filter.and(predicate), this.frameFilter, this.formatter, this.frameFormatter, this.frameHasher);
	}

	/**
	 * 返回仅包含匹配给定谓词的栈帧的新实例。
	 *
	 * @param predicate the predicate used to filter frames 过滤栈帧的谓词
	 * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例
	 */
	public StandardStackTracePrinter withFrameFilter(BiPredicate<Integer, StackTraceElement> predicate) {
		Assert.notNull(predicate, "'predicate' must not be null");
		return new StandardStackTracePrinter(this.options, this.maximumLength, this.lineSeparator, this.filter,
				this.frameFilter.and(predicate), this.formatter, this.frameFormatter, this.frameHasher);
	}

	/**
	 * 返回使用指定行分隔符打印堆栈的新实例。
	 *
	 * @param lineSeparator the line separator to use 行分隔符
	 * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例
	 */
	public StandardStackTracePrinter withLineSeparator(String lineSeparator) {
		Assert.notNull(lineSeparator, "'lineSeparator' must not be null");
		return new StandardStackTracePrinter(this.options, this.maximumLength, lineSeparator, this.filter,
				this.frameFilter, this.formatter, this.frameFormatter, this.frameHasher);
	}

	/**
	 * 返回使用指定格式化器生成异常字符串表示的新实例。
	 *
	 * @param formatter the formatter to use 格式化器
	 * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例
	 * @see #withLineSeparator(String)
	 */
	public StandardStackTracePrinter withFormatter(Function<Throwable, String> formatter) {
		Assert.notNull(formatter, "'formatter' must not be null");
		return new StandardStackTracePrinter(this.options, this.maximumLength, this.lineSeparator, this.filter,
				this.frameFilter, formatter, this.frameFormatter, this.frameHasher);
	}

	/**
	 * 返回使用指定格式化器生成栈帧字符串表示的新实例。
	 *
	 * @param frameFormatter the frame formatter to use 栈帧格式化器
	 * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例
	 * @see #withLineSeparator(String)
	 */
	public StandardStackTracePrinter withFrameFormatter(Function<StackTraceElement, String> frameFormatter) {
		Assert.notNull(frameFormatter, "'frameFormatter' must not be null");
		return new StandardStackTracePrinter(this.options, this.maximumLength, this.lineSeparator, this.filter,
				this.frameFilter, this.formatter, frameFormatter, this.frameHasher);
	}

	/**
	 * 返回为每个堆栈生成并打印哈希值的新实例。
	 *
	 * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例
	 */
	public StandardStackTracePrinter withHashes() {
		return withHashes(true);
	}

	/**
	 * 返回控制是否为每个堆栈生成并打印哈希值的新实例。
	 *
	 * @param hashes if hashes should be added 是否添加哈希
	 * @return a new {@link StandardStackTracePrinter} instance 新的 {@link StandardStackTracePrinter} 实例
	 */
	public StandardStackTracePrinter withHashes(boolean hashes) {
		return withHashes((!hashes) ? null : DEFAULT_FRAME_HASHER);
	}

	public StandardStackTracePrinter withHashes(@Nullable ToIntFunction<StackTraceElement> frameHasher) {
		return new StandardStackTracePrinter(this.options, this.maximumLength, this.lineSeparator, this.filter,
				this.frameFilter, this.formatter, this.frameFormatter, frameHasher);
	}

	private StandardStackTracePrinter withOption(Option option) {
		EnumSet<Option> options = EnumSet.copyOf(this.options);
		options.add(option);
		return new StandardStackTracePrinter(options, this.maximumLength, this.lineSeparator, this.filter,
				this.frameFilter, this.formatter, this.frameFormatter, this.frameHasher);
	}

	private boolean hasOption(Option option) {
		return this.options.contains(option);
	}

	/**
	 * 返回根异常最后打印的 {@link StandardStackTracePrinter}
	 * （与 {@link Throwable#printStackTrace()} 相同）。
	 *
	 * @return a {@link StandardStackTracePrinter} that prints the stack trace root last 根异常在最后的打印器
	 */
	public static StandardStackTracePrinter rootLast() {
		return new StandardStackTracePrinter(EnumSet.noneOf(Option.class), UNLIMITED, null, null, null, null, null,
				null);
	}

	/**
	 * 返回根异常最先打印的 {@link StandardStackTracePrinter}
	 * （与 {@link Throwable#printStackTrace()} 相反）。
	 *
	 * @return a {@link StandardStackTracePrinter} that prints the stack trace root first 根异常在前的打印器
	 */
	public static StandardStackTracePrinter rootFirst() {
		return new StandardStackTracePrinter(EnumSet.of(Option.ROOT_FIRST), UNLIMITED, null, null, null, null, null,
				null);
	}

	/**
	 * 本打印器支持的选项。
	 */
	private enum Option {

		ROOT_FIRST, SHOW_COMMON_FRAMES, HIDE_SUPPRESSED

	}

	/**
	 * 打印实际行输出。
	 */
	private record Print(String indent, String caption, Output output) {

		void circularReference(String hashPrefix, String throwable) throws IOException {
			this.output.println(this.indent, this.caption + "[CIRCULAR REFERENCE: " + hashPrefix + throwable + "]");
		}

		void thrown(String hashPrefix, String throwable) throws IOException {
			this.output.println(this.indent, this.caption + hashPrefix + throwable);
		}

		void at(String frame) throws IOException {
			this.output.println(this.indent, "\tat " + frame);
		}

		void omittedFilteredFrames(int filteredFrameCount) throws IOException {
			if (filteredFrameCount > 0) {
				this.output.println(this.indent, "\t... " + filteredFrameCount + " filtered");
			}
		}

		void omittedCommonFrames(int commonFrameCount) throws IOException {
			this.output.println(this.indent, "\t... " + commonFrameCount + " more");
		}

		Print withCausedByCaption(@Nullable StackTrace causedBy) {
			return withCaption(causedBy != null, "", "Caused by: ");
		}

		Print withWrappedByCaption(@Nullable StackTrace wrappedBy) {
			return withCaption(wrappedBy != null, "", "Wrapped by: ");
		}

		public Print withSuppressedCaption() {
			return withCaption(true, "\t", "Suppressed: ");
		}

		private Print withCaption(boolean test, String extraIndent, String caption) {
			return (test) ? new Print(this.indent + extraIndent, caption, this.output) : this;
		}

	}

	/**
	 * 逐行输出。
	 */
	private class Output {

		private static final String ELLIPSIS = "...";

		private final Appendable out;

		private int remaining;

		Output(Appendable out) {
			this.out = out;
			this.remaining = StandardStackTracePrinter.this.maximumLength - ELLIPSIS.length();
		}

		void println(String indent, String string) throws IOException {
			if (this.remaining > 0) {
				String line = indent + string + StandardStackTracePrinter.this.lineSeparator;
				if (line.length() > this.remaining) {
					line = line.substring(0, this.remaining) + ELLIPSIS;
				}
				this.out.append(line);
				this.remaining -= line.length();
			}
		}

	}

	/**
	 * 持有特定异常的堆栈信息，并缓存计算开销较大的数据。
	 */
	private static final class StackTrace {

		private final Throwable throwable;

		private final StackTraceElement @Nullable [] frames;

		private StackTrace @Nullable [] suppressed;

		private @Nullable StackTrace cause;

		private @Nullable Integer hash;

		private @Nullable String hashPrefix;

		private StackTrace(Throwable throwable) {
			this.throwable = throwable;
			this.frames = (throwable != null) ? throwable.getStackTrace() : null;
		}

		Throwable throwable() {
			return this.throwable;
		}

		StackTraceElement @Nullable [] frames() {
			return this.frames;
		}

		int commonFramesCount(@Nullable StackTrace other) {
			if (other == null || this.frames == null || other.frames == null) {
				return 0;
			}
			int index = this.frames.length - 1;
			int otherIndex = other.frames.length - 1;
			while (index >= 0 && otherIndex >= 0 && this.frames[index].equals(other.frames[otherIndex])) {
				index--;
				otherIndex--;
			}
			return this.frames.length - 1 - index;
		}

		StackTrace @Nullable [] suppressed() {
			if (this.suppressed == null && this.throwable != null) {
				this.suppressed = Arrays.stream(this.throwable.getSuppressed())
					.map(StackTrace::new)
					.toArray(StackTrace[]::new);
			}
			return this.suppressed;
		}

		@Nullable StackTrace cause() {
			if (this.cause == null && this.throwable != null) {
				Throwable cause = this.throwable.getCause();
				this.cause = (cause != null) ? new StackTrace(cause) : null;
			}
			return this.cause;
		}

		String hashPrefix(@Nullable ToIntFunction<StackTraceElement> frameHasher) {
			if (frameHasher == null || throwable() == null) {
				return "";
			}
			this.hashPrefix = (this.hashPrefix != null) ? this.hashPrefix
					: String.format("<#%08x> ", hash(new HashSet<>(), frameHasher));
			return this.hashPrefix;
		}

		private int hash(HashSet<Throwable> seen, ToIntFunction<StackTraceElement> frameHasher) {
			if (this.hash != null) {
				return this.hash;
			}
			int hash = 0;
			StackTrace cause = cause();
			if (cause != null && seen.add(cause.throwable())) {
				hash = cause.hash(seen, frameHasher);
			}
			hash = 31 * hash + throwable().getClass().getName().hashCode();
			if (frames() != null) {
				for (StackTraceElement frame : frames()) {
					hash = 31 * hash + frameHasher.applyAsInt(frame);
				}
			}
			this.hash = hash;
			return hash;
		}

	}

}
