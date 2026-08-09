#!/usr/bin/env python3
"""Chinese-annotate Disruptor 4.0.0 perftest batch [20:40]."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "disruptor/4.0.0"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
BATCH_FILES = json.loads((VER / "_reports/class-queue/batch.json").read_text())["files"][20:40]

COMMON_JAVADOC_REPLACEMENTS: list[tuple[str, str]] = [
    (
        "UniCast a series of items between 1 publisher and 1 event processor.",
        "单播：1 个发布者与 1 个事件处理器之间传递一系列事件。",
    ),
    (
        "MultiCast a series of items between 1 publisher and 3 event processors.",
        "多播：1 个发布者与 3 个事件处理器之间传递一系列事件。",
    ),
    (
        "Produce an event replicated to two event processors and fold back to a single third event processor.",
        "将事件复制到两个事件处理器，再汇聚到第三个事件处理器。",
    ),
    (
        "Pipeline a series of stages from a publisher to ultimate event processor.\n * Each event processor depends on the output of the event processor.",
        "流水线：从发布者经多个阶段到达最终事件处理器，每个处理器依赖前一级的输出。",
    ),
    (
        "Sequence a series of events from multiple publishers going to one event processor.",
        "多发布者向单一事件处理器顺序发送一系列事件。",
    ),
    (
        "Ping pongs between 2 event handlers and measures the latency of\n * a round trip.",
        "两个事件处理器之间乒乓往返，测量单次往返延迟。",
    ),
    ("Queue Based:", "基于队列："),
    ("Disruptor:", "Disruptor："),
    ("track to prevent wrap", "跟踪序号以防环绕"),
    ("P1  - Publisher 1", "P1  - 发布者 1"),
    ("P2  - Publisher 2", "P2  - 发布者 2"),
    ("P3  - Publisher 3", "P3  - 发布者 3"),
    ("P1 - Publisher 1", "P1 - 发布者 1"),
    ("P2 - Publisher 2", "P2 - 发布者 2"),
    ("P3 - Publisher 3", "P3 - 发布者 3"),
    ("Q1  - Queue 1", "Q1  - 队列 1"),
    ("Q2  - Queue 2", "Q2  - 队列 2"),
    ("Q3  - Queue 3", "Q3  - 队列 3"),
    ("Q4  - Queue 4", "Q4  - 队列 4"),
    ("EP1 - EventProcessor 1", "EP1 - 事件处理器 1"),
    ("EP2 - EventProcessor 2", "EP2 - 事件处理器 2"),
    ("EP3 - EventProcessor 3", "EP3 - 事件处理器 3"),
    ("EP1 - EventProcessor 1", "EP1 - 事件处理器 1"),
    ("RB  - RingBuffer", "RB  - 环形缓冲区"),
    ("RB - RingBuffer", "RB - 环形缓冲区"),
    ("SB  - SequenceBarrier", "SB  - 序号屏障"),
    ("SB - SequenceBarrier", "SB - 序号屏障"),
    ("SB1 - SequenceBarrier 1", "SB1 - 序号屏障 1"),
    ("SB2 - SequenceBarrier 2", "SB2 - 序号屏障 2"),
    ("SB3 - SequenceBarrier 3", "SB3 - 序号屏障 3"),
    ("EP - EventProcessor", "EP - 事件处理器"),
    (
        "<p>Note: <b>This test is only useful on a system using an invariant TSC in user space from the System.nanoTime() call.</b>",
        "<p>注意：<b>本测试仅在用户态 System.nanoTime() 使用不变 TSC 的系统上有意义。</b>",
    ),
    ("P1 - QueuePinger", "P1 - 队列 Ping 端"),
    ("P2 - QueuePonger", "P2 - 队列 Pong 端"),
    ("Q1 - PingQueue", "Q1 - Ping 队列"),
    ("Q2 - PongQueue", "Q2 - Pong 队列"),
    ("EP1 - Pinger", "EP1 - Ping 端"),
    ("EP2 - Ponger", "EP2 - Pong 端"),
    ("RB1 - PingBuffer", "RB1 - Ping 环形缓冲区"),
    ("SB1 - PingBarrier", "SB1 - Ping 序号屏障"),
    ("RB2 - PongBuffer", "RB2 - Pong 环形缓冲区"),
    ("SB2 - PongBarrier", "SB2 - Pong 序号屏障"),
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "EventCountingQueueProcessor.java": [
        (
            "public final class EventCountingQueueProcessor implements Runnable",
            "/**\n * 基于阻塞队列的事件计数处理器：从队列取事件并对指定计数器加一。\n */\npublic final class EventCountingQueueProcessor implements Runnable",
        ),
        (
            "    public void halt()\n    {\n        running = false;\n    }",
            "    /** 请求停止消费循环。 */\n    public void halt()\n    {\n        running = false;\n    }",
        ),
        (
            "                blockingQueue.take();\n                counters[index].set(counters[index].get() + 1L);",
            "                blockingQueue.take();\n                // 步骤：每消费一个事件，对应槽位计数器加一\n                counters[index].set(counters[index].get() + 1L);",
        ),
    ],
    "FizzBuzzEvent.java": [
        (
            "public final class FizzBuzzEvent\n{",
            "/**\n * FizzBuzz 性能测试事件：携带数值及 fizz/buzz 标记。\n */\npublic final class FizzBuzzEvent\n{",
        ),
        (
            "    public void setValue(final long value)\n    {\n        fizz = false;\n        buzz = false;\n        this.value = value;\n    }",
            "    /** 设置新数值并重置 fizz/buzz 标记。 */\n    public void setValue(final long value)\n    {\n        fizz = false;\n        buzz = false;\n        this.value = value;\n    }",
        ),
        (
            "    public static final EventFactory<FizzBuzzEvent> EVENT_FACTORY = () -> new FizzBuzzEvent();",
            "    /** RingBuffer 预分配事件工厂。 */\n    public static final EventFactory<FizzBuzzEvent> EVENT_FACTORY = () -> new FizzBuzzEvent();",
        ),
    ],
    "FizzBuzzEventHandler.java": [
        (
            "public final class FizzBuzzEventHandler implements EventHandler<FizzBuzzEvent>\n{",
            "/**\n * FizzBuzz 流水线事件处理器：按步骤判定整除 3/5 或统计 fizz+buzz。\n */\npublic final class FizzBuzzEventHandler implements EventHandler<FizzBuzzEvent>\n{",
        ),
        (
            "    public void reset(final CountDownLatch latch, final long expectedCount)\n    {",
            "    /** 重置计数器并绑定完成 latch（在 expectedCount 序号处触发）。 */\n    public void reset(final CountDownLatch latch, final long expectedCount)\n    {",
        ),
        (
            "            case FIZZ:\n                if (0 == (event.getValue() % 3))\n                {\n                    event.setFizz(true);\n                }",
            "            case FIZZ:\n                // 步骤：能被 3 整除则标记 fizz\n                if (0 == (event.getValue() % 3))\n                {\n                    event.setFizz(true);\n                }",
        ),
        (
            "            case BUZZ:\n                if (0 == (event.getValue() % 5))\n                {\n                    event.setBuzz(true);\n                }",
            "            case BUZZ:\n                // 步骤：能被 5 整除则标记 buzz\n                if (0 == (event.getValue() % 5))\n                {\n                    event.setBuzz(true);\n                }",
        ),
        (
            "            case FIZZ_BUZZ:\n                if (event.isFizz() && event.isBuzz())\n                {\n                    fizzBuzzCounter.set(fizzBuzzCounter.get() + 1L);\n                }",
            "            case FIZZ_BUZZ:\n                // 步骤：同时满足 fizz 与 buzz 时累加计数\n                if (event.isFizz() && event.isBuzz())\n                {\n                    fizzBuzzCounter.set(fizzBuzzCounter.get() + 1L);\n                }",
        ),
    ],
    "FizzBuzzQueueProcessor.java": [
        (
            "public final class FizzBuzzQueueProcessor implements Runnable\n{",
            "/**\n * 基于阻塞队列的 FizzBuzz 处理器：FIZZ/BUZZ 分支判定，FIZZ_BUZZ 分支汇聚统计。\n */\npublic final class FizzBuzzQueueProcessor implements Runnable\n{",
        ),
        (
            "    public void reset(final CountDownLatch latch)\n    {",
            "    /** 重置内部计数与完成 latch。 */\n    public void reset(final CountDownLatch latch)\n    {",
        ),
        (
            "    public void halt()\n    {\n        running = false;\n    }",
            "    /** 请求停止消费循环。 */\n    public void halt()\n    {\n        running = false;\n    }",
        ),
        (
            "                        if (fizz && buzz)\n                        {\n                            ++fizzBuzzCounter;\n                        }",
            "                        // 步骤：两路均为 true 时计入 fizzBuzz 命中\n                        if (fizz && buzz)\n                        {\n                            ++fizzBuzzCounter;\n                        }",
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
            raise ValueError(f"Pattern not found in {rel}:\n{old[:100]}...")
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
