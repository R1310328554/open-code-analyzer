#!/usr/bin/env python3
"""Chinese-annotate Disruptor 4.0.0 wave-6b examples batch [20:37]."""
from __future__ import annotations

import json
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "disruptor/4.0.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
BATCH_FILES = json.loads((VER / "_reports/class-queue/batch.json").read_text())["files"][20:37]

COMMON_JAVADOC_REPLACEMENTS: list[tuple[str, str]] = [
    (
        "UniCast a series of items between 1 publisher and 1 event processor using the EventTranslator API",
        "单播：1 个发布者与 1 个事件处理器之间通过 EventTranslator API 传递一系列事件。",
    ),
    ("track to prevent wrap", "跟踪序号以防环绕"),
    ("Disruptor:", "Disruptor："),
    ("P1  - Publisher 1", "P1  - 发布者 1"),
    ("RB  - RingBuffer", "RB  - 环形缓冲区"),
    ("SB  - SequenceBarrier", "SB  - 序号屏障"),
    ("EP1 - EventProcessor 1", "EP1 - 事件处理器 1"),
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "OneToOneTranslatorThroughputTest.java": [
        (
            "        for (long l = 0; l < ITERATIONS; l++)\n        {\n            value.set(l);\n            rb.publishEvent(Translator.INSTANCE, value);\n        }",
            "        for (long l = 0; l < ITERATIONS; l++)\n        {\n            value.set(l);\n            // 步骤：通过 EventTranslatorOneArg 发布事件\n            rb.publishEvent(Translator.INSTANCE, value);\n        }",
        ),
        (
            "        @Override\n        public void translateTo(final ValueEvent event, final long sequence, final MutableLong arg0)\n        {\n            event.setValue(arg0.get());\n        }",
            "        @Override\n        public void translateTo(final ValueEvent event, final long sequence, final MutableLong arg0)\n        {\n            // 步骤：从 MutableLong 参数写入事件值\n            event.setValue(arg0.get());\n        }",
        ),
    ],
    "DynamicallyAddHandler.java": [
        (
            "package com.lmax.disruptor.examples;",
            "/**\n * 运行时动态添加与移除 BatchEventProcessor 的示例。\n */\n\npackage com.lmax.disruptor.examples;",
        ),
        (
            "    private static class DynamicHandler implements EventHandler<StubEvent>\n    {",
            "    /** 带关闭 latch 的动态处理器，onShutdown 时通知等待方。 */\n    private static class DynamicHandler implements EventHandler<StubEvent>\n    {",
        ),
        (
            "        // Build a disruptor and start it.",
            "        // 步骤：构建 Disruptor 并启动，获取 RingBuffer",
        ),
        (
            "        // Construct 2 batch event processors.",
            "        // 步骤：手动构造两个 BatchEventProcessor",
        ),
        (
            "        // Dynamically add both sequences to the ring buffer",
            "        // 步骤：将两个处理器的序号动态注册为门控序号",
        ),
        (
            "        // Start the new batch processors.",
            "        // 步骤：在线程池中启动处理器",
        ),
        (
            "        // Remove a processor.\n\n        // Stop the processor",
            "        // 步骤：移除其中一个处理器\n\n        // 停止 processor2",
        ),
        (
            "        // Wait for shutdown the complete",
            "        // 等待 onShutdown 完成",
        ),
        (
            "        // Remove the gating sequence from the ring buffer",
            "        // 从 RingBuffer 移除对应门控序号",
        ),
    ],
    "EarlyReleaseHandler.java": [
        (
            "package com.lmax.disruptor.examples;",
            "/**\n * 提前释放序号示例：在逻辑工作块完成时手动推进 sequenceCallback。\n */\n\npackage com.lmax.disruptor.examples;",
        ),
        (
            "        boolean logicalChunkOfWorkComplete = isLogicalChunkOfWorkComplete();\n        if (logicalChunkOfWorkComplete)\n        {\n            sequenceCallback.set(sequence);\n        }",
            "        boolean logicalChunkOfWorkComplete = isLogicalChunkOfWorkComplete();\n        if (logicalChunkOfWorkComplete)\n        {\n            // 步骤：逻辑块完成时提前释放已处理序号\n            sequenceCallback.set(sequence);\n        }",
        ),
        (
            "        // Ret true or false based on whatever criteria is required for the smaller\n        // chunk.  If this is doing I/O, it may be after flushing/syncing to disk\n        // or at the end of DB batch+commit.\n        // Or it could simply be working off a smaller batch size.",
            "        // 按更小工作块的完成条件返回 true/false，例如刷盘、DB 提交或自定义批大小。",
        ),
        (
            "        // Do processing",
            "        // 处理事件",
        ),
    ],
    "HandleExceptionOnTranslate.java": [
        (
            "package com.lmax.disruptor.examples;",
            "/**\n * 翻译阶段抛出异常时的处理示例：publishEvent 捕获异常，handler 识别丢弃事件。\n */\n\npackage com.lmax.disruptor.examples;",
        ),
        (
            "    private static class MyHandler implements EventHandler<LongEvent>\n    {",
            "    /** 识别 NO_VALUE_SPECIFIED 标记的已丢弃事件。 */\n    private static class MyHandler implements EventHandler<LongEvent>\n    {",
        ),
        (
            "            if (sequence % 3 == 0)\n            {\n                throw new RuntimeException(\"Skipping\");\n            }",
            "            // 步骤：每第三条序号在翻译阶段抛异常，事件被标记为丢弃\n            if (sequence % 3 == 0)\n            {\n                throw new RuntimeException(\"Skipping\");\n            }",
        ),
        (
            "                // Skipping",
            "                // 翻译失败，跳过本次发布",
        ),
    ],
    "KeyedBatching.java": [
        (
            "package com.lmax.disruptor.examples;",
            "/**\n * 按 key 分组批处理示例：key 变化或达到批大小时刷新批次。\n */\n\npackage com.lmax.disruptor.examples;",
        ),
        (
            "        if (!batch.isEmpty() && event.key != key)\n        {\n            processBatch(batch);\n        }",
            "        // 步骤：key 变化时先处理已有批次\n        if (!batch.isEmpty() && event.key != key)\n        {\n            processBatch(batch);\n        }",
        ),
        (
            "        if (endOfBatch || batch.size() >= MAX_BATCH_SIZE)\n        {\n            processBatch(batch);\n        }",
            "        // 步骤：批末或达到上限时刷新\n        if (endOfBatch || batch.size() >= MAX_BATCH_SIZE)\n        {\n            processBatch(batch);\n        }",
        ),
        (
            "        // do work.",
            "        // 处理当前批次",
        ),
        (
            "    public static class KeyedEvent\n    {",
            "    /** 带 key 与 data 的分组事件。 */\n    public static class KeyedEvent\n    {",
        ),
    ],
    "MultiProducerWithTranslator.java": [
        (
            "package com.lmax.disruptor.examples;",
            "/**\n * 多生产者通过 EventTranslatorThreeArg 发布三参数事件的示例。\n */\n\npackage com.lmax.disruptor.examples;",
        ),
        (
            "    private static class ObjectBox\n    {",
            "    /** 承载 message、transportable 与 streamName 的事件槽位。 */\n    private static class ObjectBox\n    {",
        ),
        (
            "    public static class Publisher implements EventTranslatorThreeArg<ObjectBox, IMessage, ITransportable, String>\n    {",
            "    /** 三参数 EventTranslator，将发布参数写入 ObjectBox。 */\n    public static class Publisher implements EventTranslatorThreeArg<ObjectBox, IMessage, ITransportable, String>\n    {",
        ),
        (
            "    public static class Consumer implements EventHandler<ObjectBox>\n    {",
            "    /** 消费 ObjectBox 事件的占位处理器。 */\n    public static class Consumer implements EventHandler<ObjectBox>\n    {",
        ),
    ],
    "NamedEventHandler.java": [
        (
            "package com.lmax.disruptor.examples;",
            "/**\n * 在 onStart/onShutdown 中设置与恢复线程名的 EventHandler 包装器。\n */\n\npackage com.lmax.disruptor.examples;",
        ),
        (
            "        oldName = currentThread.getName();\n        currentThread.setName(name);",
            "        // 步骤：启动时保存旧名并设置为可读名称\n        oldName = currentThread.getName();\n        currentThread.setName(name);",
        ),
        (
            "        Thread.currentThread().setName(oldName);",
            "        // 步骤：关闭时恢复原始线程名\n        Thread.currentThread().setName(oldName);",
        ),
    ],
    "Pipeliner.java": [
        (
            "package com.lmax.disruptor.examples;",
            "/**\n * 并行分片处理再汇聚的流水线示例：三个 ParallelHandler 后接 JoiningHandler。\n */\n\npackage com.lmax.disruptor.examples;",
        ),
        (
            "            if (sequence % totalHandlers == ordinal)\n            {\n                event.result = Long.toString(event.input);\n            }",
            "            // 步骤：按序号取模分派到对应并行处理器\n            if (sequence % totalHandlers == ordinal)\n            {\n                event.result = Long.toString(event.input);\n            }",
        ),
        (
            "            if (event.input != lastEvent + 1 || event.result == null)\n            {\n                System.out.println(\"Error: \" + event);\n            }",
            "            // 步骤：校验顺序连续且各并行分支均已写入 result\n            if (event.input != lastEvent + 1 || event.result == null)\n            {\n                System.out.println(\"Error: \" + event);\n            }",
        ),
        (
            "    private static class PipelinerEvent\n    {",
            "    /** 流水线事件：input 为输入，result 为并行阶段输出。 */\n    private static class PipelinerEvent\n    {",
        ),
    ],
    "PullWithBatchedPoller.java": [
        (
            "/**\n * Alternative usage of EventPoller, here we wrap it around BatchedEventPoller\n * to achieve Disruptor's batching. this speeds up the polling feature\n */",
            "/**\n * EventPoller 的批量拉取用法：本地缓冲一批事件以加速轮询消费。\n */",
        ),
        (
            "        // Value could be null if no events are available.",
            "        // 无可用事件时 value 可能为 null",
        ),
        (
            "            // Process value.",
            "            // 处理取出的值",
        ),
        (
            "                return polledData.pollMessage(); // we just fetch from our local",
            "                return polledData.pollMessage(); // 步骤：优先从本地缓冲取",
        ),
        (
            "            loadNextValues(poller, polledData); // we try to load from the ring",
            "            loadNextValues(poller, polledData); // 步骤：本地为空时从 RingBuffer 批量加载",
        ),
        (
            "                // Copy the data out here. In this case we have a single reference\n                // object, so the pass by\n                // reference is sufficient. But if we were reusing a byte array,\n                // then we\n                // would need to copy\n                // the actual contents.",
            "                // 此处拷贝数据；单引用对象传引用即可，复用 byte[] 时需深拷贝内容。",
        ),
    ],
    "PullWithPoller.java": [
        (
            "package com.lmax.disruptor.examples;",
            "/**\n * 使用 EventPoller 单条拉取事件的示例。\n */\n\npackage com.lmax.disruptor.examples;",
        ),
        (
            "            // Copy the data out here.  In this case we have a single reference object, so the pass by\n            // reference is sufficient.  But if we were reusing a byte array, then we would need to copy\n            // the actual contents.",
            "            // 此处拷贝数据；单引用对象传引用即可，复用 byte[] 时需深拷贝内容。",
        ),
        (
            "        // Value could be null if no events are available.",
            "        // 无可用事件时 value 可能为 null",
        ),
        (
            "            // Process value.",
            "            // 处理取出的值",
        ),
        (
            "                    // Return false so that only one event is processed at a time.",
            "                    // 返回 false 表示每次 poll 只处理一条事件",
        ),
    ],
    "SequentialThreeConsumers.java": [
        (
            "package com.lmax.disruptor.examples;",
            "/**\n * 三个顺序消费者链式传递字段的示例：a → b → c → d。\n */\n\npackage com.lmax.disruptor.examples;",
        ),
        (
            "    private static class MyEvent\n    {",
            "    /** 在流水线各阶段间传递的四个字段槽位。 */\n    private static class MyEvent\n    {",
        ),
        (
            "        disruptor.handleEventsWith((event, sequence, endOfBatch) -> event.b = event.a)\n                .then((event, sequence, endOfBatch) -> event.c = event.b)\n                .then((event, sequence, endOfBatch) -> event.d = event.c);",
            "        // 步骤：三阶段顺序消费者，字段逐级传递\n        disruptor.handleEventsWith((event, sequence, endOfBatch) -> event.b = event.a)\n                .then((event, sequence, endOfBatch) -> event.c = event.b)\n                .then((event, sequence, endOfBatch) -> event.d = event.c);",
        ),
    ],
    "ShutdownOnError.java": [
        (
            "package com.lmax.disruptor.examples;",
            "/**\n * 事件处理异常时通过 ExceptionHandler 决定是否终止 Disruptor 的示例。\n */\n\npackage com.lmax.disruptor.examples;",
        ),
        (
            "            // do work, if a failure occurs throw exception.",
            "            // 执行业务逻辑，失败时抛出异常",
        ),
        (
            "            // Do what is appropriate here.",
            "            // 按业务规则判定异常是否致命",
        ),
        (
            "            if (execeptionIsFatal(ex))\n            {\n                throw new RuntimeException(ex);\n            }",
            "            // 步骤：致命异常重新抛出以触发 Disruptor 关闭\n            if (execeptionIsFatal(ex))\n            {\n                throw new RuntimeException(ex);\n            }",
        ),
    ],
    "ThreeToOneDisruptor.java": [
        (
            "package com.lmax.disruptor.examples;",
            "/**\n * 三并行转换后汇聚到单一 CollatingHandler 的示例。\n */\n\npackage com.lmax.disruptor.examples;",
        ),
        (
            "    public static class DataEvent\n    {",
            "    /** 输入与一个固定长度 output 数组的事件载体。 */\n    public static class DataEvent\n    {",
        ),
        (
            "    public static class TransformingHandler implements EventHandler<DataEvent>\n    {",
            "    /** 并行转换处理器，写入 output 的指定索引。 */\n    public static class TransformingHandler implements EventHandler<DataEvent>\n    {",
        ),
        (
            "            // Do Stuff.\n            event.output[outputIndex] = doSomething(event.input);",
            "            // 步骤：将 input 转换后写入 output[outputIndex]\n            event.output[outputIndex] = doSomething(event.input);",
        ),
        (
            "            // Do required transformation here....",
            "            // 在此执行具体转换",
        ),
        (
            "    public static class CollatingHandler implements EventHandler<DataEvent>\n    {",
            "    /** 汇聚处理器：三路 output 齐备后执行 collate。 */\n    public static class CollatingHandler implements EventHandler<DataEvent>\n    {",
        ),
        (
            "            // Do required collation here....",
            "            // 在此执行汇聚逻辑",
        ),
    ],
    "WaitForProcessing.java": [
        (
            "package com.lmax.disruptor.examples;",
            "/**\n * 等待特定消费者或 RingBuffer 空闲的处理同步示例。\n */\n\npackage com.lmax.disruptor.examples;",
        ),
        (
            "    public static class Consumer implements EventHandler<LongEvent>\n    {",
            "    /** 占位消费者，用于演示序号等待。 */\n    public static class Consumer implements EventHandler<LongEvent>\n    {",
        ),
        (
            "        EventTranslator<LongEvent> translator = (event, sequence) -> event.set(sequence - 4);",
            "        // 步骤：发布一条带偏移赋值的事件\n        EventTranslator<LongEvent> translator = (event, sequence) -> event.set(sequence - 4);",
        ),
        (
            "            // Wait for priocessing...",
            "            // 等待 RingBuffer 中事件全部被消费",
        ),
        (
            "        do\n        {\n            lastPublishedValue = ringBuffer.getCursor();\n            sequenceValueFor = disruptor.getSequenceValueFor(lastConsumer);\n        }\n        while (sequenceValueFor < lastPublishedValue);",
            "        // 步骤：轮询直到 lastConsumer 追上已发布游标\n        do\n        {\n            lastPublishedValue = ringBuffer.getCursor();\n            sequenceValueFor = disruptor.getSequenceValueFor(lastConsumer);\n        }\n        while (sequenceValueFor < lastPublishedValue);",
        ),
    ],
    "WaitForShutdown.java": [
        (
            "package com.lmax.disruptor.examples;",
            "/**\n * 带超时 shutdown 并通过 CountDownLatch 等待 handler 关闭完成的示例。\n */\n\npackage com.lmax.disruptor.examples;",
        ),
        (
            "    private static class Handler implements EventHandler<LongEvent>\n    {",
            "    /** 在 onShutdown 中递减 latch 的处理器。 */\n    private static class Handler implements EventHandler<LongEvent>\n    {",
        ),
        (
            "        disruptor.shutdown(10, TimeUnit.SECONDS);",
            "        // 步骤：带超时请求关闭 Disruptor\n        disruptor.shutdown(10, TimeUnit.SECONDS);",
        ),
        (
            "        shutdownLatch.await();",
            "        // 等待两个 handler 均完成 onShutdown\n        shutdownLatch.await();",
        ),
    ],
    "LongEvent.java": [
        (
            "package com.lmax.disruptor.examples.longevent;",
            "/**\n * 长整型事件载体示例。\n */\n\npackage com.lmax.disruptor.examples.longevent;",
        ),
        (
            "    public void set(long value)\n    {",
            "    /** 设置事件值。 */\n    public void set(long value)\n    {",
        ),
    ],
    "LongEventFactory.java": [
        (
            "package com.lmax.disruptor.examples.longevent;",
            "/**\n * LongEvent 的 EventFactory 实现。\n */\n\npackage com.lmax.disruptor.examples.longevent;",
        ),
        (
            "    @Override\n    public LongEvent newInstance()\n    {",
            "    /** 预分配 RingBuffer 槽位时创建新的 LongEvent。 */\n    @Override\n    public LongEvent newInstance()\n    {",
        ),
    ],
}


def apply_common_javadoc(text: str) -> str:
    for old, new in COMMON_JAVADOC_REPLACEMENTS:
        text = text.replace(old, new)
    return text


def annotate_file(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    text = apply_common_javadoc(text)
    name = Path(rel).name
    for old, new in FILE_REPLACEMENTS.get(name, []):
        if old not in text:
            raise ValueError(f"Pattern not found in {rel}:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    dst.write_text(text, encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
