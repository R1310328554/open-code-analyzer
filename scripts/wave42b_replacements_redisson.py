"""Chinese annotation replacements for Redisson 4.7.0 wave-42b api [15:30]."""
from __future__ import annotations

_A = "redisson/src/main/java/org/redisson/api/"

W42B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# --- shared method javadoc blocks ---

_CMP_DEL = (
    "    /**\n     * Atomically deletes the value if it satisfies the condition\n     * defined by the specified arguments.\n     *\n     * @param args compare and delete arguments\n     * @return {@code true} if deleted, {@code false} otherwise\n     */",
    "    /**\n     * 若当前值满足 {@link CompareAndDeleteArgs} 定义的条件，则原子删除。\n     *\n     * @param args 比较并删除参数\n     * @return 删除成功为 {@code true}，否则 {@code false}\n     */",
)
_CMP_SET = (
    "    /**\n     * Atomically sets the value to the given updated value\n     * only if the current value {@code ==} the expected value.\n     *\n     * @param expect the expected value\n     * @param update the new value\n     * @return true if successful; or false if the actual value\n     *         was not equal to the expected value.\n     */",
    "    /**\n     * 仅当当前值等于 {@code expect} 时，原子设置为 {@code update}（CAS）。\n     *\n     * @param expect 期望值\n     * @param update 新值\n     * @return 成功为 {@code true}；当前值与期望不符则为 {@code false}\n     */",
)
_ADD_GET = (
    "    /**\n     * Atomically adds the given value to the current value.\n     *\n     * @param delta the value to add\n     * @return the updated value\n     */",
    "    /**\n     * 原子地将 {@code delta} 加到当前值上。\n     *\n     * @param delta 增量\n     * @return 更新后的值\n     */",
)
_DEC_GET = (
    "    /**\n     * Atomically decrements the current value by one.\n     *\n     * @return the updated value\n     */",
    "    /**\n     * 原子地将当前值减 1。\n     *\n     * @return 更新后的值\n     */",
)
_GET_CUR = (
    "    /**\n     * Returns current value.\n     *\n     * @return current value\n     */",
    "    /**\n     * 返回当前值。\n     *\n     * @return 当前值\n     */",
)
_GET_CUR_LONG = (
    "    /**\n     * Returns current value.\n     *\n     * @return the current value\n     */",
    "    /**\n     * 返回当前值。\n     *\n     * @return 当前值\n     */",
)
_GET_DEL = (
    "    /**\n     * Returns and deletes object\n     * \n     * @return the current value\n     */",
    "    /**\n     * 读取当前值并删除该 Redis 键。\n     *\n     * @return 删除前的值\n     */",
)
_GET_DEL_LONG = (
    "    /**\n     * Gets and deletes object\n     * \n     * @return the current value\n     */",
    "    /**\n     * 读取当前值并删除该 Redis 键。\n     *\n     * @return 删除前的值\n     */",
)
_GET_ADD_UPD = (
    "    /**\n     * Atomically adds the given value to the current value.\n     *\n     * @param delta the value to add\n     * @return the updated value\n     */",
    "    /**\n     * 原子地将 {@code delta} 加到当前值上并返回更新后的值。\n     *\n     * @param delta 增量\n     * @return 更新后的值\n     */",
)
_GET_ADD_OLD = (
    "    /**\n     * Atomically adds the given value to the current value.\n     *\n     * @param delta the value to add\n     * @return the old value before the add\n     */",
    "    /**\n     * 原子地将 {@code delta} 加到当前值上并返回加之前的旧值。\n     *\n     * @param delta 增量\n     * @return 相加前的旧值\n     */",
)
_GET_SET = (
    "    /**\n     * Atomically sets the given value and returns the old value.\n     *\n     * @param newValue the new value\n     * @return the old value\n     */",
    "    /**\n     * 原子地设置为 {@code newValue} 并返回旧值。\n     *\n     * @param newValue 新值\n     * @return 设置前的旧值\n     */",
)
_INC_GET = (
    "    /**\n     * Atomically increments the current value by one.\n     *\n     * @return the updated value\n     */",
    "    /**\n     * 原子地将当前值加 1。\n     *\n     * @return 更新后的值\n     */",
)
_INC_GET_ARGS = (
    "    /**\n     * Atomically increments the current value according to the specified arguments.\n     *\n     * @param args increment arguments\n     * @return the updated value\n     */",
    "    /**\n     * 按 {@code args} 指定的步长与上界原子递增当前值。\n     *\n     * @param args 递增参数（步长、上界等）\n     * @return 更新后的值\n     */",
)
_INC_OLD = (
    "    /**\n     * Atomically increments the current value by one.\n     *\n     * @return the old value\n     */",
    "    /**\n     * 原子地将当前值加 1 并返回旧值。\n     *\n     * @return 加 1 前的旧值\n     */",
)
_DEC_OLD = (
    "    /**\n     * Atomically decrements by one the current value.\n     *\n     * @return the previous value\n     */",
    "    /**\n     * 原子地将当前值减 1 并返回旧值。\n     *\n     * @return 减 1 前的旧值\n     */",
)
_SET_VAL_VOID = (
    "    /**\n     * Atomically sets the given value.\n     *\n     * @param newValue the new value\n     * @return void\n     */",
    "    /**\n     * 原子地设置为 {@code newValue}。\n     *\n     * @param newValue 新值\n     */",
)
_SET_VAL = (
    "    /**\n     * Atomically sets the given value.\n     *\n     * @param newValue the new value\n     */",
    "    /**\n     * 原子地设置为 {@code newValue}。\n     *\n     * @param newValue 新值\n     */",
)
_SET_LESS = (
    "    /**\n     * Atomically sets the given value if current value is less than\n     * the special value\n     *\n     * @param less  compare value\n     * @param value newValue\n     * @return true when the value update is successful\n     */",
    "    /**\n     * 仅当当前值小于 {@code less} 时，原子设置为 {@code value}。\n     *\n     * @param less 比较阈值\n     * @param value 新值\n     * @return 更新成功为 {@code true}\n     */",
)
_SET_GREATER = (
    "    /**\n     * Atomically sets the given value if current value is greater than\n     * the special value\n     *\n     * @param greater  compare value\n     * @param value newValue\n     * @return true when the value update is successful\n     */",
    "    /**\n     * 仅当当前值大于 {@code greater} 时，原子设置为 {@code value}。\n     *\n     * @param greater 比较阈值\n     * @param value 新值\n     * @return 更新成功为 {@code true}\n     */",
)
_ADD_LISTENER = (
    "    /**\n     * Adds object event listener\n     *\n     * @see org.redisson.api.listener.IncrByListener\n     * @see org.redisson.api.ExpiredObjectListener\n     * @see org.redisson.api.DeletedObjectListener\n     *\n     * @param listener object event listener\n     * @return listener id\n     */",
    "    /**\n     * 注册对象事件监听器。\n     *\n     * @see org.redisson.api.listener.IncrByListener\n     * @see org.redisson.api.ExpiredObjectListener\n     * @see org.redisson.api.DeletedObjectListener\n     *\n     * @param listener 事件监听器\n     * @return 监听器 ID\n     */",
)
_ADD_LISTENER_ASYNC = (
    "    /**\n     * Adds object event listener\n     *\n     * @see org.redisson.api.listener.IncrByListener\n     * @see org.redisson.api.ExpiredObjectListener\n     * @see org.redisson.api.DeletedObjectListener\n     *\n     * @param listener - object event listener\n     * @return listener id\n     */",
    "    /**\n     * 注册对象事件监听器。\n     *\n     * @see org.redisson.api.listener.IncrByListener\n     * @see org.redisson.api.ExpiredObjectListener\n     * @see org.redisson.api.DeletedObjectListener\n     *\n     * @param listener 事件监听器\n     * @return 监听器 ID\n     */",
)

# --- RAtomicDoubleAsync ---

_atomic_double_async = [
    (
        "/**\n * Distributed implementation to the AtomicDouble\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link RAtomicDouble} 的异步 API 接口。\n"
        " * <p>各方法返回 {@link RFuture}，基于 Redis {@code INCRBYFLOAT} 等命令实现分布式浮点原子操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    _CMP_DEL,
    _CMP_SET,
    _ADD_GET,
    _DEC_GET,
    _GET_CUR,
    _GET_DEL,
    _GET_ADD_UPD,
    _GET_SET,
    _INC_GET,
    _INC_GET_ARGS,
    _INC_OLD,
    _DEC_OLD,
    _SET_VAL_VOID,
    _SET_LESS,
    _SET_GREATER,
    _ADD_LISTENER_ASYNC,
]
W42B_REPLACEMENTS[f"{_A}RAtomicDoubleAsync.java"] = _atomic_double_async
W42B_REPLACEMENTS["RAtomicDoubleAsync.java"] = _atomic_double_async

# --- RAtomicDoubleReactive ---

_atomic_double_reactive = [
    (
        "/**\n * Reactive interface for AtomicDouble object\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link RAtomicDouble} 的 Reactor 风格 API 接口。\n"
        " * <p>各方法返回 {@link Mono}，用于非阻塞响应式编程。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    _CMP_DEL,
    _CMP_SET,
    _ADD_GET,
    _DEC_GET,
    _GET_CUR,
    _GET_DEL,
    _GET_ADD_UPD,
    _GET_SET,
    _INC_GET,
    _INC_GET_ARGS,
    _INC_OLD,
    _DEC_OLD,
    _SET_VAL_VOID,
    _SET_LESS,
    _SET_GREATER,
]
W42B_REPLACEMENTS[f"{_A}RAtomicDoubleReactive.java"] = _atomic_double_reactive
W42B_REPLACEMENTS["RAtomicDoubleReactive.java"] = _atomic_double_reactive

# --- RAtomicDoubleRx ---

_atomic_double_rx = [
    (
        "/**\n * Reactive interface for AtomicDouble object\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link RAtomicDouble} 的 RxJava 风格 API 接口。\n"
        " * <p>各方法返回 RxJava3 的 {@link Single} 或 {@link Completable}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    _CMP_DEL,
    _CMP_SET,
    _ADD_GET,
    _DEC_GET,
    _GET_CUR,
    _GET_DEL,
    _GET_ADD_UPD,
    _GET_SET,
    _INC_GET,
    _INC_GET_ARGS,
    _INC_OLD,
    _DEC_OLD,
    _SET_VAL_VOID,
    _SET_LESS,
    _SET_GREATER,
]
W42B_REPLACEMENTS[f"{_A}RAtomicDoubleRx.java"] = _atomic_double_rx
W42B_REPLACEMENTS["RAtomicDoubleRx.java"] = _atomic_double_rx

# --- RAtomicLong ---

_atomic_long = [
    (
        "/**\n * Distributed implementation of {@link java.util.concurrent.atomic.AtomicLong}\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 对标 {@link java.util.concurrent.atomic.AtomicLong} 的分布式长整型原子计数器 API。\n"
        " * <p>基于 Redis {@code INCR/DECR/INCRBY} 命令，支持 CAS、条件删除与带界递增。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    _CMP_DEL,
    _DEC_OLD,
    _ADD_GET,
    _CMP_SET,
    _DEC_GET,
    _GET_CUR_LONG,
    _GET_DEL_LONG,
    _GET_ADD_OLD,
    _GET_SET,
    _INC_GET,
    _INC_GET_ARGS,
    _INC_OLD,
    _SET_VAL,
    _SET_LESS,
    _SET_GREATER,
    _ADD_LISTENER,
]
W42B_REPLACEMENTS[f"{_A}RAtomicLong.java"] = _atomic_long
W42B_REPLACEMENTS["RAtomicLong.java"] = _atomic_long

# --- RAtomicLongAsync ---

_atomic_long_async = [
    (
        "/**\n * Distributed async implementation of {@link java.util.concurrent.atomic.AtomicLong}\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link RAtomicLong} 的异步 API 接口。\n"
        " * <p>各方法返回 {@link RFuture}，基于 Redis 原子整型命令实现。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    _CMP_DEL,
    _CMP_SET,
    _ADD_GET,
    _DEC_GET,
    _GET_CUR_LONG,
    _GET_DEL,
    _GET_ADD_OLD,
    _GET_SET,
    _INC_GET,
    _INC_GET_ARGS,
    _INC_OLD,
    _DEC_OLD,
    _SET_VAL_VOID,
    _SET_LESS,
    _SET_GREATER,
    _ADD_LISTENER_ASYNC,
]
W42B_REPLACEMENTS[f"{_A}RAtomicLongAsync.java"] = _atomic_long_async
W42B_REPLACEMENTS["RAtomicLongAsync.java"] = _atomic_long_async

# --- RAtomicLongReactive ---

_atomic_long_reactive = [
    (
        "/**\n * Reactive interface for AtomicLong object\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link RAtomicLong} 的 Reactor 风格 API 接口。\n"
        " * <p>各方法返回 {@link Mono}，用于非阻塞响应式编程。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    _CMP_DEL,
    _CMP_SET,
    _ADD_GET,
    _DEC_GET,
    _GET_CUR_LONG,
    _GET_DEL,
    _GET_ADD_OLD,
    _GET_SET,
    _INC_GET,
    _INC_GET_ARGS,
    _INC_OLD,
    _DEC_OLD,
    _SET_VAL_VOID,
    _SET_LESS,
    _SET_GREATER,
]
W42B_REPLACEMENTS[f"{_A}RAtomicLongReactive.java"] = _atomic_long_reactive
W42B_REPLACEMENTS["RAtomicLongReactive.java"] = _atomic_long_reactive

# --- RAtomicLongRx ---

_atomic_long_rx = [
    (
        "/**\n * RxJava2 interface for AtomicLong object\n *\n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link RAtomicLong} 的 RxJava 风格 API 接口。\n"
        " * <p>各方法返回 RxJava3 的 {@link Single} 或 {@link Completable}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    _CMP_DEL,
    _CMP_SET,
    _ADD_GET,
    _DEC_GET,
    _GET_CUR_LONG,
    _GET_DEL,
    _GET_ADD_OLD,
    _GET_SET,
    _INC_GET,
    _INC_GET_ARGS,
    _INC_OLD,
    _DEC_OLD,
    _SET_VAL_VOID,
    _SET_LESS,
    _SET_GREATER,
]
W42B_REPLACEMENTS[f"{_A}RAtomicLongRx.java"] = _atomic_long_rx
W42B_REPLACEMENTS["RAtomicLongRx.java"] = _atomic_long_rx

# --- RBinaryStream ---

_binary_stream = [
    (
        "/**\n * Binary stream holder stores a sequence of bytes.\n * Maximum size of stream is limited to 512Mb.\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 二进制流容器，在 Redis 中存储字节序列。\n"
        " * <p>单个流最大容量为 512MB。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Returns async channel object which allows to write and read binary stream.\n     * This object isn't thread-safe.\n     *\n     * @return channel object\n     */",
        "    /**\n     * 返回用于读写二进制流的异步 {@link AsynchronousByteChannel}。\n"
        "     * <p>该对象非线程安全。\n"
        "     *\n"
        "     * @return 异步字节通道\n"
        "     */",
    ),
    (
        "    /**\n     * Returns channel object which allows to write and read binary stream.\n     * This object isn't thread-safe.\n     *\n     * @return channel object\n     */",
        "    /**\n     * 返回用于读写二进制流的可定位 {@link SeekableByteChannel}。\n"
        "     * <p>该对象非线程安全。\n"
        "     *\n"
        "     * @return 可定位字节通道\n"
        "     */",
    ),
    (
        "    /**\n     * Returns inputStream object which allows to read binary stream.\n     * This object isn't thread-safe.\n     * \n     * @return stream object\n     */",
        "    /**\n     * 返回用于读取二进制流的 {@link InputStream}。\n"
        "     * <p>该对象非线程安全。\n"
        "     *\n"
        "     * @return 输入流\n"
        "     */",
    ),
    (
        "    /**\n     * Returns outputStream object which allows to write binary stream.\n     * This object isn't thread-safe.\n     * \n     * @return stream object\n     */",
        "    /**\n     * 返回用于写入二进制流的 {@link OutputStream}。\n"
        "     * <p>该对象非线程安全。\n"
        "     *\n"
        "     * @return 输出流\n"
        "     */",
    ),
]
W42B_REPLACEMENTS[f"{_A}RBinaryStream.java"] = _binary_stream
W42B_REPLACEMENTS["RBinaryStream.java"] = _binary_stream

# --- RBinaryStreamReactive / Rx (shared body) ---

_binary_stream_reactive_body = [
    (
        "/**\n * Binary stream holder stores a sequence of bytes.\n * Maximum size of stream is limited to 512Mb.\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link RBinaryStream} 的 Reactor 风格 API 接口。\n"
        " * <p>单个流最大容量为 512MB；支持按位置读写 {@link ByteBuffer}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Returns current channel's position\n     *\n     * @return current position\n     */",
        "    /**\n     * 返回当前读写位置（字节偏移）。\n     *\n     * @return 当前位置\n     */",
    ),
    (
        "    /**\n     * Sets channel's position\n     *\n     * @param newPosition - new position\n     */",
        "    /**\n     * 设置读写位置。\n     *\n     * @param newPosition 新的字节偏移\n     */",
    ),
    (
        "    /**\n     * Reads a sequence of bytes into defined buffer.\n     *\n     * @param buf buffer object into which bytes are read\n     * @return amount of read bytes\n     */",
        "    /**\n     * 从当前位置读取字节到 {@code buf}。\n     *\n     * @param buf 目标缓冲区\n     * @return 实际读取的字节数\n     */",
    ),
    (
        "    /**\n     * Writes a sequence of bytes from defined buffer.\n     *\n     * @param  buf buffer object from which bytes are transferred\n     * @return amount of written bytes\n     */",
        "    /**\n     * 将 {@code buf} 中的字节写入当前位置。\n     *\n     * @param buf 源缓冲区\n     * @return 实际写入的字节数\n     */",
    ),
]
W42B_REPLACEMENTS[f"{_A}RBinaryStreamReactive.java"] = _binary_stream_reactive_body
W42B_REPLACEMENTS["RBinaryStreamReactive.java"] = _binary_stream_reactive_body

_binary_stream_rx_body = [
    (
        "/**\n * Binary stream holder stores a sequence of bytes.\n * Maximum size of stream is limited to 512Mb.\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * {@link RBinaryStream} 的 RxJava 风格 API 接口。\n"
        " * <p>单个流最大容量为 512MB；支持按位置读写 {@link ByteBuffer}。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " */",
    ),
    (
        "    /**\n     * Returns current channel's position\n     *\n     * @return current position\n     */",
        "    /**\n     * 返回当前读写位置（字节偏移）。\n     *\n     * @return 当前位置\n     */",
    ),
    (
        "    /**\n     * Sets channel's position\n     *\n     * @param newPosition - new position\n     */",
        "    /**\n     * 设置读写位置。\n     *\n     * @param newPosition 新的字节偏移\n     */",
    ),
    (
        "    /**\n     * Reads a sequence of bytes into defined buffer.\n     *\n     * @param buf buffer object into which bytes are read\n     * @return amount of read bytes\n     */",
        "    /**\n     * 从当前位置读取字节到 {@code buf}。\n     *\n     * @param buf 目标缓冲区\n     * @return 实际读取的字节数\n     */",
    ),
    (
        "    /**\n     * Writes a sequence of bytes from defined buffer.\n     *\n     * @param  buf buffer object from which bytes are transferred\n     * @return amount of written bytes\n     */",
        "    /**\n     * 将 {@code buf} 中的字节写入当前位置。\n     *\n     * @param buf 源缓冲区\n     * @return 实际写入的字节数\n     */",
    ),
]
W42B_REPLACEMENTS[f"{_A}RBinaryStreamRx.java"] = _binary_stream_rx_body
W42B_REPLACEMENTS["RBinaryStreamRx.java"] = _binary_stream_rx_body

# --- shared blocking deque poll-from-any javadoc ---

_POLL_FIRST_ANY = (
    "    /**\n     * Retrieves and removes first available head element of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue own.\n     * \n     * @param queueNames - names of queue\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     * @throws InterruptedException if interrupted while waiting\n     */",
    "    /**\n     * 从指定队列集合（含自身）中阻塞拉取首个可用<b>队头</b>元素并移除。\n"
    "     * <p>在 {@code timeout} 内无元素可用则返回 {@code null}。\n"
    "     *\n"
    "     * @param queueNames 候选队列名列表\n"
    "     * @param timeout 最长等待时间\n"
    "     * @param unit 时间单位\n"
    "     * @return 取到的元素；超时为 {@code null}\n"
    "     * @throws InterruptedException 等待被中断时\n"
    "     */",
)
_POLL_LAST_ANY = (
    "    /**\n     * Retrieves and removes first available tail element of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue own.\n     * \n     * @param queueNames - names of queue\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     * @throws InterruptedException if interrupted while waiting\n     */",
    "    /**\n     * 从指定队列集合（含自身）中阻塞拉取首个可用<b>队尾</b>元素并移除。\n"
    "     * <p>在 {@code timeout} 内无元素可用则返回 {@code null}。\n"
    "     *\n"
    "     * @param queueNames 候选队列名列表\n"
    "     * @param timeout 最长等待时间\n"
    "     * @param unit 时间单位\n"
    "     * @return 取到的元素；超时为 {@code null}\n"
    "     * @throws InterruptedException 等待被中断时\n"
    "     */",
)
_POLL_FIRST_ANY_ASYNC = (
    "    /**\n     * Retrieves and removes first available head element of <b>any</b> queue in async mode,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue own.\n     *\n     * @param queueNames - names of queue\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     */",
    "    /**\n     * 异步地从指定队列集合（含自身）中拉取首个可用<b>队头</b>元素并移除。\n"
    "     * <p>在 {@code timeout} 内无元素可用则结果为 {@code null}。\n"
    "     *\n"
    "     * @param queueNames 候选队列名列表\n"
    "     * @param timeout 最长等待时间\n"
    "     * @param unit 时间单位\n"
    "     * @return 取到的元素；超时为 {@code null}\n"
    "     */",
)
_POLL_LAST_ANY_ASYNC = (
    "    /**\n     * Retrieves and removes first available tail element of <b>any</b> queue in async mode,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue own.\n     * \n     * @param queueNames - names of queue\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     */",
    "    /**\n     * 异步地从指定队列集合（含自身）中拉取首个可用<b>队尾</b>元素并移除。\n"
    "     * <p>在 {@code timeout} 内无元素可用则结果为 {@code null}。\n"
    "     *\n"
    "     * @param queueNames 候选队列名列表\n"
    "     * @param timeout 最长等待时间\n"
    "     * @param unit 时间单位\n"
    "     * @return 取到的元素；超时为 {@code null}\n"
    "     */",
)
_POLL_FIRST_ANY_REACTIVE = (
    "    /**\n     * Retrieves and removes first available head element of <b>any</b> queue in reactive mode,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue own.\n     *\n     * @param queueNames - names of queue\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     */",
    "    /**\n     * 响应式地从指定队列集合（含自身）中拉取首个可用<b>队头</b>元素并移除。\n"
    "     * <p>在 {@code timeout} 内无元素可用则结果为 {@code null}。\n"
    "     *\n"
    "     * @param queueNames 候选队列名列表\n"
    "     * @param timeout 最长等待时间\n"
    "     * @param unit 时间单位\n"
    "     * @return 取到的元素；超时为 {@code null}\n"
    "     */",
)
_POLL_LAST_ANY_REACTIVE = (
    "    /**\n     * Retrieves and removes first available tail element of <b>any</b> queue in reactive mode,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue own.\n     * \n     * @param queueNames - names of queue\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     */",
    "    /**\n     * 响应式地从指定队列集合（含自身）中拉取首个可用<b>队尾</b>元素并移除。\n"
    "     * <p>在 {@code timeout} 内无元素可用则结果为 {@code null}。\n"
    "     *\n"
    "     * @param queueNames 候选队列名列表\n"
    "     * @param timeout 最长等待时间\n"
    "     * @param unit 时间单位\n"
    "     * @return 取到的元素；超时为 {@code null}\n"
    "     */",
)
_PUT_FIRST = (
    "    /**\n     * Adds value to the head of queue.\n     * \n     * @param e value\n     * @return void\n     */",
    "    /**\n     * 阻塞地将元素插入队头。\n     *\n     * @param e 待插入元素\n     */",
)
_PUT_LAST = (
    "    /**\n     * Adds value to the tail of queue.\n     * \n     * @param e value\n     * @return void\n     */",
    "    /**\n     * 阻塞地将元素插入队尾。\n     *\n     * @param e 待插入元素\n     */",
)
_POLL_LAST_TO = (
    "    /**\n     * Retrieves and removes value at the tail of queue. If necessary waits up to defined <code>timeout</code> for an element become available.\n     * \n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return the element at the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     */",
    "    /**\n     * 从队尾取出并移除元素；必要时阻塞等待至多 {@code timeout}。\n"
    "     *\n"
    "     * @param timeout 最长等待时间\n"
    "     * @param unit 时间单位\n"
    "     * @return 队尾元素；超时为 {@code null}\n"
    "     */",
)
_TAKE_LAST = (
    "    /**\n     * Retrieves and removes value at the tail of queue. Waits for an element become available.\n     * \n     * @return the tail element of this queue\n     */",
    "    /**\n     * 阻塞地从队尾取出并移除元素，直至有元素可用。\n"
    "     *\n"
    "     * @return 队尾元素\n"
    "     */",
)
_POLL_FIRST_TO = (
    "    /**\n     * Retrieves and removes value at the head of queue. If necessary waits up to defined <code>timeout</code> for an element become available.\n     * \n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return the element at the tail of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     */",
    "    /**\n     * 从队头取出并移除元素；必要时阻塞等待至多 {@code timeout}。\n"
    "     *\n"
    "     * @param timeout 最长等待时间\n"
    "     * @param unit 时间单位\n"
    "     * @return 队头元素；超时为 {@code null}\n"
    "     */",
)
_TAKE_FIRST = (
    "    /**\n     * Retrieves and removes value at the head of queue. Waits for an element become available.\n     * \n     * @return the head element of this queue\n     */",
    "    /**\n     * 阻塞地从队头取出并移除元素，直至有元素可用。\n"
    "     *\n"
    "     * @return 队头元素\n"
    "     */",
)
_TAKE_FIRST_ELEMENTS = (
    "    /**\n     * Retrieves and removes continues stream of elements from the head of this queue. \n     * Waits for next element become available.\n     * \n     * @return stream of head elements\n     */",
    "    /**\n     * 持续从队头阻塞取元素并移除，形成元素流。\n"
    "     * <p>每次等待下一个元素可用后再发射。\n"
    "     *\n"
    "     * @return 队头元素流\n"
    "     */",
)
_TAKE_LAST_ELEMENTS = (
    "    /**\n     * Retrieves and removes continues stream of elements from the tail of this queue. \n     * Waits for next element become available.\n     * \n     * @return stream of tail elements\n     */",
    "    /**\n     * 持续从队尾阻塞取元素并移除，形成元素流。\n"
    "     * <p>每次等待下一个元素可用后再发射。\n"
    "     *\n"
    "     * @return 队尾元素流\n"
    "     */",
)

# --- RBlockingDeque ---

_blocking_deque = [
    (
        "/**\n * {@link BlockingDeque} backed by Redis\n *\n * @author Nikita Koksharov\n * @param <V> the type of elements held in this collection\n */",
        "/**\n"
        " * 由 Redis 列表实现的分布式 {@link BlockingDeque}。\n"
        " * <p>支持双端阻塞入队/出队、跨队列批量拉取及元素订阅。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 集合元素类型\n"
        " */",
    ),
    _POLL_FIRST_ANY,
    _POLL_LAST_ANY,
    (
        "    V move(Duration timeout, DequeMoveArgs args);",
        "    /**\n     * 按 {@link DequeMoveArgs} 在双端队列间原子迁移元素。\n"
        "     *\n"
        "     * @param timeout 最长等待时间\n"
        "     * @param args 迁移参数\n"
        "     * @return 迁移的元素；超时可能为 {@code null}\n"
        "     */\n"
        "    V move(Duration timeout, DequeMoveArgs args);",
    ),
    (
        "    /**\n     * Use {@link #subscribeOnFirstElements(Function)} instead.\n     *\n     * @param consumer - queue elements listener\n     * @return listenerId - id of listener\n     */",
        "    /**\n     * 已废弃，请改用 {@link #subscribeOnFirstElements(Function)}。\n"
        "     *\n"
        "     * @param consumer 队列元素监听器\n"
        "     * @return 监听器 ID\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #subscribeOnLastElements(Function)} instead.\n     *\n     * @param consumer - queue elements listener\n     * @return listenerId - id of listener\n     */",
        "    /**\n     * 已废弃，请改用 {@link #subscribeOnLastElements(Function)}。\n"
        "     *\n"
        "     * @param consumer 队列元素监听器\n"
        "     * @return 监听器 ID\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #subscribeOnLastElements(Function)} instead.\n     * Continuously invokes {@link #takeFirstAsync()} method to get a new element.\n     * <p>\n     * NOTE: don't call blocking methods in the elements listener\n     *\n     * @param consumer - queue elements listener\n     * @return listenerId - id of listener\n     */",
        "    /**\n     * 订阅队头新元素；内部循环调用 {@link #takeFirstAsync()} 取元素。\n"
        "     * <p>注意：监听器内勿调用阻塞方法。\n"
        "     *\n"
        "     * @param consumer 异步元素处理器\n"
        "     * @return 监听器 ID\n"
        "     */",
    ),
    (
        "    /**\n     * Subscribes on last elements appeared in this queue.\n     * Continuously invokes {@link #takeLastAsync()} method to get a new element.\n     * <p>\n     * NOTE: don't call blocking methods in the elements listener\n     *\n     * @param consumer - queue elements listener\n     * @return listenerId - id of listener\n     */",
        "    /**\n     * 订阅队尾新元素；内部循环调用 {@link #takeLastAsync()} 取元素。\n"
        "     * <p>注意：监听器内勿调用阻塞方法。\n"
        "     *\n"
        "     * @param consumer 异步元素处理器\n"
        "     * @return 监听器 ID\n"
        "     */",
    ),
]
W42B_REPLACEMENTS[f"{_A}RBlockingDeque.java"] = _blocking_deque
W42B_REPLACEMENTS["RBlockingDeque.java"] = _blocking_deque

# --- RBlockingDequeAsync ---

_blocking_deque_async = [
    (
        "/**\n * Distributed async implementation of {@link BlockingDeque}\n *\n * @author Nikita Koksharov\n * @param <V> the type of elements held in this collection\n */",
        "/**\n"
        " * {@link RBlockingDeque} 的异步 API 接口。\n"
        " * <p>各方法返回 {@link RFuture}，支持双端阻塞操作的非阻塞调用。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 集合元素类型\n"
        " */",
    ),
    _POLL_FIRST_ANY_ASYNC,
    _POLL_LAST_ANY_ASYNC,
    _PUT_FIRST,
    _PUT_LAST,
    _POLL_LAST_TO,
    _TAKE_LAST,
    _POLL_FIRST_TO,
    _TAKE_FIRST,
    (
        "    RFuture<V> moveAsync(Duration timeout, DequeMoveArgs args);",
        "    /**\n     * 异步地按 {@link DequeMoveArgs} 在双端队列间迁移元素。\n"
        "     *\n"
        "     * @param timeout 最长等待时间\n"
        "     * @param args 迁移参数\n"
        "     * @return 迁移结果的 {@link RFuture}\n"
        "     */\n"
        "    RFuture<V> moveAsync(Duration timeout, DequeMoveArgs args);",
    ),
]
W42B_REPLACEMENTS[f"{_A}RBlockingDequeAsync.java"] = _blocking_deque_async
W42B_REPLACEMENTS["RBlockingDequeAsync.java"] = _blocking_deque_async

# --- RBlockingDequeReactive ---

_blocking_deque_reactive = [
    (
        "/**\n * Reactive interface for Redis based BlockingDeque object\n *\n * @author Nikita Koksharov\n * @param <V> the type of elements held in this collection\n */",
        "/**\n"
        " * {@link RBlockingDeque} 的 Reactor 风格 API 接口。\n"
        " * <p>阻塞双端操作以 {@link Mono} 或 {@link Flux} 形式暴露。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 集合元素类型\n"
        " */",
    ),
    _POLL_FIRST_ANY_REACTIVE,
    _POLL_LAST_ANY_REACTIVE,
    _PUT_FIRST,
    _PUT_LAST,
    _POLL_LAST_TO,
    _TAKE_LAST,
    _POLL_FIRST_TO,
    _TAKE_FIRST,
    _TAKE_FIRST_ELEMENTS,
    _TAKE_LAST_ELEMENTS,
]
W42B_REPLACEMENTS[f"{_A}RBlockingDequeReactive.java"] = _blocking_deque_reactive
W42B_REPLACEMENTS["RBlockingDequeReactive.java"] = _blocking_deque_reactive

# --- RBlockingDequeRx ---

_blocking_deque_rx = [
    (
        "/**\n * RxJava2 interface for Redis based BlockingDeque object\n *\n * @author Nikita Koksharov\n * @param <V> the type of elements held in this collection\n */",
        "/**\n"
        " * {@link RBlockingDeque} 的 RxJava 风格 API 接口。\n"
        " * <p>阻塞双端操作以 RxJava3 的 {@link Single}、{@link Maybe} 或 {@link Flowable} 暴露。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 集合元素类型\n"
        " */",
    ),
    _POLL_FIRST_ANY_REACTIVE,
    _POLL_LAST_ANY_REACTIVE,
    _PUT_FIRST,
    _PUT_LAST,
    _POLL_LAST_TO,
    _TAKE_LAST,
    _POLL_FIRST_TO,
    _TAKE_FIRST,
    _TAKE_FIRST_ELEMENTS,
    _TAKE_LAST_ELEMENTS,
]
W42B_REPLACEMENTS[f"{_A}RBlockingDequeRx.java"] = _blocking_deque_rx
W42B_REPLACEMENTS["RBlockingDequeRx.java"] = _blocking_deque_rx

# --- RBlockingQueue ---

_blocking_queue = [
    (
        "/**\n * Distributed implementation of {@link BlockingQueue}\n *\n * @author Nikita Koksharov\n * @param <V> the type of elements held in this collection\n */",
        "/**\n"
        " * 由 Redis 列表实现的分布式 {@link BlockingQueue}。\n"
        " * <p>支持阻塞取元素、跨队列 poll、批量迁移及元素订阅。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 集合元素类型\n"
        " */",
    ),
    (
        "    /**\n     * Retrieves and removes first available head element of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * @param queueNames - queue names. Queue name itself is always included\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     * @throws InterruptedException if interrupted while waiting\n     */",
        "    /**\n     * 从指定队列集合（含自身）中阻塞拉取首个可用队头元素并移除。\n"
        "     * <p>当前队列名始终包含在候选集合中。\n"
        "     *\n"
        "     * @param queueNames 候选队列名（自身始终参与）\n"
        "     * @param timeout 最长等待时间\n"
        "     * @param unit 时间单位\n"
        "     * @return 取到的元素；超时为 {@code null}\n"
        "     * @throws InterruptedException 等待被中断时\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves and removes first available head element of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * @param queueNames queue names. Queue name itself is always included\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @return the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     * @throws InterruptedException if interrupted while waiting\n     */",
        "    /**\n     * 从指定队列集合（含自身）中阻塞拉取首个可用队头元素，并返回元素及其来源队列名。\n"
        "     *\n"
        "     * @param queueNames 候选队列名（自身始终参与）\n"
        "     * @param timeout 最长等待时间\n"
        "     * @return 队列名与元素的 {@link Entry}；超时为 {@code null}\n"
        "     * @throws InterruptedException 等待被中断时\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves and removes first available head elements of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * <p>\n     * Requires <b>Redis 7.0.0 and higher.</b>\n     *\n     * @param duration how long to wait before giving up\n     * @param count elements amount\n     * @param queueNames name of queues\n     * @return the head elements\n     */",
        "    /**\n     * 从多个队列（含自身）批量拉取队头元素。\n"
        "     * <p>需要 <b>Redis 7.0.0 及以上</b>。\n"
        "     *\n"
        "     * @param duration 最长等待时间\n"
        "     * @param count 每个队列最多拉取数量\n"
        "     * @param queueNames 候选队列名\n"
        "     * @return 队列名到元素列表的映射\n"
        "     * @throws InterruptedException 等待被中断时\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves and removes first available tail elements of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * <p>\n     * Requires <b>Redis 7.0.0 and higher.</b>\n     *\n     * @param duration how long to wait before giving up\n     * @param count elements amount\n     * @param queueNames name of queues\n     * @return the tail elements\n     */",
        "    /**\n     * 从多个队列（含自身）批量拉取队尾元素。\n"
        "     * <p>需要 <b>Redis 7.0.0 及以上</b>。\n"
        "     *\n"
        "     * @param duration 最长等待时间\n"
        "     * @param count 每个队列最多拉取数量\n"
        "     * @param queueNames 候选队列名\n"
        "     * @return 队列名到元素列表的映射\n"
        "     * @throws InterruptedException 等待被中断时\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves and removes first available tail element of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * @param queueNames queue names. Queue name itself is always included\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @return the tail of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     * @throws InterruptedException if interrupted while waiting\n     */",
        "    /**\n     * 从指定队列集合（含自身）中阻塞拉取首个可用队尾元素，并返回元素及其来源队列名。\n"
        "     *\n"
        "     * @param queueNames 候选队列名（自身始终参与）\n"
        "     * @param timeout 最长等待时间\n"
        "     * @return 队列名与元素的 {@link Entry}；超时为 {@code null}\n"
        "     * @throws InterruptedException 等待被中断时\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves and removes last available tail element of this queue and adds it at the head of <code>queueName</code>,\n     * waiting up to the specified wait time if necessary for an element to become available.\n     *\n     * @param queueName - names of destination queue\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return the tail of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     * @throws InterruptedException if interrupted while waiting\n     */",
        "    /**\n     * 从本队列队尾取出元素并插入目标队列队头；必要时阻塞等待。\n"
        "     *\n"
        "     * @param queueName 目标队列名\n"
        "     * @param timeout 最长等待时间\n"
        "     * @param unit 时间单位\n"
        "     * @return 被移动的元素；超时为 {@code null}\n"
        "     * @throws InterruptedException 等待被中断时\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves and removes last available tail element of <b>any</b> queue and adds it at the head of <code>queueName</code>,\n     * waiting if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * @param queueName - names of destination queue\n     * @return the tail of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     * @throws InterruptedException if interrupted while waiting\n     */",
        "    /**\n     * 从候选队列（含自身）中阻塞取出队尾元素并插入目标队列队头。\n"
        "     *\n"
        "     * @param queueName 目标队列名\n"
        "     * @return 被移动的元素\n"
        "     * @throws InterruptedException 等待被中断时\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves and removes the head elements of this queue\n     * and adds them at the tail of <code>queueName</code>.\n     * If necessary waits up to defined <code>timeout</code>\n     * for elements to become available.\n     * Returns moved elements.\n     * <p>\n     * Code example:\n     * <pre>\n     * List&lt;V&gt; elements = queue.move(Duration.ofSeconds(10),\n     *                                QueueMoveElementsArgs.to(\"myQueue\")\n     *                                                     .count(10));\n     * </pre>\n     * <p>\n     * Requires <b>Redis 8.10.0 and higher.</b>\n     *\n     * @param timeout how long to wait before giving up\n     * @param args - arguments object\n     * @return moved elements or empty list if the\n     *         specified waiting time elapses before elements are available\n     */",
        "    /**\n     * 将本队列队头元素批量迁移至目标队列队尾。\n"
        "     * <p>必要时阻塞等待至多 {@code timeout}；返回已迁移元素列表。\n"
        "     * <p>示例：\n"
        "     * <pre>\n"
        "     * List&lt;V&gt; elements = queue.move(Duration.ofSeconds(10),\n"
        "     *                                QueueMoveElementsArgs.to(\"myQueue\")\n"
        "     *                                                     .count(10));\n"
        "     * </pre>\n"
        "     * <p>需要 <b>Redis 8.10.0 及以上</b>。\n"
        "     *\n"
        "     * @param timeout 最长等待时间\n"
        "     * @param args 迁移参数\n"
        "     * @return 已迁移元素；超时返回空列表\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #subscribeOnElements(Function)} instead.\n     *\n     * @param consumer - queue elements listener\n     * @return listenerId - id of listener\n     */",
        "    /**\n     * 已废弃，请改用 {@link #subscribeOnElements(Function)}。\n"
        "     *\n"
        "     * @param consumer 队列元素监听器\n"
        "     * @return 监听器 ID\n"
        "     */",
    ),
    (
        "    /**\n     * Subscribes on elements appeared in this queue.\n     * Continuously invokes {@link #takeAsync()} method to get a new element.\n     * <p>\n     * NOTE: don't call blocking methods in the elements listener\n     *\n     * @param consumer - queue elements listener\n     * @return listenerId - id of listener\n     */",
        "    /**\n     * 订阅队列新元素；内部循环调用 {@link #takeAsync()} 取元素。\n"
        "     * <p>注意：监听器内勿调用阻塞方法。\n"
        "     *\n"
        "     * @param consumer 异步元素处理器\n"
        "     * @return 监听器 ID\n"
        "     */",
    ),
    (
        "    /**\n     * Un-subscribes defined listener.\n     *\n     * @param listenerId - id of listener\n     */",
        "    /**\n     * 取消指定 ID 的元素订阅。\n"
        "     *\n"
        "     * @param listenerId 监听器 ID\n"
        "     */",
    ),
]
W42B_REPLACEMENTS[f"{_A}RBlockingQueue.java"] = _blocking_queue
W42B_REPLACEMENTS["RBlockingQueue.java"] = _blocking_queue
