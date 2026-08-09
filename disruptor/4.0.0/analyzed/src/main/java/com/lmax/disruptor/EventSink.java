package com.lmax.disruptor;

/**
 * {@link RingBuffer} 的写入接口。
 *
 * @param <E> 事件类型
 */
public interface EventSink<E>
{
    /**
     * 向环形缓冲区发布事件：申领下一序号、取得当前（未初始化）事件槽位，
     * 经翻译器填充后发布已申领的序号。
     *
     * @param translator 用户指定的事件翻译器
     */
    void publishEvent(EventTranslator<E> translator);

    /**
     * 尝试向环形缓冲区发布事件：申领下一序号、取得当前（未初始化）事件槽位，
     * 经翻译器填充后发布已申领的序号。若容量不足则返回 false。
     *
     * @param translator 用户指定的事件翻译器
     * @return 发布成功返回 true，容量不足返回 false
     */
    boolean tryPublishEvent(EventTranslator<E> translator);

    /**
     * 支持传入一个用户参数。
     *
     * @param <A> 用户参数类型
     * @param translator 用户指定的事件翻译器
     * @param arg0       用户参数
     * @see #publishEvent(EventTranslator)
     */
    <A> void publishEvent(EventTranslatorOneArg<E, A> translator, A arg0);

    /**
     * 支持传入一个用户参数。
     *
     * @param <A> 用户参数类型
     * @param translator 用户指定的事件翻译器
     * @param arg0       用户参数
     * @return 发布成功返回 true，容量不足返回 false
     * @see #tryPublishEvent(EventTranslator)
     */
    <A> boolean tryPublishEvent(EventTranslatorOneArg<E, A> translator, A arg0);

    /**
     * 支持传入两个用户参数。
     *
     * @param <A> 第一个用户参数类型
     * @param <B> 第二个用户参数类型
     * @param translator 用户指定的事件翻译器
     * @param arg0       第一个用户参数
     * @param arg1       第二个用户参数
     * @see #publishEvent(EventTranslator)
     */
    <A, B> void publishEvent(EventTranslatorTwoArg<E, A, B> translator, A arg0, B arg1);

    /**
     * 支持传入两个用户参数。
     *
     * @param <A> 第一个用户参数类型
     * @param <B> 第二个用户参数类型
     * @param translator 用户指定的事件翻译器
     * @param arg0       第一个用户参数
     * @param arg1       第二个用户参数
     * @return 发布成功返回 true，容量不足返回 false
     * @see #tryPublishEvent(EventTranslator)
     */
    <A, B> boolean tryPublishEvent(EventTranslatorTwoArg<E, A, B> translator, A arg0, B arg1);

    /**
     * 支持传入三个用户参数。
     *
     * @param <A> 第一个用户参数类型
     * @param <B> 第二个用户参数类型
     * @param <C> 第三个用户参数类型
     * @param translator 用户指定的事件翻译器
     * @param arg0       第一个用户参数
     * @param arg1       第二个用户参数
     * @param arg2       第三个用户参数
     * @see #publishEvent(EventTranslator)
     */
    <A, B, C> void publishEvent(EventTranslatorThreeArg<E, A, B, C> translator, A arg0, B arg1, C arg2);

    /**
     * 支持传入三个用户参数。
     *
     * @param <A> 第一个用户参数类型
     * @param <B> 第二个用户参数类型
     * @param <C> 第三个用户参数类型
     * @param translator 用户指定的事件翻译器
     * @param arg0       第一个用户参数
     * @param arg1       第二个用户参数
     * @param arg2       第三个用户参数
     * @return 发布成功返回 true，容量不足返回 false
     * @see #publishEvent(EventTranslator)
     */
    <A, B, C> boolean tryPublishEvent(EventTranslatorThreeArg<E, A, B, C> translator, A arg0, B arg1, C arg2);

    /**
     * 支持传入可变数量的用户参数。
     *
     * @param translator 用户指定的事件翻译器
     * @param args       用户参数
     * @see #publishEvent(EventTranslator)
     */
    void publishEvent(EventTranslatorVararg<E> translator, Object... args);

    /**
     * 支持传入可变数量的用户参数。
     *
     * @param translator 用户指定的事件翻译器
     * @param args       用户参数
     * @return 发布成功返回 true，容量不足返回 false
     * @see #publishEvent(EventTranslator)
     */
    boolean tryPublishEvent(EventTranslatorVararg<E> translator, Object... args);

    /**
     * <p>向环形缓冲区批量发布事件：申领下一序号区间、取得当前（未初始化）事件槽位，
     * 经翻译器填充后发布已申领的序号。</p>
     *
     * <p>本调用要求写入环形缓冲区的数据为字段（显式声明或匿名捕获），
     * 因此每个待插入值都需要一个翻译器实例。</p>
     *
     * @param translators 每个事件对应的用户翻译器
     */
    void publishEvents(EventTranslator<E>[] translators);

    /**
     * <p>Publishes multiple events to the ring buffer.  It handles
     * claiming the next sequence, getting the current (uninitialised)
     * event from the ring buffer and publishing the claimed sequence
     * after translation.</p>
     *
     * <p>With this call the data that is to be inserted into the ring
     * buffer will be a field (either explicitly or captured anonymously),
     * therefore this call will require an instance of the translator
     * for each value that is to be inserted into the ring buffer.</p>
     *
     * @param translators   每个事件对应的用户翻译器
     * @param batchStartsAt 批次在数组中的起始下标
     * @param batchSize     批次实际大小
     */
    void publishEvents(EventTranslator<E>[] translators, int batchStartsAt, int batchSize);

    /**
     * 尝试向环形缓冲区批量发布事件：申领下一序号区间、取得当前（未初始化）事件槽位，
     * 经翻译器填充后发布已申领的序号。若容量不足则返回 false。
     *
     * @param translators 每个事件对应的用户翻译器
     * @return 全部发布成功返回 true，容量不足返回 false
     */
    boolean tryPublishEvents(EventTranslator<E>[] translators);

    /**
     * Attempts to publish multiple events to the ring buffer.  It handles
     * claiming the next sequence, getting the current (uninitialised)
     * event from the ring buffer and publishing the claimed sequence
     * after translation.  Will return false if specified capacity
     * was not available.
     *
     * @param translators   每个事件对应的用户翻译器
     * @param batchStartsAt 批次在数组中的起始下标
     * @param batchSize     批次实际大小
     * @return 全部发布成功返回 true，容量不足返回 false
     */
    boolean tryPublishEvents(EventTranslator<E>[] translators, int batchStartsAt, int batchSize);

    /**
     * 每个事件支持传入一个用户参数。
     *
     * @param <A> 用户参数类型
     * @param translator 用户指定的事件翻译器
     * @param arg0       用户参数数组，每个元素对应一个事件
     * @see #publishEvents(com.lmax.disruptor.EventTranslator[])
     */
    <A> void publishEvents(EventTranslatorOneArg<E, A> translator, A[] arg0);

    /**
     * Allows one user supplied argument per event.
     *
     * @param <A> Class of the user supplied argument
     * @param translator    每个事件对应的用户翻译器
     * @param batchStartsAt 批次在数组中的起始下标
     * @param batchSize     批次实际大小
     * @param arg0          用户参数数组，每个元素对应一个事件
     * @see #publishEvents(EventTranslator[])
     */
    <A> void publishEvents(EventTranslatorOneArg<E, A> translator, int batchStartsAt, int batchSize, A[] arg0);

    /**
     * Allows one user supplied argument.
     *
     * @param <A> Class of the user supplied argument
     * @param translator 每个事件对应的用户翻译器
     * @param arg0       用户参数数组，每个元素对应一个事件
     * @return 全部发布成功返回 true，容量不足返回 false
     * @see #tryPublishEvents(com.lmax.disruptor.EventTranslator[])
     */
    <A> boolean tryPublishEvents(EventTranslatorOneArg<E, A> translator, A[] arg0);

    /**
     * Allows one user supplied argument.
     *
     * @param <A> Class of the user supplied argument
     * @param translator    The user specified translation for each event
     * @param batchStartsAt The first element of the array which is within the batch.
     * @param batchSize     The actual size of the batch
     * @param arg0          An array of user supplied arguments, one element per event.
     * @return true if the value was published, false if there was insufficient
     * capacity.
     * @see #tryPublishEvents(EventTranslator[])
     */
    <A> boolean tryPublishEvents(EventTranslatorOneArg<E, A> translator, int batchStartsAt, int batchSize, A[] arg0);

    /**
     * 每个事件支持传入两个用户参数。
     *
     * @param <A> 第一个用户参数类型
     * @param <B> 第二个用户参数类型
     * @param translator 用户指定的事件翻译器
     * @param arg0       用户参数数组，每个元素对应一个事件
     * @param arg1       用户参数数组，每个元素对应一个事件
     * @see #publishEvents(com.lmax.disruptor.EventTranslator[])
     */
    <A, B> void publishEvents(EventTranslatorTwoArg<E, A, B> translator, A[] arg0, B[] arg1);

    /**
     * Allows two user supplied arguments per event.
     *
     * @param <A> Class of the user supplied argument
     * @param <B> Class of the user supplied argument
     * @param translator    用户指定的事件翻译器
     * @param batchStartsAt 批次在数组中的起始下标
     * @param batchSize     批次实际大小
     * @param arg0          用户参数数组，每个元素对应一个事件
     * @param arg1          用户参数数组，每个元素对应一个事件
     * @see #publishEvents(EventTranslator[])
     */
    <A, B> void publishEvents(
        EventTranslatorTwoArg<E, A, B> translator, int batchStartsAt, int batchSize, A[] arg0,
        B[] arg1);

    /**
     * Allows two user supplied arguments per event.
     *
     * @param <A> Class of the user supplied argument
     * @param <B> Class of the user supplied argument
     * @param translator The user specified translation for the event
     * @param arg0       An array of user supplied arguments, one element per event.
     * @param arg1       An array of user supplied arguments, one element per event.
     * @return true if the value was published, false if there was insufficient
     * capacity.
     * @see #tryPublishEvents(com.lmax.disruptor.EventTranslator[])
     */
    <A, B> boolean tryPublishEvents(EventTranslatorTwoArg<E, A, B> translator, A[] arg0, B[] arg1);

    /**
     * Allows two user supplied arguments per event.
     *
     * @param <A> Class of the user supplied argument
     * @param <B> Class of the user supplied argument
     * @param translator    The user specified translation for the event
     * @param batchStartsAt The first element of the array which is within the batch.
     * @param batchSize     The actual size of the batch.
     * @param arg0          An array of user supplied arguments, one element per event.
     * @param arg1          An array of user supplied arguments, one element per event.
     * @return true if the value was published, false if there was insufficient
     * capacity.
     * @see #tryPublishEvents(EventTranslator[])
     */
    <A, B> boolean tryPublishEvents(
        EventTranslatorTwoArg<E, A, B> translator, int batchStartsAt, int batchSize,
        A[] arg0, B[] arg1);

    /**
     * 每个事件支持传入三个用户参数。
     *
     * @param <A> 第一个用户参数类型
     * @param <B> 第二个用户参数类型
     * @param <C> 第三个用户参数类型
     * @param translator 用户指定的事件翻译器
     * @param arg0       用户参数数组，每个元素对应一个事件
     * @param arg1       用户参数数组，每个元素对应一个事件
     * @param arg2       用户参数数组，每个元素对应一个事件
     * @see #publishEvents(com.lmax.disruptor.EventTranslator[])
     */
    <A, B, C> void publishEvents(EventTranslatorThreeArg<E, A, B, C> translator, A[] arg0, B[] arg1, C[] arg2);

    /**
     * Allows three user supplied arguments per event.
     *
     * @param <A> Class of the user supplied argument
     * @param <B> Class of the user supplied argument
     * @param <C> Class of the user supplied argument
     * @param translator    The user specified translation for the event
     * @param batchStartsAt The first element of the array which is within the batch.
     * @param batchSize     批次中的元素个数
     * @param arg0          用户参数数组，每个元素对应一个事件
     * @param arg1          用户参数数组，每个元素对应一个事件
     * @param arg2          用户参数数组，每个元素对应一个事件
     * @see #publishEvents(EventTranslator[])
     */
    <A, B, C> void publishEvents(
        EventTranslatorThreeArg<E, A, B, C> translator, int batchStartsAt, int batchSize,
        A[] arg0, B[] arg1, C[] arg2);

    /**
     * Allows three user supplied arguments per event.
     *
     * @param <A> Class of the user supplied argument
     * @param <B> Class of the user supplied argument
     * @param <C> Class of the user supplied argument
     * @param translator The user specified translation for the event
     * @param arg0       An array of user supplied arguments, one element per event.
     * @param arg1       An array of user supplied arguments, one element per event.
     * @param arg2       An array of user supplied arguments, one element per event.
     * @return true if the value was published, false if there was insufficient
     * capacity.
     * @see #publishEvents(com.lmax.disruptor.EventTranslator[])
     */
    <A, B, C> boolean tryPublishEvents(EventTranslatorThreeArg<E, A, B, C> translator, A[] arg0, B[] arg1, C[] arg2);

    /**
     * Allows three user supplied arguments per event.
     *
     * @param <A> Class of the user supplied argument
     * @param <B> Class of the user supplied argument
     * @param <C> Class of the user supplied argument
     * @param translator    The user specified translation for the event
     * @param batchStartsAt The first element of the array which is within the batch.
     * @param batchSize     The actual size of the batch.
     * @param arg0          An array of user supplied arguments, one element per event.
     * @param arg1          An array of user supplied arguments, one element per event.
     * @param arg2          An array of user supplied arguments, one element per event.
     * @return true if the value was published, false if there was insufficient
     * capacity.
     * @see #publishEvents(EventTranslator[])
     */
    <A, B, C> boolean tryPublishEvents(
        EventTranslatorThreeArg<E, A, B, C> translator, int batchStartsAt,
        int batchSize, A[] arg0, B[] arg1, C[] arg2);

    /**
     * 每个事件支持传入可变数量的用户参数。
     *
     * @param translator 用户指定的事件翻译器
     * @param args       用户参数，每个事件对应一个 {@code Object[]}
     * @see #publishEvents(com.lmax.disruptor.EventTranslator[])
     */
    void publishEvents(EventTranslatorVararg<E> translator, Object[]... args);

    /**
     * Allows a variable number of user supplied arguments per event.
     *
     * @param translator    The user specified translation for the event
     * @param batchStartsAt The first element of the array which is within the batch.
     * @param batchSize     The actual size of the batch
     * @param args          用户参数，每个事件对应一个 {@code Object[]}
     * @see #publishEvents(EventTranslator[])
     */
    void publishEvents(EventTranslatorVararg<E> translator, int batchStartsAt, int batchSize, Object[]... args);

    /**
     * Allows a variable number of user supplied arguments per event.
     *
     * @param translator The user specified translation for the event
     * @param args       User supplied arguments, one Object[] per event.
     * @return true if the value was published, false if there was insufficient
     * capacity.
     * @see #publishEvents(com.lmax.disruptor.EventTranslator[])
     */
    boolean tryPublishEvents(EventTranslatorVararg<E> translator, Object[]... args);

    /**
     * Allows a variable number of user supplied arguments per event.
     *
     * @param translator    The user specified translation for the event
     * @param batchStartsAt The first element of the array which is within the batch.
     * @param batchSize     The actual size of the batch.
     * @param args          User supplied arguments, one Object[] per event.
     * @return true if the value was published, false if there was insufficient
     * capacity.
     * @see #publishEvents(EventTranslator[])
     */
    boolean tryPublishEvents(EventTranslatorVararg<E> translator, int batchStartsAt, int batchSize, Object[]... args);

}