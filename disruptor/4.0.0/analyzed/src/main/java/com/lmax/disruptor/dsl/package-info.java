/**
 * 围绕环形缓冲区配置 Disruptor 模式的 DSL 风格 API
 *
 * <h2>示例代码</h2>
 * <pre>{@code
 * // 指定环形缓冲区大小，须为 2 的幂
 *  int bufferSize = 1024;
 *
 *  // 构造 Disruptor
 *  Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, DaemonThreadFactory.INSTANCE);
 *
 *  // 连接事件处理器
 *  disruptor.handleEventsWith((event, sequence, endOfBatch) -> System.out.println("Event: " + event));
 *
 *  // 启动 Disruptor
 *  disruptor.start();
 *
 *  // 获取环形缓冲区用于发布
 *  RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
 *
 *  ByteBuffer bb = ByteBuffer.allocate(8);
 *  for (long l = 0; true; l++)
 *  {
 *      bb.putLong(0, l);
 *      ringBuffer.publishEvent((event, sequence, buffer) -> event.set(buffer.getLong(0)), bb);
 *      Thread.sleep(1000);
 *  }
 * }</pre>
 */
package com.lmax.disruptor.dsl;