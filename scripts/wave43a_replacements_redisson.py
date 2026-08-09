"""Chinese annotation replacements for Redisson 4.7.0 wave-43a api [0:15]."""
from __future__ import annotations

_API = "redisson/src/main/java/org/redisson/api/"

W43A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}


def _add(rel: str, reps: list[tuple[str, str]]) -> None:
    W43A_REPLACEMENTS[rel] = reps
    W43A_REPLACEMENTS[rel.split("/")[-1]] = reps


# --- shared javadoc blocks ---

_DRAIN_TO_MAX = (
    "    /**\n     * Removes at most the given number of available elements from\n     * this queue and adds them to the given collection in async mode.  A failure\n     * encountered while attempting to add elements to\n     * collection {@code c} may result in elements being in neither,\n     * either or both collections when the associated exception is\n     * thrown.  Attempts to drain a queue to itself result in\n     * {@code IllegalArgumentException}. Further, the behavior of\n     * this operation is undefined if the specified collection is\n     * modified while the operation is in progress.\n     *\n     * @param c the collection to transfer elements into\n     * @param maxElements the maximum number of elements to transfer\n     * @return the number of elements transferred\n     * @throws UnsupportedOperationException if addition of elements\n     *         is not supported by the specified collection\n     * @throws ClassCastException if the class of an element of this queue\n     *         prevents it from being added to the specified collection\n     * @throws NullPointerException if the specified collection is null\n     * @throws IllegalArgumentException if the specified collection is this\n     *         queue, or some property of an element of this queue prevents\n     *         it from being added to the specified collection\n     */",
    "    /**\n     * 异步地将本队列中至多 {@code maxElements} 个可用元素移除并转入集合 {@code c}。\n"
    "     * <p>向目标集合添加失败时，元素可能留在原队列、目标集合或两者中；\n"
    "     * 不可将队列导入自身（否则抛出 {@code IllegalArgumentException}）；\n"
    "     * 操作进行中修改目标集合的行为未定义。\n"
    "     *\n     * @param c 目标集合\n"
    "     * @param maxElements 最多转移的元素数量\n"
    "     * @return 实际转移的元素数量\n"
    "     * @throws UnsupportedOperationException 目标集合不支持添加元素\n"
    "     * @throws ClassCastException 元素类型无法加入目标集合\n"
    "     * @throws NullPointerException 目标集合为 {@code null}\n"
    "     * @throws IllegalArgumentException 目标集合为本队列或元素属性不允许加入\n"
    "     */",
)

_DRAIN_TO_ALL = (
    "    /**\n     * Removes all available elements from this queue and adds them\n     * to the given collection in async mode.  This operation may be more\n     * efficient than repeatedly polling this queue.  A failure\n     * encountered while attempting to add elements to\n     * collection {@code c} may result in elements being in neither,\n     * either or both collections when the associated exception is\n     * thrown.  Attempts to drain a queue to itself result in\n     * {@code IllegalArgumentException}. Further, the behavior of\n     * this operation is undefined if the specified collection is\n     * modified while the operation is in progress.\n     *\n     * @param c the collection to transfer elements into\n     * @return the number of elements transferred\n     * @throws UnsupportedOperationException if addition of elements\n     *         is not supported by the specified collection\n     * @throws ClassCastException if the class of an element of this queue\n     *         prevents it from being added to the specified collection\n     * @throws NullPointerException if the specified collection is null\n     * @throws IllegalArgumentException if the specified collection is this\n     *         queue, or some property of an element of this queue prevents\n     *         it from being added to the specified collection\n     */",
    "    /**\n     * 异步地将本队列全部可用元素移除并转入集合 {@code c}；通常比循环 poll 更高效。\n"
    "     * <p>向目标集合添加失败时，元素可能留在原队列、目标集合或两者中；\n"
    "     * 不可将队列导入自身；操作进行中修改目标集合的行为未定义。\n"
    "     *\n     * @param c 目标集合\n"
    "     * @return 实际转移的元素数量\n"
    "     * @throws UnsupportedOperationException 目标集合不支持添加元素\n"
    "     * @throws ClassCastException 元素类型无法加入目标集合\n"
    "     * @throws NullPointerException 目标集合为 {@code null}\n"
    "     * @throws IllegalArgumentException 目标集合为本队列或元素属性不允许加入\n"
    "     */",
)

_POLL_FIRST_ANY_HEAD = (
    "    /**\n     * Retrieves and removes first available head element of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue own.\n     *\n     * @param queueNames - names of queue\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     * @throws InterruptedException if interrupted while waiting\n     */",
    "    /**\n     * 从指定队列集合（含自身）中阻塞拉取首个可用队头元素并移除。\n"
    "     * <p>在 {@code timeout} 内无元素可用则返回 {@code null}。\n"
    "     *\n     * @param queueNames 候选队列名列表\n"
    "     * @param timeout 最长等待时间\n"
    "     * @param unit 时间单位\n"
    "     * @return 取到的元素；超时为 {@code null}\n"
    "     * @throws InterruptedException 等待被中断时\n"
    "     */",
)

_POLL_FIRST_ANY_NAME = (
    "    /**\n     * Retrieves and removes first available head element of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * @param queueNames queue names. Queue name itself is always included\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @return the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     * @throws InterruptedException if interrupted while waiting\n     */",
    "    /**\n     * 从指定队列集合（含自身）中阻塞拉取首个可用队头元素，并返回元素及其来源队列名。\n"
    "     *\n     * @param queueNames 候选队列名（自身始终参与）\n"
    "     * @param timeout 最长等待时间\n"
    "     * @return 队列名与元素的 {@link Entry}；超时为 {@code null}\n"
    "     * @throws InterruptedException 等待被中断时\n"
    "     */",
)

_POLL_LAST_ANY_NAME = (
    "    /**\n     * Retrieves and removes first available tail element of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * @param queueNames queue names. Queue name itself is always included\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @return the tail of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     * @throws InterruptedException if interrupted while waiting\n     */",
    "    /**\n     * 从指定队列集合（含自身）中阻塞拉取首个可用队尾元素，并返回元素及其来源队列名。\n"
    "     *\n     * @param queueNames 候选队列名（自身始终参与）\n"
    "     * @param timeout 最长等待时间\n"
    "     * @return 队列名与元素的 {@link Entry}；超时为 {@code null}\n"
    "     * @throws InterruptedException 等待被中断时\n"
    "     */",
)

_POLL_FIRST_BATCH = (
    "    /**\n     * Retrieves and removes first available head elements of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * <p>\n     * Requires <b>Redis 7.0.0 and higher.</b>\n     *\n     * @param duration how long to wait before giving up\n     * @param count elements amount\n     * @param queueNames name of queues\n     * @return the head elements\n     */",
    "    /**\n     * 从多个队列（含自身）批量拉取队头元素。\n"
    "     * <p>需要 <b>Redis 7.0.0 及以上</b>。\n"
    "     *\n     * @param duration 最长等待时间\n"
    "     * @param count 每个队列最多拉取数量\n"
    "     * @param queueNames 候选队列名\n"
    "     * @return 队列名到元素列表的映射\n"
    "     */",
)

_POLL_LAST_BATCH = (
    "    /**\n     * Retrieves and removes first available tail elements of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * <p>\n     * Requires <b>Redis 7.0.0 and higher.</b>\n     *\n     * @param duration how long to wait before giving up\n     * @param count elements amount\n     * @param queueNames name of queues\n     * @return the tail elements\n     */",
    "    /**\n     * 从多个队列（含自身）批量拉取队尾元素。\n"
    "     * <p>需要 <b>Redis 7.0.0 及以上</b>。\n"
    "     *\n     * @param duration 最长等待时间\n"
    "     * @param count 每个队列最多拉取数量\n"
    "     * @param queueNames 候选队列名\n"
    "     * @return 队列名到元素列表的映射\n"
    "     */",
)

_POLL_LAST_OFFER_FIRST = (
    "    /**\n     * Retrieves and removes last available tail element of this queue and adds it at the head of <code>queueName</code>,\n     * waiting up to the specified wait time if necessary for an element to become available.\n     *\n     * @param queueName - names of destination queue\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return the tail of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     */",
    "    /**\n     * 从本队列队尾取出元素并插入目标队列队头；必要时阻塞等待。\n"
    "     *\n     * @param queueName 目标队列名\n"
    "     * @param timeout 最长等待时间\n"
    "     * @param unit 时间单位\n"
    "     * @return 被移动的元素；超时为 {@code null}\n"
    "     */",
)

_TAKE_LAST_OFFER_FIRST = (
    "    /**\n     * Retrieves and removes last available tail element of <b>any</b> queue and adds it at the head of <code>queueName</code>,\n     * waiting if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * @param queueName - names of destination queue\n     * @return the tail of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     */",
    "    /**\n     * 从候选队列（含自身）中阻塞取出队尾元素并插入目标队列队头。\n"
    "     *\n     * @param queueName 目标队列名\n"
    "     * @return 被移动的元素\n"
    "     */",
)

_POLL_HEAD = (
    "    /**\n     * Retrieves and removes the head of this queue in async mode, waiting up to the\n     * specified wait time if necessary for an element to become available.\n     *\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     */",
    "    /**\n     * 异步阻塞地从队头取出并移除元素；必要时等待至多 {@code timeout}。\n"
    "     *\n     * @param timeout 最长等待时间\n"
    "     * @param unit 时间单位\n"
    "     * @return 队头元素；超时为 {@code null}\n"
    "     */",
)

_TAKE_HEAD = (
    "    /**\n     * Retrieves and removes the head of this queue in async mode, waiting if necessary\n     * until an element becomes available.\n     *\n     * @return the head of this queue\n     */",
    "    /**\n     * 异步阻塞地从队头取出并移除元素，直至有元素可用。\n"
    "     *\n     * @return 队头元素\n"
    "     */",
)

_PUT = (
    "    /**\n     * Inserts the specified element into this queue in async mode, waiting if necessary\n     * for space to become available.\n     *\n     * @param e the element to add\n     * @throws ClassCastException if the class of the specified element\n     *         prevents it from being added to this queue\n     * @throws NullPointerException if the specified element is null\n     * @throws IllegalArgumentException if some property of the specified\n     *         element prevents it from being added to this queue\n     * @return void\n     */",
    "    /**\n     * 异步阻塞地将元素插入队列；必要时等待直至有可用空间。\n"
    "     *\n     * @param e 待插入元素\n"
    "     * @throws ClassCastException 元素类型不允许加入本队列\n"
    "     * @throws NullPointerException 元素为 {@code null}\n"
    "     * @throws IllegalArgumentException 元素属性不允许加入本队列\n"
    "     */",
)

_MOVE_ASYNC = (
    "    /**\n     * Retrieves and removes the head elements of this queue\n     * and adds them at the tail of <code>queueName</code>.\n     * If necessary waits up to defined <code>timeout</code>\n     * for elements to become available.\n     * Returns moved elements.\n     * <p>\n     * Requires <b>Redis 8.10.0 and higher.</b>\n     *\n     * @param timeout how long to wait before giving up\n     * @param args - arguments object\n     * @return moved elements or empty list if the\n     *         specified waiting time elapses before elements are available\n     */",
    "    /**\n     * 将本队列队头元素批量迁移至目标队列队尾。\n"
    "     * <p>必要时阻塞等待至多 {@code timeout}；返回已迁移元素列表。\n"
    "     * <p>需要 <b>Redis 8.10.0 及以上</b>。\n"
    "     *\n     * @param timeout 最长等待时间\n"
    "     * @param args 迁移参数\n"
    "     * @return 已迁移元素；超时返回空列表\n"
    "     */",
)

_TAKE_ELEMENTS = (
    "    /**\n     * Retrieves and removes continues stream of elements from the head of this queue.\n     * Waits for next element become available.\n     * \n     * @return stream of elements\n     */",
    "    /**\n     * 持续从队头阻塞取元素并移除，形成元素流。\n"
    "     * <p>每次等待下一个元素可用后再发射。\n"
    "     *\n     * @return 元素流\n"
    "     */",
)

# --- RBloomFilter (Highway) shared ---

_BLOOM_CLASS = (
    "/**\n * Distributed implementation of Bloom filter based on Highway 128-bit hash.\n *\n * @author Nikita Koksharov\n *\n * @param <T> - type of object\n */",
    "/**\n"
    " * 基于 Highway 128 位哈希的分布式布隆过滤器 API。\n"
    " * <p>支持添加、批量检测、初始化参数查询及概率计数等操作。\n"
    " *\n"
    " * @author Nikita Koksharov\n"
    " * @param <T> 元素类型\n"
    " */",
)

_BLOOM_ADD_ONE = (
    "    /**\n     * Adds element\n     * \n     * @param object - element to add\n     * @return <code>true</code> if element has been added successfully\n     *         <code>false</code> if element is already present\n     */",
    "    /**\n     * 添加单个元素。\n"
    "     *\n     * @param object 待添加元素\n"
    "     * @return 新插入为 {@code true}，已存在为 {@code false}\n"
    "     */",
)

_BLOOM_ADD_MANY = (
    "    /**\n     * Adds elements\n     *\n     * @param elements elements to add\n     * @return number of added elements\n     */",
    "    /**\n     * 批量添加元素。\n"
    "     *\n     * @param elements 待添加元素集合\n"
    "     * @return 成功添加的数量\n"
    "     */",
)

_BLOOM_CONTAINS_ONE = (
    "    /**\n     * Checks for element presence\n     * \n     * @param object element\n     * @return <code>true</code> if element is present\n     *         <code>false</code> if element is not present\n     */",
    "    /**\n     * 检测单个元素是否可能存在。\n"
    "     *\n     * @param object 待检测元素\n"
    "     * @return 可能存在为 {@code true}，肯定不存在为 {@code false}\n"
    "     */",
)

_BLOOM_CONTAINS_MANY = (
    "    /**\n     * Checks for elements presence\n     *\n     * @param elements elements to check presence\n     * @return number of elements present\n     */",
    "    /**\n     * 批量检测元素是否可能存在。\n"
    "     *\n     * @param elements 待检测元素集合\n"
    "     * @return 判定为可能存在的元素数量\n"
    "     */",
)

_BLOOM_EXISTS = (
    "    /**\n     * Checks multiple elements for existence.\n     * Returns the set of elements that may exist in the filter.\n     * <p>\n     * Elements missing from the returned set definitely do not exist in the filter.\n     *\n     * @param elements elements to check\n     * @return set of elements that may exist\n     */",
    "    /**\n     * 批量检测元素，返回可能存在于过滤器中的元素集合。\n"
    "     * <p>未出现在返回集合中的元素一定不存在。\n"
    "     *\n     * @param elements 待检测元素\n"
    "     * @return 可能存在的元素集合\n"
    "     */",
)

_BLOOM_TRY_INIT = (
    "    /**\n     * Initializes Bloom filter params (size and hashIterations)\n     * calculated from <code>expectedInsertions</code> and <code>falseProbability</code>\n     * Stores config to Redis server.\n     *\n     * @param expectedInsertions - expected amount of insertions per element\n     * @param falseProbability - expected false probability\n     * @return <code>true</code> if Bloom filter initialized\n     *         <code>false</code> if Bloom filter already has been initialized\n     */",
    "    /**\n     * 根据期望插入量与误判率初始化布隆过滤器参数（位数组大小与哈希迭代次数），并写入 Redis。\n"
    "     *\n     * @param expectedInsertions 预期插入元素数量\n"
    "     * @param falseProbability 可接受的误判率\n"
    "     * @return 首次初始化成功为 {@code true}，已初始化过为 {@code false}\n"
    "     */",
)

_BLOOM_GET_EXPECTED = (
    "    /**\n     * Returns expected amount of insertions per element.\n     * Calculated during bloom filter initialization. \n     * \n     * @return expected amount of insertions per element\n     */",
    "    /**\n     * 返回初始化时设定的预期插入元素数量。\n"
    "     *\n     * @return 预期插入量\n"
    "     */",
)

_BLOOM_GET_FALSE_PROB = (
    "    /**\n     * Returns false probability of element presence. \n     * Calculated during bloom filter initialization.\n     * \n     * @return false probability of element presence\n     */",
    "    /**\n     * 返回初始化时设定的误判率。\n"
    "     *\n     * @return 误判率\n"
    "     */",
)

_BLOOM_GET_SIZE = (
    "    /**\n     * Returns number of bits in Redis memory required by this instance\n     * \n     * @return number of bits\n     */",
    "    /**\n     * 返回本实例在 Redis 中占用的位数。\n"
    "     *\n     * @return 位数\n"
    "     */",
)

_BLOOM_GET_HASH_ITER = (
    "    /**\n     * Returns hash iterations amount used per element. \n     * Calculated during bloom filter initialization. \n     * \n     * @return hash iterations amount\n     */",
    "    /**\n     * 返回每个元素使用的哈希迭代次数（初始化时计算）。\n"
    "     *\n     * @return 哈希迭代次数\n"
    "     */",
)

_BLOOM_COUNT = (
    "    /**\n     * Calculates probabilistic number of elements already added to Bloom filter.\n     *\n     * @return probabilistic number of elements\n     */",
    "    /**\n     * 估算已添加到布隆过滤器的元素数量（概率性统计）。\n"
    "     *\n     * @return 估算元素数量\n"
    "     */",
)

_BLOOM_HIGHWAY = [
    _BLOOM_CLASS,
    _BLOOM_ADD_ONE,
    _BLOOM_ADD_MANY,
    _BLOOM_CONTAINS_ONE,
    _BLOOM_CONTAINS_MANY,
    _BLOOM_EXISTS,
    _BLOOM_TRY_INIT,
    _BLOOM_GET_EXPECTED,
    _BLOOM_GET_FALSE_PROB,
    _BLOOM_GET_SIZE,
    _BLOOM_GET_HASH_ITER,
    _BLOOM_COUNT,
]

# --- RBloomFilterNative shared ---

_BLOOM_NATIVE_CLASS = (
    "/**\n * Bloom filter based on BF.* commands\n *\n * @author Su Ko\n *\n * @param <T> - type of object\n */",
    "/**\n"
    " * 基于 Redis {@code BF.*} 命令的原生布隆过滤器 API。\n"
    " * <p>支持 {@code BF.RESERVE/BF.ADD/BF.INSERT/BF.MEXISTS} 及扫描导出等操作。\n"
    " *\n"
    " * @author Su Ko\n"
    " * @param <T> 元素类型\n"
    " */",
)

_BLOOM_NATIVE_ADD_ONE = (
    "    /**\n     * Adds element\n     *\n     * @param element - element to add\n     *\n     * @return <code>true</code> if element has been added successfully\n     *         <code>false</code> if element is already present\n     */",
    "    /**\n     * 添加单个元素。\n"
    "     *\n     * @param element 待添加元素\n"
    "     * @return 新插入为 {@code true}，已存在为 {@code false}\n"
    "     */",
)

_BLOOM_NATIVE_ADD_MANY = (
    "    /**\n     * Adds elements\n     *\n     * @param elements elements to add\n     *\n     * @return set of elements representing whether each element has been added successfully\n     */",
    "    /**\n     * 批量添加元素。\n"
    "     *\n     * @param elements 待添加元素集合\n"
    "     * @return 各元素是否新插入成功的集合\n"
    "     */",
)

_BLOOM_NATIVE_INSERT = (
    "    /**\n     * create filter (if filter is not existing and not NOCREATE mode)\n     * and\n     * Adds elements\n     *\n     * @param args insert args\n     *\n     * @return set of elements representing whether each element has been added successfully\n     */",
    "    /**\n     * 若过滤器不存在且非 NOCREATE 模式则创建，并批量添加元素。\n"
    "     *\n     * @param args 插入参数\n"
    "     * @return 各元素是否新插入成功的集合\n"
    "     */",
)

_BLOOM_NATIVE_INIT_RATE = (
    "    /**\n     * Initializes Bloom filter\n     *\n     * @param errorRate acceptable false positive rate\n     * @param capacity expected number of elements to be added\n     */",
    "    /**\n     * 以误判率与预期容量初始化布隆过滤器。\n"
    "     *\n     * @param errorRate 可接受的误判率\n"
    "     * @param capacity 预期元素数量\n"
    "     */",
)

_BLOOM_NATIVE_INIT_ARGS = (
    "    /**\n     * Initializes Bloom filter\n     *\n     * @param args init args\n     */",
    "    /**\n     * 使用参数对象初始化布隆过滤器。\n"
    "     *\n     * @param args 初始化参数\n"
    "     */",
)

_BLOOM_NATIVE_EXISTS_ONE = (
    "    /**\n     * Checks for element presence\n     *\n     * @param element element\n     *\n     * @return <code>true</code> if element is present\n     *         <code>false</code> if element is not present\n     */",
    "    /**\n     * 检测单个元素是否可能存在。\n"
    "     *\n     * @param element 待检测元素\n"
    "     * @return 可能存在为 {@code true}，肯定不存在为 {@code false}\n"
    "     */",
)

_BLOOM_NATIVE_EXISTS_MANY = (
    "    /**\n     * Checks for elements presence\n     *\n     * @param elements elements to check presence\n     *\n     * @return set of elements representing whether each element is present\n     */",
    "    /**\n     * 批量检测元素是否可能存在。\n"
    "     *\n     * @param elements 待检测元素集合\n"
    "     * @return 各元素是否可能存在的集合\n"
    "     */",
)

_BLOOM_NATIVE_COUNT = (
    "    /**\n     * Returns count of present elements\n     *\n     * @return count of present elements\n     */",
    "    /**\n     * 返回过滤器中可能存在的元素计数。\n"
    "     *\n     * @return 元素计数\n"
    "     */",
)

_BLOOM_NATIVE_GET_INFO = (
    "    /**\n     * Returns Bloom filter information\n     *\n     * @return Bloom filter information\n     */",
    "    /**\n     * 返回布隆过滤器完整信息。\n"
    "     *\n     * @return 过滤器信息对象\n"
    "     */",
)

_BLOOM_NATIVE_GET_INFO_OPT = (
    "    /**\n     * Returns specific Bloom filter information\n     *\n     * @param option information option\n     * @return specific Bloom filter information value\n     */",
    "    /**\n     * 返回指定选项的布隆过滤器信息值。\n"
    "     *\n     * @param option 信息选项\n"
    "     * @return 对应信息值\n"
    "     */",
)

_BLOOM_NATIVE_SCAN = (
    "    /**\n     * Returns ScanDumpInfo\n     * Requires <b>Redis Bloom 1.0.0 and higher.</b>\n     *\n     * @param iterator the iterator returned by the previous call to BF.SCANDUMP.\n     * iteration start from 0\n     *\n     * @return BloomFilterScanDumpInfo\n     */",
    "    /**\n     * 返回扫描导出信息（{@code BF.SCANDUMP}）。\n"
    "     * <p>需要 <b>Redis Bloom 1.0.0 及以上</b>；迭代从 0 开始。\n"
    "     *\n     * @param iterator 上次 {@code BF.SCANDUMP} 返回的迭代器\n"
    "     * @return 扫描导出信息\n"
    "     */",
)

_BLOOM_NATIVE_LOAD = (
    "    /**\n     * Loads chunk\n     * Requires <b>Redis Bloom 1.0.0 and higher.</b>\n     *\n     * @param iterator the iterator returned by the previous call to BF.SCANDUMP.\n     * @param data data to load\n     */",
    "    /**\n     * 加载扫描导出的数据块。\n"
    "     * <p>需要 <b>Redis Bloom 1.0.0 及以上</b>。\n"
    "     *\n     * @param iterator 上次 {@code BF.SCANDUMP} 返回的迭代器\n"
    "     * @param data 待加载的数据\n"
    "     */",
)

_BLOOM_NATIVE = [
    _BLOOM_NATIVE_CLASS,
    _BLOOM_NATIVE_ADD_ONE,
    _BLOOM_NATIVE_ADD_MANY,
    _BLOOM_NATIVE_INSERT,
    _BLOOM_NATIVE_INIT_RATE,
    _BLOOM_NATIVE_INIT_ARGS,
    _BLOOM_NATIVE_EXISTS_ONE,
    _BLOOM_NATIVE_EXISTS_MANY,
    _BLOOM_NATIVE_COUNT,
    _BLOOM_NATIVE_GET_INFO,
    _BLOOM_NATIVE_GET_INFO_OPT,
    _BLOOM_NATIVE_SCAN,
    _BLOOM_NATIVE_LOAD,
]

# --- RBoundedBlockingQueue ---

_BOUNDED_CLASS = (
    "/**\n * Instead, use the {@link RReliableQueue} object with the queue size limit feature.\n *\n * @author Nikita Koksharov\n * @param <V> the type of elements held in this collection\n */",
    "/**\n"
    " * 已废弃：请改用带队列大小限制功能的 {@link RReliableQueue}。\n"
    " *\n"
    " * @author Nikita Koksharov\n"
    " * @param <V> 集合元素类型\n"
    " */",
)

_TRY_SET_CAPACITY = (
    "    /**\n     * Sets queue capacity only if it is not set before.\n     *\n     * @param capacity - queue capacity\n     * @return <code>true</code> if capacity set successfully\n     *         <code>false</code> if capacity already set\n     */",
    "    /**\n     * 仅在尚未设置容量时设置队列容量。\n"
    "     *\n     * @param capacity 队列容量\n"
    "     * @return 设置成功为 {@code true}，容量已存在为 {@code false}\n"
    "     */",
)

_OFFER_ASYNC = (
    "    /**\n     * Inserts the specified element into this queue, waiting up to the\n     * specified wait time if necessary for space to become available.\n     *\n     * @param e the element to add\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return {@code true} if successful, or {@code false} if\n     *         the specified waiting time elapses before space is available\n     * @throws ClassCastException if the class of the specified element\n     *         prevents it from being added to this queue\n     * @throws NullPointerException if the specified element is null\n     */",
    "    /**\n     * 异步尝试将元素插入队列；必要时等待至多 {@code timeout} 直至有可用空间。\n"
    "     *\n     * @param e 待插入元素\n"
    "     * @param timeout 最长等待时间\n"
    "     * @param unit 时间单位\n"
    "     * @return 插入成功为 {@code true}，超时为 {@code false}\n"
    "     * @throws ClassCastException 元素类型不允许加入本队列\n"
    "     * @throws NullPointerException 元素为 {@code null}\n"
    "     */",
)

# --- RBucket shared ---

_BUCKET_CLASS = (
    "/**\n * Object holder. Max size of object is 512MB\n *\n * @author Nikita Koksharov\n *\n * @param <V> - the type of object\n */",
    "/**\n"
    " * 单对象持有者（Bucket）；单个对象最大 512MB。\n"
    " * <p>支持读写、条件设置、过期管理及哈希摘要等操作。\n"
    " *\n"
    " * @author Nikita Koksharov\n"
    " * @param <V> 对象类型\n"
    " */",
)

_BUCKET_ASYNC_CLASS = (
    "/**\n * Async implementation of object holder. Max size of object is 512MB\n *\n * @author Nikita Koksharov\n *\n * @param <V> - the type of object\n */",
    "/**\n"
    " * 单对象持有者（Bucket）的异步 API；单个对象最大 512MB。\n"
    " *\n"
    " * @author Nikita Koksharov\n"
    " * @param <V> 对象类型\n"
    " */",
)

_BUCKET_SYNC = [
    (
        "    /**\n     * Returns size of object in bytes.\n     * \n     * @return object size\n     */",
        "    /**\n     * 返回对象序列化后的字节大小。\n"
        "     *\n     * @return 字节数\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves element stored in the holder.\n     * \n     * @return element\n     */",
        "    /**\n     * 读取持有者中存储的对象。\n"
        "     *\n     * @return 对象值\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves element in the holder and removes it.\n     * \n     * @return element\n     */",
        "    /**\n     * 读取并删除持有者中的对象。\n"
        "     *\n     * @return 对象值\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #setIfAbsent(Object)} instead\n     *\n     * @param value - value to set\n     * @return {@code true} if successful, or {@code false} if\n     *         element was already set\n     */",
        "    /**\n     * 已废弃，请改用 {@link #setIfAbsent(Object)}。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @return 设置成功为 {@code true}，键已存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #setIfAbsent(Object, Duration)} instead\n     * \n     * @param value - value to set\n     * @param timeToLive - time to live interval\n     * @param timeUnit - unit of time to live interval\n     * @return {@code true} if successful, or {@code false} if\n     *         element was already set\n     */",
        "    /**\n     * 已废弃，请改用 {@link #setIfAbsent(Object, Duration)}。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @param timeToLive 存活时间\n"
        "     * @param timeUnit 时间单位\n"
        "     * @return 设置成功为 {@code true}，键已存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Sets value only if object holder doesn't exist.\n     *\n     * @param value - value to set\n     * @return {@code true} if successful, or {@code false} if\n     *         element was already set\n     */",
        "    /**\n     * 仅当持有者不存在时设置值（SET NX）。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @return 设置成功为 {@code true}，键已存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Sets value with defined duration only if object holder doesn't exist.\n     *\n     * @param value value to set\n     * @param duration expiration duration\n     * @return {@code true} if successful, or {@code false} if\n     *         element was already set\n     */",
        "    /**\n     * 仅当持有者不存在时设置值并指定过期时间。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @param duration 过期时长\n"
        "     * @return 设置成功为 {@code true}，键已存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Sets value only if object holder already exists.\n     *\n     * @param value - value to set\n     * @return {@code true} if successful, or {@code false} if\n     *         element wasn't set\n     */",
        "    /**\n     * 仅当持有者已存在时设置值（SET XX）。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @return 设置成功为 {@code true}，键不存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #setIfExists(Object, Duration)} instead\n     *\n     * @param value - value to set\n     * @param timeToLive - time to live interval\n     * @param timeUnit - unit of time to live interval\n     * @return {@code true} if successful, or {@code false} if\n     *         element wasn't set\n     */",
        "    /**\n     * 已废弃，请改用 {@link #setIfExists(Object, Duration)}。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @param timeToLive 存活时间\n"
        "     * @param timeUnit 时间单位\n"
        "     * @return 设置成功为 {@code true}，键不存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Sets <code>value</code> with expiration <code>duration</code> only if object holder already exists.\n     *\n     * @param value value to set\n     * @param duration expiration duration\n     * @return {@code true} if successful, or {@code false} if\n     *         element wasn't set\n     */",
        "    /**\n     * 仅当持有者已存在时设置值并指定过期时间。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @param duration 过期时长\n"
        "     * @return 设置成功为 {@code true}，键不存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Atomically sets the value to the given updated value\n     * only if serialized state of the current value equals \n     * to serialized state of the expected value.\n     *\n     * @param expect the expected value\n     * @param update the new value\n     * @return {@code true} if successful; or {@code false} if the actual value\n     *         was not equal to the expected value.\n     */",
        "    /**\n     * 原子比较并设置：当前值序列化结果与期望值相等时写入新值。\n"
        "     *\n     * @param expect 期望值\n"
        "     * @param update 新值\n"
        "     * @return 成功为 {@code true}，期望值不匹配为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Conditionally deletes the bucket based on value comparison.\n     * <p>\n     * <ul>\n     *    <li> {@link CompareAndDeleteArgs#expected(Object)} - compatible with any Redis/Valkey version</li>\n     *    <li> {@link CompareAndDeleteArgs#unexpected(Object)} - compatible with any Redis/Valkey version</li>\n     *    <li> {@link CompareAndDeleteArgs#expectedDigest(String)} - requires Redis 8.4+</li>\n     *    <li> {@link CompareAndDeleteArgs#unexpectedDigest(String)} - requires Redis 8.4+</li>\n     * </ul>\n     *\n     * @param args comparison arguments\n     * @return {@code true} if bucket was deleted, {@code false} otherwise\n     */",
        "    /**\n     * 按值比较条件删除 Bucket。\n"
        "     * <ul>\n"
        "     *    <li>{@link CompareAndDeleteArgs#expected(Object)} — 任意 Redis/Valkey 版本</li>\n"
        "     *    <li>{@link CompareAndDeleteArgs#unexpected(Object)} — 任意 Redis/Valkey 版本</li>\n"
        "     *    <li>{@link CompareAndDeleteArgs#expectedDigest(String)} — 需 Redis 8.4+</li>\n"
        "     *    <li>{@link CompareAndDeleteArgs#unexpectedDigest(String)} — 需 Redis 8.4+</li>\n"
        "     * </ul>\n"
        "     *\n     * @param args 比较参数\n"
        "     * @return 删除成功为 {@code true}，否则 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves current element in the holder and replaces it with <code>newValue</code>. \n     * \n     * @param newValue - value to set\n     * @return previous value\n     */",
        "    /**\n     * 读取当前值并用 {@code newValue} 替换。\n"
        "     *\n     * @param newValue 新值\n"
        "     * @return 替换前的值\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #getAndSet(Object, Duration)} instead\n     * \n     * @param value - value to set\n     * @param timeToLive - time to live interval\n     * @param timeUnit - unit of time to live interval\n     * @return previous value\n     */",
        "    /**\n     * 已废弃，请改用 {@link #getAndSet(Object, Duration)}。\n"
        "     *\n     * @param value 新值\n"
        "     * @param timeToLive 存活时间\n"
        "     * @param timeUnit 时间单位\n"
        "     * @return 替换前的值\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves current element in the holder and replaces it\n     * with <code>value</code> with defined expiration <code>duration</code>.\n     *\n     * @param value value to set\n     * @param duration expiration duration\n     * @return previous value\n     */",
        "    /**\n     * 读取当前值并替换为新值，同时设置过期时间。\n"
        "     *\n     * @param value 新值\n"
        "     * @param duration 过期时长\n"
        "     * @return 替换前的值\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves current element in the holder and sets an expiration duration for it.\n     * <p>\n     * Requires <b>Redis 6.2.0 and higher.</b>\n     *\n     * @param duration of object time to live interval\n     * @return element\n     */",
        "    /**\n     * 读取当前值并为其设置过期时长。\n"
        "     * <p>需要 <b>Redis 6.2.0 及以上</b>。\n"
        "     *\n     * @param duration 存活时长\n"
        "     * @return 当前值\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves current element in the holder and sets an expiration date for it.\n     * <p>\n     * Requires <b>Redis 6.2.0 and higher.</b>\n     *\n     * @param time of exact object expiration moment\n     * @return element\n     */",
        "    /**\n     * 读取当前值并设置绝对过期时刻。\n"
        "     * <p>需要 <b>Redis 6.2.0 及以上</b>。\n"
        "     *\n     * @param time 过期时刻\n"
        "     * @return 当前值\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves current element in the holder and clears expiration date set before.\n     * <p>\n     * Requires <b>Redis 6.2.0 and higher.</b>\n     *\n     * @return element\n     */",
        "    /**\n     * 读取当前值并清除已设置的过期时间。\n"
        "     * <p>需要 <b>Redis 6.2.0 及以上</b>。\n"
        "     *\n     * @return 当前值\n"
        "     */",
    ),
    (
        "    /**\n     * Stores element into the holder. \n     * \n     * @param value - value to set\n     */",
        "    /**\n     * 将对象写入持有者。\n"
        "     *\n     * @param value 待设置的值\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #set(Object, Duration)} instead\n     * \n     * @param value - value to set\n     * @param timeToLive - time to live interval\n     * @param timeUnit - unit of time to live interval\n     */",
        "    /**\n     * 已废弃，请改用 {@link #set(Object, Duration)}。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @param timeToLive 存活时间\n"
        "     * @param timeUnit 时间单位\n"
        "     */",
    ),
    (
        "    /**\n     * Stores <code>value</code> into the holder with defined expiration <code>duration</code>.\n     *\n     * @param value value to set\n     * @param duration expiration duration\n     */",
        "    /**\n     * 写入对象并设置过期时长。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @param duration 过期时长\n"
        "     */",
    ),
    (
        "    /**\n     * Set value and keep existing TTL.\n     * <p>\n     * Requires <b>Redis 6.0.0 and higher.</b>\n     *\n     * @param value - value to set\n     */",
        "    /**\n     * 设置新值并保留原有 TTL。\n"
        "     * <p>需要 <b>Redis 6.0.0 及以上</b>。\n"
        "     *\n     * @param value 待设置的值\n"
        "     */",
    ),
    (
        "    /**\n     * Adds object event listener\n     *\n     * @see org.redisson.api.listener.TrackingListener\n     * @see org.redisson.api.ExpiredObjectListener\n     * @see org.redisson.api.DeletedObjectListener\n     * @see org.redisson.api.listener.SetObjectListener\n     *\n     * @param listener - object event listener\n     * @return listener id\n     */",
        "    /**\n     * 注册对象事件监听器。\n"
        "     *\n     * @see org.redisson.api.listener.TrackingListener\n"
        "     * @see org.redisson.api.ExpiredObjectListener\n"
        "     * @see org.redisson.api.DeletedObjectListener\n"
        "     * @see org.redisson.api.listener.SetObjectListener\n"
        "     *\n     * @param listener 事件监听器\n"
        "     * @return 监听器 ID\n"
        "     */",
    ),
    (
        "    /**\n     * Returns the common part of the data stored in this bucket\n     * and a bucket defined by the <code>name</code>\n     *\n     * @param name second bucket\n     * @return common part of the data\n     */",
        "    /**\n     * 返回本 Bucket 与指定名称 Bucket 存储数据的公共前缀部分。\n"
        "     *\n     * @param name 另一 Bucket 名称\n"
        "     * @return 公共数据部分\n"
        "     */",
    ),
    (
        "    /**\n     * Returns the length of the common part of the data stored in this bucket\n     * and a bucket defined by the <code>name</code>\n     *\n     * @param name second bucket\n     * @return common part of the data\n     */",
        "    /**\n     * 返回本 Bucket 与指定名称 Bucket 公共前缀部分的字节长度。\n"
        "     *\n     * @param name 另一 Bucket 名称\n"
        "     * @return 公共部分长度\n"
        "     */",
    ),
    (
        "    /**\n     * Returns the hash digest of the value stored in this bucket as a hexadecimal string.\n     * The digest is computed using the XXH3 hash algorithm.\n     * <p>\n     * Requires <b>Redis 8.4.0 or higher</b>.\n     *\n     * @return hash digest as hexadecimal string, or {@code null} if the bucket doesn't exist\n     */",
        "    /**\n     * 返回 Bucket 值的 XXH3 哈希摘要（十六进制字符串）。\n"
        "     * <p>需要 <b>Redis 8.4.0 及以上</b>。\n"
        "     *\n     * @return 哈希摘要；Bucket 不存在时为 {@code null}\n"
        "     */",
    ),
]

_BUCKET_ASYNC = [
    (
        "    /**\n     * Returns size of object in bytes\n     * \n     * @return object size\n     */",
        "    /**\n     * 返回对象序列化后的字节大小。\n"
        "     *\n     * @return 字节数\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves element stored in the holder.\n     * \n     * @return element\n     */",
        "    /**\n     * 读取持有者中存储的对象。\n"
        "     *\n     * @return 对象值\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves element in the holder and removes it.\n     * \n     * @return element\n     */",
        "    /**\n     * 读取并删除持有者中的对象。\n"
        "     *\n     * @return 对象值\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #setIfAbsentAsync(Object)} instead\n     * \n     * @param value - value to set\n     * @return {@code true} if successful, or {@code false} if\n     *         element was already set\n     */",
        "    /**\n     * 已废弃，请改用 {@link #setIfAbsentAsync(Object)}。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @return 设置成功为 {@code true}，键已存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #setIfAbsentAsync(Object, Duration)} instead\n     * \n     * @param value - value to set\n     * @param timeToLive - time to live interval\n     * @param timeUnit - unit of time to live interval\n     * @return {@code true} if successful, or {@code false} if\n     *         element was already set\n     */",
        "    /**\n     * 已废弃，请改用 {@link #setIfAbsentAsync(Object, Duration)}。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @param timeToLive 存活时间\n"
        "     * @param timeUnit 时间单位\n"
        "     * @return 设置成功为 {@code true}，键已存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Sets value only if object holder doesn't exist.\n     *\n     * @param value - value to set\n     * @return {@code true} if successful, or {@code false} if\n     *         element was already set\n     */",
        "    /**\n     * 仅当持有者不存在时设置值（SET NX）。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @return 设置成功为 {@code true}，键已存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Sets value with defined duration only if object holder doesn't exist.\n     *\n     * @param value value to set\n     * @param duration expiration duration\n     * @return {@code true} if successful, or {@code false} if\n     *         element was already set\n     */",
        "    /**\n     * 仅当持有者不存在时设置值并指定过期时间。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @param duration 过期时长\n"
        "     * @return 设置成功为 {@code true}，键已存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Sets value only if it's already exists.\n     *\n     * @param value - value to set\n     * @return {@code true} if successful, or {@code false} if\n     *         element wasn't set\n     */",
        "    /**\n     * 仅当持有者已存在时设置值（SET XX）。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @return 设置成功为 {@code true}，键不存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #setIfExistsAsync(Object, Duration)} instead\n     *\n     * @param value - value to set\n     * @param timeToLive - time to live interval\n     * @param timeUnit - unit of time to live interval\n     * @return {@code true} if successful, or {@code false} if\n     *         element wasn't set\n     */",
        "    /**\n     * 已废弃，请改用 {@link #setIfExistsAsync(Object, Duration)}。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @param timeToLive 存活时间\n"
        "     * @param timeUnit 时间单位\n"
        "     * @return 设置成功为 {@code true}，键不存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Sets <code>value</code> with expiration <code>duration</code> only if object holder already exists.\n     *\n     * @param value value to set\n     * @param duration expiration duration\n     * @return {@code true} if successful, or {@code false} if\n     *         element wasn't set\n     */",
        "    /**\n     * 仅当持有者已存在时设置值并指定过期时间。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @param duration 过期时长\n"
        "     * @return 设置成功为 {@code true}，键不存在为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Atomically sets the value to the given updated value\n     * only if serialized state of the current value equals \n     * to serialized state of the expected value.\n     *\n     * @param expect the expected value\n     * @param update the new value\n     * @return {@code true} if successful; or {@code false} if the actual value\n     *         was not equal to the expected value.\n     */",
        "    /**\n     * 原子比较并设置：当前值序列化结果与期望值相等时写入新值。\n"
        "     *\n     * @param expect 期望值\n"
        "     * @param update 新值\n"
        "     * @return 成功为 {@code true}，期望值不匹配为 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Conditionally deletes the bucket based on value comparison.\n     * <p>\n     * <ul>\n     *    <li> {@link CompareAndDeleteArgs#expected(Object)} - compatible with any Redis/Valkey version</li>\n     *    <li> {@link CompareAndDeleteArgs#unexpected(Object)} - compatible with any Redis/Valkey version</li>\n     *    <li> {@link CompareAndDeleteArgs#expectedDigest(String)} - requires Redis 8.4+</li>\n     *    <li> {@link CompareAndDeleteArgs#unexpectedDigest(String)} - requires Redis 8.4+</li>\n     * </ul>\n     *\n     * @param args comparison arguments\n     * @return {@code true} if bucket was deleted, {@code false} otherwise\n     */",
        "    /**\n     * 按值比较条件删除 Bucket。\n"
        "     * <ul>\n"
        "     *    <li>{@link CompareAndDeleteArgs#expected(Object)} — 任意 Redis/Valkey 版本</li>\n"
        "     *    <li>{@link CompareAndDeleteArgs#unexpected(Object)} — 任意 Redis/Valkey 版本</li>\n"
        "     *    <li>{@link CompareAndDeleteArgs#expectedDigest(String)} — 需 Redis 8.4+</li>\n"
        "     *    <li>{@link CompareAndDeleteArgs#unexpectedDigest(String)} — 需 Redis 8.4+</li>\n"
        "     * </ul>\n"
        "     *\n     * @param args 比较参数\n"
        "     * @return 删除成功为 {@code true}，否则 {@code false}\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves current element in the holder and replaces it with <code>newValue</code>. \n     * \n     * @param newValue - value to set\n     * @return previous value\n     */",
        "    /**\n     * 读取当前值并用 {@code newValue} 替换。\n"
        "     *\n     * @param newValue 新值\n"
        "     * @return 替换前的值\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #getAndSetAsync(Object, Duration)} instead\n     * \n     * @param value - value to set\n     * @param timeToLive - time to live interval\n     * @param timeUnit - unit of time to live interval\n     * @return previous value\n     */",
        "    /**\n     * 已废弃，请改用 {@link #getAndSetAsync(Object, Duration)}。\n"
        "     *\n     * @param value 新值\n"
        "     * @param timeToLive 存活时间\n"
        "     * @param timeUnit 时间单位\n"
        "     * @return 替换前的值\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves current element in the holder and replaces it\n     * with <code>value</code> with defined expiration <code>duration</code>.\n     *\n     * @param value value to set\n     * @param duration expiration duration\n     * @return previous value\n     */",
        "    /**\n     * 读取当前值并替换为新值，同时设置过期时间。\n"
        "     *\n     * @param value 新值\n"
        "     * @param duration 过期时长\n"
        "     * @return 替换前的值\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves current element in the holder and sets an expiration duration for it.\n     * <p>\n     * Requires <b>Redis 6.2.0 and higher.</b>\n     *\n     * @param duration of object time to live interval\n     * @return element\n     */",
        "    /**\n     * 读取当前值并为其设置过期时长。\n"
        "     * <p>需要 <b>Redis 6.2.0 及以上</b>。\n"
        "     *\n     * @param duration 存活时长\n"
        "     * @return 当前值\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves current element in the holder and sets an expiration date for it.\n     * <p>\n     * Requires <b>Redis 6.2.0 and higher.</b>\n     *\n     * @param time of exact object expiration moment\n     * @return element\n     */",
        "    /**\n     * 读取当前值并设置绝对过期时刻。\n"
        "     * <p>需要 <b>Redis 6.2.0 及以上</b>。\n"
        "     *\n     * @param time 过期时刻\n"
        "     * @return 当前值\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves current element in the holder and clears expiration date set before.\n     * <p>\n     * Requires <b>Redis 6.2.0 and higher.</b>\n     *\n     * @return element\n     */",
        "    /**\n     * 读取当前值并清除已设置的过期时间。\n"
        "     * <p>需要 <b>Redis 6.2.0 及以上</b>。\n"
        "     *\n     * @return 当前值\n"
        "     */",
    ),
    (
        "    /**\n     * Stores element into the holder. \n     * \n     * @param value - value to set\n     * @return void\n     */",
        "    /**\n     * 将对象写入持有者。\n"
        "     *\n     * @param value 待设置的值\n"
        "     */",
    ),
    (
        "    /**\n     * Use {@link #setAsync(Object, Duration)} instead\n     * \n     * @param value - value to set\n     * @param timeToLive - time to live interval\n     * @param timeUnit - unit of time to live interval\n     * @return void\n     */",
        "    /**\n     * 已废弃，请改用 {@link #setAsync(Object, Duration)}。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @param timeToLive 存活时间\n"
        "     * @param timeUnit 时间单位\n"
        "     */",
    ),
    (
        "    /**\n     * Stores <code>value</code> into the holder with defined expiration <code>duration</code>.\n     *\n     * @param value value to set\n     * @param duration expiration duration\n     */",
        "    /**\n     * 写入对象并设置过期时长。\n"
        "     *\n     * @param value 待设置的值\n"
        "     * @param duration 过期时长\n"
        "     */",
    ),
    (
        "    /**\n     * Set value and keep existing TTL.\n     * <p>\n     * Requires <b>Redis 6.0.0 and higher.</b>\n     *\n     * @param value - value to set\n     * @return void\n     */",
        "    /**\n     * 设置新值并保留原有 TTL。\n"
        "     * <p>需要 <b>Redis 6.0.0 及以上</b>。\n"
        "     *\n     * @param value 待设置的值\n"
        "     */",
    ),
    (
        "    /**\n     * Adds object event listener\n     *\n     * @see org.redisson.api.listener.TrackingListener\n     * @see org.redisson.api.ExpiredObjectListener\n     * @see org.redisson.api.DeletedObjectListener\n     * @see org.redisson.api.listener.SetObjectListener\n     *\n     * @param listener - object event listener\n     * @return listener id\n     */",
        "    /**\n     * 注册对象事件监听器。\n"
        "     *\n     * @see org.redisson.api.listener.TrackingListener\n"
        "     * @see org.redisson.api.ExpiredObjectListener\n"
        "     * @see org.redisson.api.DeletedObjectListener\n"
        "     * @see org.redisson.api.listener.SetObjectListener\n"
        "     *\n     * @param listener 事件监听器\n"
        "     * @return 监听器 ID\n"
        "     */",
    ),
    (
        "    /**\n     * Returns the common part of the data stored in this bucket\n     * and a bucket defined by the <code>name</code>\n     *\n     * @param name second bucket\n     * @return common part of the data\n     */",
        "    /**\n     * 返回本 Bucket 与指定名称 Bucket 存储数据的公共前缀部分。\n"
        "     *\n     * @param name 另一 Bucket 名称\n"
        "     * @return 公共数据部分\n"
        "     */",
    ),
    (
        "    /**\n     * Returns the length of the common part of the data stored in this bucket\n     * and a bucket defined by the <code>name</code>\n     *\n     * @param name second bucket\n     * @return common part of the data\n     */",
        "    /**\n     * 返回本 Bucket 与指定名称 Bucket 公共前缀部分的字节长度。\n"
        "     *\n     * @param name 另一 Bucket 名称\n"
        "     * @return 公共部分长度\n"
        "     */",
    ),
    (
        "    /**\n     * Returns the hash digest of the value stored in this bucket as a hexadecimal string.\n     * The digest is computed using the XXH3 hash algorithm.\n     * <p>\n     * Requires <b>Redis 8.4.0 or higher</b>.\n     *\n     * @return hash digest as hexadecimal string, or {@code null} if the bucket doesn't exist\n     */",
        "    /**\n     * 返回 Bucket 值的 XXH3 哈希摘要（十六进制字符串）。\n"
        "     * <p>需要 <b>Redis 8.4.0 及以上</b>。\n"
        "     *\n     * @return 哈希摘要；Bucket 不存在时为 {@code null}\n"
        "     */",
    ),
]

_BUCKET_CAS = (
    "    /**\n     * Atomically sets the value if the condition specified in args is met.\n     * <p>\n     * Supports multiple comparison modes:\n     * <ul>\n     *   <li>{@link CompareAndSetArgs#expected(Object)} - compatible with any Redis/Valkey version</li>\n     *   <li>{@link CompareAndSetArgs#unexpected(Object)} - compatible with any Redis/Valkey version</li>\n     *   <li>{@link CompareAndSetArgs#expectedDigest(String)} - requires Redis 8.4+, uses SET IFDEQ</li>\n     *   <li>{@link CompareAndSetArgs#unexpectedDigest(String)} - requires Redis 8.4+, uses SET IFDNE</li>\n     * </ul>\n     * <p>\n     * Example usage:\n     * <pre>\n     * // Set new value if current value equals expected value\n     * bucket.compareAndSet(CompareAndSetArgs.&lt;String&gt;expected(\"oldValue\").set(\"newValue\"));\n     *\n     * // Set new value with TTL if current value does not equal unexpected value\n     * bucket.compareAndSet(CompareAndSetArgs.&lt;String&gt;unexpected(\"badValue\")\n     *     .set(\"newValue\")\n     *     .timeToLive(Duration.ofMinutes(5)));\n     *\n     * // Set new value if hash digest matches (Redis 8.4+)\n     * bucket.compareAndSet(CompareAndSetArgs.&lt;String&gt;expectedDigest(\"b6acb9d84a38ff74\")\n     *     .set(\"newValue\")\n     *     .expireAt(Instant.now().plusSeconds(3600)));\n     * </pre>\n     *\n     * @param args compare-and-set arguments containing condition and new value\n     * @return {@code true} if successful, {@code false} if condition was not met\n     */",
    "    /**\n     * 按 {@code args} 指定条件原子设置值。\n"
    "     * <p>支持多种比较模式：\n"
    "     * <ul>\n"
    "     *   <li>{@link CompareAndSetArgs#expected(Object)} — 任意 Redis/Valkey 版本</li>\n"
    "     *   <li>{@link CompareAndSetArgs#unexpected(Object)} — 任意 Redis/Valkey 版本</li>\n"
    "     *   <li>{@link CompareAndSetArgs#expectedDigest(String)} — 需 Redis 8.4+，使用 SET IFDEQ</li>\n"
    "     *   <li>{@link CompareAndSetArgs#unexpectedDigest(String)} — 需 Redis 8.4+，使用 SET IFDNE</li>\n"
    "     * </ul>\n"
    "     * <p>示例：\n"
    "     * <pre>\n"
    "     * bucket.compareAndSet(CompareAndSetArgs.&lt;String&gt;expected(\"oldValue\").set(\"newValue\"));\n"
    "     * bucket.compareAndSet(CompareAndSetArgs.&lt;String&gt;unexpected(\"badValue\")\n"
    "     *     .set(\"newValue\").timeToLive(Duration.ofMinutes(5)));\n"
    "     * bucket.compareAndSet(CompareAndSetArgs.&lt;String&gt;expectedDigest(\"b6acb9d84a38ff74\")\n"
    "     *     .set(\"newValue\").expireAt(Instant.now().plusSeconds(3600)));\n"
    "     * </pre>\n"
    "     *\n     * @param args 比较并设置参数\n"
    "     * @return 条件满足并成功为 {@code true}，否则 {@code false}\n"
    "     */",
)

_BUCKET_CAS_ASYNC = (
    "    /**\n     * Atomically sets the value if the condition specified in args is met.\n     * <p>\n     * Supports multiple comparison modes:\n     * <ul>\n     *   <li>{@link CompareAndSetArgs#expected(Object)} - compatible with any Redis/Valkey version</li>\n     *   <li>{@link CompareAndSetArgs#unexpected(Object)} - compatible with any Redis/Valkey version</li>\n     *   <li>{@link CompareAndSetArgs#expectedDigest(String)} - requires Redis 8.4+, uses SET IFDEQ</li>\n     *   <li>{@link CompareAndSetArgs#unexpectedDigest(String)} - requires Redis 8.4+, uses SET IFDNE</li>\n     * </ul>\n     *\n     * @param args compare-and-set arguments containing condition and new value\n     * @return {@code true} if successful, {@code false} if condition was not met\n     */",
    "    /**\n     * 按 {@code args} 指定条件原子设置值。\n"
    "     * <ul>\n"
    "     *   <li>{@link CompareAndSetArgs#expected(Object)} — 任意 Redis/Valkey 版本</li>\n"
    "     *   <li>{@link CompareAndSetArgs#unexpected(Object)} — 任意 Redis/Valkey 版本</li>\n"
    "     *   <li>{@link CompareAndSetArgs#expectedDigest(String)} — 需 Redis 8.4+，使用 SET IFDEQ</li>\n"
    "     *   <li>{@link CompareAndSetArgs#unexpectedDigest(String)} — 需 Redis 8.4+，使用 SET IFDNE</li>\n"
    "     * </ul>\n"
    "     *\n     * @param args 比较并设置参数\n"
    "     * @return 条件满足并成功为 {@code true}，否则 {@code false}\n"
    "     */",
)

# --- RBlockingQueueAsync ---

_blocking_queue_async = [
    (
        "/**\n * Distributed async implementation of {@link BlockingQueue}\n *\n * @author Nikita Koksharov\n * @param <V> the type of elements held in this collection\n */",
        "/**\n"
        " * {@link BlockingQueue} 的分布式异步 API。\n"
        " * <p>提供阻塞 poll/take/put、跨队列拉取及批量迁移等异步操作。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 集合元素类型\n"
        " */",
    ),
    (
        "    /**\n     * Retrieves and removes first available head element of <b>any</b> queue in async mode,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * @param queueNames - queue names. Queue name itself is always included\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return Future object with the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     */",
        "    /**\n     * 异步地从指定队列集合（含自身）中拉取首个可用队头元素并移除。\n"
        "     *\n     * @param queueNames 候选队列名（自身始终参与）\n"
        "     * @param timeout 最长等待时间\n"
        "     * @param unit 时间单位\n"
        "     * @return 队头元素；超时为 {@code null}\n"
        "     */",
    ),
    (
        "    /**\n     * Retrieves and removes first available head element of <b>any</b> queue in async mode,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * @param queueNames - queue names. Queue name itself is always included\n     * @param timeout how long to wait before giving up\n     * @return Future object with the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     */",
        "    /**\n     * 异步地从指定队列集合（含自身）中拉取首个可用队头元素，并返回元素及其来源队列名。\n"
        "     *\n     * @param queueNames 候选队列名（自身始终参与）\n"
        "     * @param timeout 最长等待时间\n"
        "     * @return 队列名与元素的 {@link Entry}；超时为 {@code null}\n"
        "     */",
    ),
    _POLL_FIRST_BATCH,
    _POLL_LAST_BATCH,
    (
        "    /**\n     * Retrieves and removes first available tail element of <b>any</b> queue in async mode,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * @param queueNames - queue names. Queue name itself is always included\n     * @param timeout    how long to wait before giving up\n     * @return Future object with the tail of this queue, or {@code null} if the\n     * specified waiting time elapses before an element is available\n     */",
        "    /**\n     * 异步地从指定队列集合（含自身）中拉取首个可用队尾元素，并返回元素及其来源队列名。\n"
        "     *\n     * @param queueNames 候选队列名（自身始终参与）\n"
        "     * @param timeout 最长等待时间\n"
        "     * @return 队列名与元素的 {@link Entry}；超时为 {@code null}\n"
        "     */",
    ),
    _DRAIN_TO_MAX,
    _DRAIN_TO_ALL,
    _POLL_LAST_OFFER_FIRST,
    _TAKE_LAST_OFFER_FIRST,
    _POLL_HEAD,
    _TAKE_HEAD,
    _PUT,
    _MOVE_ASYNC,
]
_add(f"{_API}RBlockingQueueAsync.java", _blocking_queue_async)

# --- RBlockingQueueReactive ---

_blocking_queue_reactive = [
    (
        "/**\n * Reactive interface for BlockingQueue object\n *\n * @author Nikita Koksharov\n * @param <V> the type of elements held in this collection\n */",
        "/**\n"
        " * {@link BlockingQueue} 的响应式 API（Project Reactor）。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 集合元素类型\n"
        " */",
    ),
    (
        "    /**\n     * Retrieves and removes first available head element of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue own.\n     *\n     * @param queueNames - names of queue\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return Mono object with the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     */",
        "    /**\n     * 响应式地从指定队列集合（含自身）中拉取首个可用队头元素并移除。\n"
        "     *\n     * @param queueNames 候选队列名列表\n"
        "     * @param timeout 最长等待时间\n"
        "     * @param unit 时间单位\n"
        "     * @return 队头元素；超时为 {@code null}\n"
        "     */",
    ),
    _POLL_FIRST_ANY_NAME,
    _POLL_LAST_ANY_NAME,
    _POLL_FIRST_BATCH,
    _POLL_LAST_BATCH,
    _DRAIN_TO_MAX,
    _DRAIN_TO_ALL,
    _POLL_LAST_OFFER_FIRST,
    _POLL_HEAD,
    _TAKE_LAST_OFFER_FIRST,
    _TAKE_HEAD,
    _PUT,
    _TAKE_ELEMENTS,
]
_add(f"{_API}RBlockingQueueReactive.java", _blocking_queue_reactive)

# --- RBlockingQueueRx ---

_blocking_queue_rx = [
    (
        "/**\n * RxJava2 interface for BlockingQueue\n *\n * @author Nikita Koksharov\n * @param <V> the type of elements held in this collection\n */",
        "/**\n"
        " * {@link BlockingQueue} 的 RxJava 响应式 API。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " * @param <V> 集合元素类型\n"
        " */",
    ),
    (
        "    /**\n     * Retrieves and removes first available head element of <b>any</b> queue,\n     * waiting up to the specified wait time if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue own.\n     *\n     * @param queueNames - names of queue\n     * @param timeout how long to wait before giving up, in units of\n     *        {@code unit}\n     * @param unit a {@code TimeUnit} determining how to interpret the\n     *        {@code timeout} parameter\n     * @return Flowable object with the head of this queue, or {@code null} if the\n     *         specified waiting time elapses before an element is available\n     */",
        "    /**\n     * 响应式地从指定队列集合（含自身）中拉取首个可用队头元素并移除。\n"
        "     *\n     * @param queueNames 候选队列名列表\n"
        "     * @param timeout 最长等待时间\n"
        "     * @param unit 时间单位\n"
        "     * @return 队头元素；超时为 {@code null}\n"
        "     */",
    ),
    _POLL_FIRST_ANY_NAME,
    _POLL_LAST_ANY_NAME,
    _POLL_FIRST_BATCH,
    _POLL_LAST_BATCH,
    _DRAIN_TO_MAX,
    _DRAIN_TO_ALL,
    _POLL_LAST_OFFER_FIRST,
    _POLL_HEAD,
    (
        "    /**\n     * Retrieves and removes last available tail element of <b>any</b> queue and adds it at the head of <code>queueName</code>,\n     * waiting if necessary for an element to become available\n     * in any of defined queues <b>including</b> queue itself.\n     *\n     * @param queueName - names of destination queue\n     * @return the tail of this queue\n     */",
        "    /**\n     * 从候选队列（含自身）中阻塞取出队尾元素并插入目标队列队头。\n"
        "     *\n     * @param queueName 目标队列名\n"
        "     * @return 被移动的元素\n"
        "     */",
    ),
    _TAKE_HEAD,
    _PUT,
    _TAKE_ELEMENTS,
]
_add(f"{_API}RBlockingQueueRx.java", _blocking_queue_rx)

# --- RBloomFilter family ---

for _name in (
    "RBloomFilter.java",
    "RBloomFilterAsync.java",
    "RBloomFilterReactive.java",
    "RBloomFilterRx.java",
):
    _add(f"{_API}{_name}", _BLOOM_HIGHWAY)

for _name in (
    "RBloomFilterNative.java",
    "RBloomFilterNativeAsync.java",
    "RBloomFilterNativeReactive.java",
    "RBloomFilterNativeRx.java",
):
    _add(f"{_API}{_name}", _BLOOM_NATIVE)

# --- RBoundedBlockingQueue ---

_add(
    f"{_API}RBoundedBlockingQueue.java",
    [_BOUNDED_CLASS, _TRY_SET_CAPACITY],
)
_add(
    f"{_API}RBoundedBlockingQueueAsync.java",
    [_BOUNDED_CLASS, _TRY_SET_CAPACITY, _OFFER_ASYNC],
)

# --- RBucket ---

_add(
    f"{_API}RBucket.java",
    [_BUCKET_CLASS, _BUCKET_CAS, *_BUCKET_SYNC],
)
_add(
    f"{_API}RBucketAsync.java",
    [_BUCKET_ASYNC_CLASS, _BUCKET_CAS_ASYNC, *_BUCKET_ASYNC],
)
