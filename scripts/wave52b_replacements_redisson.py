"""Chinese annotation replacements for Redisson 4.7.0 wave-52b api [15:30]."""
from __future__ import annotations

_A = "redisson/src/main/java/org/redisson/api/"

W52B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    f"{_A}array/ArrayGrepArgs.java": [
        (
            "/**\n * Arguments object for array grep operation.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * 数组 grep 搜索操作的参数对象。\n *\n * @author lamnt2008\n *\n */",
        ),
        (
            "/**\n     * Defines exact match predicate.\n     *\n     * @param value value\n     * @return arguments object\n     */",
            "/**\n     * 定义精确匹配谓词。\n     *\n     * @param value 匹配值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines exact match predicate.\n     *\n     * @param value value\n     * @return arguments object\n     */",
            "/**\n     * 定义精确匹配谓词。\n     *\n     * @param value 匹配值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines substring match predicate.\n     *\n     * @param value value\n     * @return arguments object\n     */",
            "/**\n     * 定义子串匹配谓词。\n     *\n     * @param value 匹配值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines substring match predicate.\n     *\n     * @param value value\n     * @return arguments object\n     */",
            "/**\n     * 定义子串匹配谓词。\n     *\n     * @param value 匹配值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines glob match predicate.\n     *\n     * @param pattern pattern\n     * @return arguments object\n     */",
            "/**\n     * 定义 glob 通配符匹配谓词。\n     *\n     * @param pattern 匹配模式\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines glob match predicate.\n     *\n     * @param pattern pattern\n     * @return arguments object\n     */",
            "/**\n     * 定义 glob 通配符匹配谓词。\n     *\n     * @param pattern 匹配模式\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines regular expression match predicate.\n     *\n     * @param pattern pattern\n     * @return arguments object\n     */",
            "/**\n     * 定义正则表达式匹配谓词。\n     *\n     * @param pattern 匹配模式\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines regular expression match predicate.\n     *\n     * @param pattern pattern\n     * @return arguments object\n     */",
            "/**\n     * 定义正则表达式匹配谓词。\n     *\n     * @param pattern 匹配模式\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines predicates to be combined with AND.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 定义以 AND 逻辑组合的谓词。\n     *\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines predicates to be combined with OR.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 定义以 OR 逻辑组合的谓词。\n     *\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines matches limit.\n     *\n     * @param value limit value\n     * @return arguments object\n     */",
            "/**\n     * 定义匹配结果数量上限。\n     *\n     * @param value 上限值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines case-insensitive matching.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 定义不区分大小写的匹配。\n     *\n     * @return 参数对象\n     */",
        ),
    ],
    "ArrayGrepArgs.java": [
        (
            "/**\n * Arguments object for array grep operation.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * 数组 grep 搜索操作的参数对象。\n *\n * @author lamnt2008\n *\n */",
        ),
        (
            "/**\n     * Defines exact match predicate.\n     *\n     * @param value value\n     * @return arguments object\n     */",
            "/**\n     * 定义精确匹配谓词。\n     *\n     * @param value 匹配值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines exact match predicate.\n     *\n     * @param value value\n     * @return arguments object\n     */",
            "/**\n     * 定义精确匹配谓词。\n     *\n     * @param value 匹配值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines substring match predicate.\n     *\n     * @param value value\n     * @return arguments object\n     */",
            "/**\n     * 定义子串匹配谓词。\n     *\n     * @param value 匹配值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines substring match predicate.\n     *\n     * @param value value\n     * @return arguments object\n     */",
            "/**\n     * 定义子串匹配谓词。\n     *\n     * @param value 匹配值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines glob match predicate.\n     *\n     * @param pattern pattern\n     * @return arguments object\n     */",
            "/**\n     * 定义 glob 通配符匹配谓词。\n     *\n     * @param pattern 匹配模式\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines glob match predicate.\n     *\n     * @param pattern pattern\n     * @return arguments object\n     */",
            "/**\n     * 定义 glob 通配符匹配谓词。\n     *\n     * @param pattern 匹配模式\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines regular expression match predicate.\n     *\n     * @param pattern pattern\n     * @return arguments object\n     */",
            "/**\n     * 定义正则表达式匹配谓词。\n     *\n     * @param pattern 匹配模式\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines regular expression match predicate.\n     *\n     * @param pattern pattern\n     * @return arguments object\n     */",
            "/**\n     * 定义正则表达式匹配谓词。\n     *\n     * @param pattern 匹配模式\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines predicates to be combined with AND.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 定义以 AND 逻辑组合的谓词。\n     *\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines predicates to be combined with OR.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 定义以 OR 逻辑组合的谓词。\n     *\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines matches limit.\n     *\n     * @param value limit value\n     * @return arguments object\n     */",
            "/**\n     * 定义匹配结果数量上限。\n     *\n     * @param value 上限值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines case-insensitive matching.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 定义不区分大小写的匹配。\n     *\n     * @return 参数对象\n     */",
        ),
    ],
    f"{_A}array/ArrayGrepParams.java": [
        (
            "/**\n * Array grep arguments implementation.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * {@link ArrayGrepArgs} 的默认实现，\n * 封装数组 grep 的谓词、组合方式与匹配选项。\n *\n * @author lamnt2008\n *\n */",
        ),
    ],
    "ArrayGrepParams.java": [
        (
            "/**\n * Array grep arguments implementation.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * {@link ArrayGrepArgs} 的默认实现，\n * 封装数组 grep 的谓词、组合方式与匹配选项。\n *\n * @author lamnt2008\n *\n */",
        ),
    ],
    f"{_A}array/ArrayInfo.java": [
        (
            "/**\n * Array information object.\n * <p>\n * Holds the information that is always available for an array. Extended\n * statistics that are only populated when full information is requested are\n * exposed by {@link ArrayFullInfo}.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * 数组信息对象。\n * <p>\n * 保存数组始终可用的基础信息；仅在请求完整信息时才填充的扩展统计见 {@link ArrayFullInfo}。\n *\n * @author lamnt2008\n *\n */",
        ),
        (
            "/**\n     * Returns number of values stored in array.\n     *\n     * @return number of values\n     */",
            "/**\n     * 返回数组中存储的元素个数。\n     *\n     * @return 元素个数\n     */",
        ),
        (
            "/**\n     * Returns array length.\n     *\n     * @return array length\n     */",
            "/**\n     * 返回数组长度。\n     *\n     * @return 数组长度\n     */",
        ),
        (
            "/**\n     * Returns next array index used by array insert operations.\n     *\n     * @return next array index used by array insert operations\n     */",
            "/**\n     * 返回数组插入操作将使用的下一个索引。\n     *\n     * @return 下一次插入使用的索引\n     */",
        ),
        (
            "/**\n     * Returns number of slices.\n     *\n     * @return number of slices\n     */",
            "/**\n     * 返回切片数量。\n     *\n     * @return 切片数量\n     */",
        ),
        (
            "/**\n     * Returns directory size.\n     *\n     * @return directory size\n     */",
            "/**\n     * 返回目录大小。\n     *\n     * @return 目录大小\n     */",
        ),
        (
            "/**\n     * Returns number of super directory entries.\n     *\n     * @return number of super directory entries\n     */",
            "/**\n     * 返回超级目录项数量。\n     *\n     * @return 超级目录项数量\n     */",
        ),
        (
            "/**\n     * Returns slice size.\n     *\n     * @return slice size\n     */",
            "/**\n     * 返回切片大小。\n     *\n     * @return 切片大小\n     */",
        ),
    ],
    "ArrayInfo.java": [
        (
            "/**\n * Array information object.\n * <p>\n * Holds the information that is always available for an array. Extended\n * statistics that are only populated when full information is requested are\n * exposed by {@link ArrayFullInfo}.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * 数组信息对象。\n * <p>\n * 保存数组始终可用的基础信息；仅在请求完整信息时才填充的扩展统计见 {@link ArrayFullInfo}。\n *\n * @author lamnt2008\n *\n */",
        ),
        (
            "/**\n     * Returns number of values stored in array.\n     *\n     * @return number of values\n     */",
            "/**\n     * 返回数组中存储的元素个数。\n     *\n     * @return 元素个数\n     */",
        ),
        (
            "/**\n     * Returns array length.\n     *\n     * @return array length\n     */",
            "/**\n     * 返回数组长度。\n     *\n     * @return 数组长度\n     */",
        ),
        (
            "/**\n     * Returns next array index used by array insert operations.\n     *\n     * @return next array index used by array insert operations\n     */",
            "/**\n     * 返回数组插入操作将使用的下一个索引。\n     *\n     * @return 下一次插入使用的索引\n     */",
        ),
        (
            "/**\n     * Returns number of slices.\n     *\n     * @return number of slices\n     */",
            "/**\n     * 返回切片数量。\n     *\n     * @return 切片数量\n     */",
        ),
        (
            "/**\n     * Returns directory size.\n     *\n     * @return directory size\n     */",
            "/**\n     * 返回目录大小。\n     *\n     * @return 目录大小\n     */",
        ),
        (
            "/**\n     * Returns number of super directory entries.\n     *\n     * @return number of super directory entries\n     */",
            "/**\n     * 返回超级目录项数量。\n     *\n     * @return 超级目录项数量\n     */",
        ),
        (
            "/**\n     * Returns slice size.\n     *\n     * @return slice size\n     */",
            "/**\n     * 返回切片大小。\n     *\n     * @return 切片大小\n     */",
        ),
    ],
    f"{_A}atomic/BaseIncrementArgs.java": [
        (
            "/**\n * Base arguments for extended atomic increment operations.\n *\n * @author lamnt2008\n *\n * @param <T> arguments type\n */",
            "/**\n * 扩展原子递增操作的基础参数接口。\n *\n * @author lamnt2008\n *\n * @param <T> 参数对象类型\n */",
        ),
        (
            "/**\n     * Caps the increment result at the lower or upper bound (or the type\n     * limits when no explicit bound is given) instead of rejecting it.\n     * <p>\n     * Without this option, an out-of-bounds result leaves the value and its\n     * expiration unchanged, and the current value is returned.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 将递增结果限制在下界或上界（未显式指定时使用类型极限）内，\n     * 而非拒绝本次操作。\n     * <p>\n     * 未启用此选项时，越界结果会保持当前值与过期时间不变，并返回当前值。\n     *\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines the specified expiration time.\n     *\n     * @param ttl time to live duration\n     * @return arguments object\n     */",
            "/**\n     * 设置指定的过期时间（TTL）。\n     *\n     * @param ttl 存活时长\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines the specified Unix time at which the key will expire.\n     *\n     * @param time expire date\n     * @return arguments object\n     */",
            "/**\n     * 设置键将在指定 Unix 时间点过期。\n     *\n     * @param time 过期时间\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines removal of the existing expiration.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 移除现有过期时间，使键持久化。\n     *\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines expiration setting only if the key doesn't have an expiration.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 仅在键尚未设置过期时间时才应用过期配置。\n     *\n     * @return 参数对象\n     */",
        ),
    ],
    "BaseIncrementArgs.java": [
        (
            "/**\n * Base arguments for extended atomic increment operations.\n *\n * @author lamnt2008\n *\n * @param <T> arguments type\n */",
            "/**\n * 扩展原子递增操作的基础参数接口。\n *\n * @author lamnt2008\n *\n * @param <T> 参数对象类型\n */",
        ),
        (
            "/**\n     * Caps the increment result at the lower or upper bound (or the type\n     * limits when no explicit bound is given) instead of rejecting it.\n     * <p>\n     * Without this option, an out-of-bounds result leaves the value and its\n     * expiration unchanged, and the current value is returned.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 将递增结果限制在下界或上界（未显式指定时使用类型极限）内，\n     * 而非拒绝本次操作。\n     * <p>\n     * 未启用此选项时，越界结果会保持当前值与过期时间不变，并返回当前值。\n     *\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines the specified expiration time.\n     *\n     * @param ttl time to live duration\n     * @return arguments object\n     */",
            "/**\n     * 设置指定的过期时间（TTL）。\n     *\n     * @param ttl 存活时长\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines the specified Unix time at which the key will expire.\n     *\n     * @param time expire date\n     * @return arguments object\n     */",
            "/**\n     * 设置键将在指定 Unix 时间点过期。\n     *\n     * @param time 过期时间\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines removal of the existing expiration.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 移除现有过期时间，使键持久化。\n     *\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines expiration setting only if the key doesn't have an expiration.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 仅在键尚未设置过期时间时才应用过期配置。\n     *\n     * @return 参数对象\n     */",
        ),
    ],
    f"{_A}atomic/BaseIncrementParams.java": [
        (
            "/**\n * Base increment arguments implementation.\n *\n * @author lamnt2008\n *\n * @param <T> arguments type\n */",
            "/**\n * {@link BaseIncrementArgs} 的基类实现，保存饱和、过期与持久化等通用参数。\n *\n * @author lamnt2008\n *\n * @param <T> 参数对象类型\n */",
        ),
    ],
    "BaseIncrementParams.java": [
        (
            "/**\n * Base increment arguments implementation.\n *\n * @author lamnt2008\n *\n * @param <T> arguments type\n */",
            "/**\n * {@link BaseIncrementArgs} 的基类实现，保存饱和、过期与持久化等通用参数。\n *\n * @author lamnt2008\n *\n * @param <T> 参数对象类型\n */",
        ),
    ],
    f"{_A}atomic/CompareAndDeleteArgs.java": [
        (
            "/**\n * Arguments for {@link org.redisson.api.RAtomicLong#compareAndDelete(CompareAndDeleteArgs)}\n * and {@link org.redisson.api.RAtomicDouble#compareAndDelete(CompareAndDeleteArgs)} methods.\n * Defines conditions for conditional deletion of atomic value.\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * {@link org.redisson.api.RAtomicLong#compareAndDelete(CompareAndDeleteArgs)}\n * 与 {@link org.redisson.api.RAtomicDouble#compareAndDelete(CompareAndDeleteArgs)} 的参数对象；\n * 定义按数值条件删除原子值的规则。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Deletes entry if stored value is less than specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值小于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is less than specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值小于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is less than or equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值小于或等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is less than or equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值小于或等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is greater than specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值大于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is greater than specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值大于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is greater than or equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值大于或等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is greater than or equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值大于或等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is not equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值不等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is not equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值不等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
    ],
    "CompareAndDeleteArgs.java": [
        (
            "/**\n * Arguments for {@link org.redisson.api.RAtomicLong#compareAndDelete(CompareAndDeleteArgs)}\n * and {@link org.redisson.api.RAtomicDouble#compareAndDelete(CompareAndDeleteArgs)} methods.\n * Defines conditions for conditional deletion of atomic value.\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * {@link org.redisson.api.RAtomicLong#compareAndDelete(CompareAndDeleteArgs)}\n * 与 {@link org.redisson.api.RAtomicDouble#compareAndDelete(CompareAndDeleteArgs)} 的参数对象；\n * 定义按数值条件删除原子值的规则。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
        (
            "/**\n     * Deletes entry if stored value is less than specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值小于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is less than specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值小于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is less than or equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值小于或等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is less than or equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值小于或等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is greater than specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值大于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is greater than specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值大于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is greater than or equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值大于或等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is greater than or equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值大于或等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is not equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值不等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Deletes entry if stored value is not equal to specified value.\n     *\n     * @param value threshold value\n     * @return arguments object\n     */",
            "/**\n     * 当存储值不等于指定阈值时删除条目。\n     *\n     * @param value 阈值\n     * @return 参数对象\n     */",
        ),
    ],
    f"{_A}atomic/ComparisonCondition.java": [
        (
            "/**\n * Comparison condition used for numeric compare-and-delete operations.\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 数值比较并删除操作使用的比较条件。\n * <p>\n * 各常量对应 Lua 侧的比较运算符。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
    ],
    "ComparisonCondition.java": [
        (
            "/**\n * Comparison condition used for numeric compare-and-delete operations.\n *\n * @author Nikita Koksharov\n *\n */",
            "/**\n * 数值比较并删除操作使用的比较条件。\n * <p>\n * 各常量对应 Lua 侧的比较运算符。\n *\n * @author Nikita Koksharov\n *\n */",
        ),
    ],
    f"{_A}atomic/DoubleIncrementArgs.java": [
        (
            "/**\n * Arguments for extended atomic double increment operations.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * 扩展原子 double 递增操作的参数接口。\n *\n * @author lamnt2008\n *\n */",
        ),
        (
            "/**\n     * Defines default increment by {@code 1}.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 使用默认递增量 {@code 1}。\n     *\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines increment value.\n     *\n     * @param increment increment value\n     * @return arguments object\n     */",
            "/**\n     * 设置递增量。\n     *\n     * @param increment 递增量\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines lower bound for increment result.\n     *\n     * @param value lower bound value\n     * @return arguments object\n     */",
            "/**\n     * 设置递增结果的下界。\n     *\n     * @param value 下界值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines upper bound for increment result.\n     *\n     * @param value upper bound value\n     * @return arguments object\n     */",
            "/**\n     * 设置递增结果的上界。\n     *\n     * @param value 上界值\n     * @return 参数对象\n     */",
        ),
    ],
    "DoubleIncrementArgs.java": [
        (
            "/**\n * Arguments for extended atomic double increment operations.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * 扩展原子 double 递增操作的参数接口。\n *\n * @author lamnt2008\n *\n */",
        ),
        (
            "/**\n     * Defines default increment by {@code 1}.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 使用默认递增量 {@code 1}。\n     *\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines increment value.\n     *\n     * @param increment increment value\n     * @return arguments object\n     */",
            "/**\n     * 设置递增量。\n     *\n     * @param increment 递增量\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines lower bound for increment result.\n     *\n     * @param value lower bound value\n     * @return arguments object\n     */",
            "/**\n     * 设置递增结果的下界。\n     *\n     * @param value 下界值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines upper bound for increment result.\n     *\n     * @param value upper bound value\n     * @return arguments object\n     */",
            "/**\n     * 设置递增结果的上界。\n     *\n     * @param value 上界值\n     * @return 参数对象\n     */",
        ),
    ],
    f"{_A}atomic/DoubleIncrementParams.java": [
        (
            "/**\n * Double increment arguments implementation.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * {@link DoubleIncrementArgs} 的默认实现，保存递增量与上下界配置。\n *\n * @author lamnt2008\n *\n */",
        ),
    ],
    "DoubleIncrementParams.java": [
        (
            "/**\n * Double increment arguments implementation.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * {@link DoubleIncrementArgs} 的默认实现，保存递增量与上下界配置。\n *\n * @author lamnt2008\n *\n */",
        ),
    ],
    f"{_A}atomic/LongIncrementArgs.java": [
        (
            "/**\n * Arguments for extended atomic long increment operations.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * 扩展原子 long 递增操作的参数接口。\n *\n * @author lamnt2008\n *\n */",
        ),
        (
            "/**\n     * Defines default increment by {@code 1}.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 使用默认递增量 {@code 1}。\n     *\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines increment value.\n     *\n     * @param increment increment value\n     * @return arguments object\n     */",
            "/**\n     * 设置递增量。\n     *\n     * @param increment 递增量\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines lower bound for increment result.\n     *\n     * @param value lower bound value\n     * @return arguments object\n     */",
            "/**\n     * 设置递增结果的下界。\n     *\n     * @param value 下界值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines upper bound for increment result.\n     *\n     * @param value upper bound value\n     * @return arguments object\n     */",
            "/**\n     * 设置递增结果的上界。\n     *\n     * @param value 上界值\n     * @return 参数对象\n     */",
        ),
    ],
    "LongIncrementArgs.java": [
        (
            "/**\n * Arguments for extended atomic long increment operations.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * 扩展原子 long 递增操作的参数接口。\n *\n * @author lamnt2008\n *\n */",
        ),
        (
            "/**\n     * Defines default increment by {@code 1}.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 使用默认递增量 {@code 1}。\n     *\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines increment value.\n     *\n     * @param increment increment value\n     * @return arguments object\n     */",
            "/**\n     * 设置递增量。\n     *\n     * @param increment 递增量\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines lower bound for increment result.\n     *\n     * @param value lower bound value\n     * @return arguments object\n     */",
            "/**\n     * 设置递增结果的下界。\n     *\n     * @param value 下界值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Defines upper bound for increment result.\n     *\n     * @param value upper bound value\n     * @return arguments object\n     */",
            "/**\n     * 设置递增结果的上界。\n     *\n     * @param value 上界值\n     * @return 参数对象\n     */",
        ),
    ],
    f"{_A}atomic/LongIncrementParams.java": [
        (
            "/**\n * Long increment arguments implementation.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * {@link LongIncrementArgs} 的默认实现，保存递增量与上下界配置。\n *\n * @author lamnt2008\n *\n */",
        ),
    ],
    "LongIncrementParams.java": [
        (
            "/**\n * Long increment arguments implementation.\n *\n * @author lamnt2008\n *\n */",
            "/**\n * {@link LongIncrementArgs} 的默认实现，保存递增量与上下界配置。\n *\n * @author lamnt2008\n *\n */",
        ),
    ],
    f"{_A}bitset/BitFieldArgs.java": [
        (
            "/**\n * Arguments object for BITFIELD command.\n *\n * @author Su Ko\n *\n */",
            "/**\n * BITFIELD 命令的参数对象。\n *\n * @author Su Ko\n *\n */",
        ),
        (
            "/**\n     * Creates an empty arguments object.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 创建空的 BITFIELD 参数对象。\n     *\n     * @return 参数对象\n     */",
        ),
    ],
    "BitFieldArgs.java": [
        (
            "/**\n * Arguments object for BITFIELD command.\n *\n * @author Su Ko\n *\n */",
            "/**\n * BITFIELD 命令的参数对象。\n *\n * @author Su Ko\n *\n */",
        ),
        (
            "/**\n     * Creates an empty arguments object.\n     *\n     * @return arguments object\n     */",
            "/**\n     * 创建空的 BITFIELD 参数对象。\n     *\n     * @return 参数对象\n     */",
        ),
    ],
    f"{_A}bitset/BitFieldInitArgs.java": [
        (
            "/**\n * Arguments object for BITFIELD command.\n *\n * @author Su Ko\n *\n */",
            "/**\n * BITFIELD 命令的初始化参数接口，用于链式构建子命令序列。\n *\n * @author Su Ko\n *\n */",
        ),
        (
            "/**\n     * Adds OVERFLOW subcommand.\n     * Sets overflow behavior for subsequent SET/INCRBY operations until the next OVERFLOW.\n     *\n     * @param overflow overflow behavior\n     * @return arguments object\n     */",
            "/**\n     * 添加 OVERFLOW 子命令；\n     * 为后续 SET/INCRBY 操作设置溢出行为，直至下一次 OVERFLOW。\n     *\n     * @param overflow 溢出处理方式\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Adds GET subcommand for signed value.\n     * Returns the value stored at the given encoding/offset.\n     *\n     * @param size size of signed number up to 64 bits\n     * @param offset offset created by {@link BitOffset#bit(long)} or {@link BitOffset#index(long)}\n     * @return arguments object\n     */",
            "/**\n     * 添加 GET 子命令，读取有符号值。\n     * 返回指定编码/偏移处存储的值。\n     *\n     * @param size 有符号数位数，最多 64 位\n     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Adds GET subcommand for unsigned value.\n     * Returns the value stored at the given encoding/offset.\n     *\n     * @param size size of unsigned number up to 63 bits\n     * @param offset offset created by {@link BitOffset#bit(long)} or {@link BitOffset#index(long)}\n     * @return arguments object\n     */",
            "/**\n     * 添加 GET 子命令，读取无符号值。\n     * 返回指定编码/偏移处存储的值。\n     *\n     * @param size 无符号数位数，最多 63 位\n     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Adds SET subcommand for signed value.\n     * Sets the value and returns the previous value\n     * may return null if OVERFLOW FAIL is set.\n     *\n     * @param size size of signed number up to 64 bits\n     * @param offset offset created by {@link BitOffset#bit(long)} or {@link BitOffset#index(long)}\n     * @param value value to set\n     * @return arguments object\n     */",
            "/**\n     * 添加 SET 子命令，写入有符号值并返回旧值；\n     * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。\n     *\n     * @param size 有符号数位数，最多 64 位\n     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移\n     * @param value 要写入的值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Adds SET subcommand for unsigned value.\n     * Sets the value and returns the previous value\n     * may return null if OVERFLOW FAIL is set.\n     *\n     * @param size size of unsigned number up to 63 bits\n     * @param offset offset created by {@link BitOffset#bit(long)} or {@link BitOffset#index(long)}\n     * @param value value to set\n     * @return arguments object\n     */",
            "/**\n     * 添加 SET 子命令，写入无符号值并返回旧值；\n     * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。\n     *\n     * @param size 无符号数位数，最多 63 位\n     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移\n     * @param value 要写入的值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Adds INCRBY subcommand for signed value.\n     * Increments by the given amount and returns the new value\n     * may return null if OVERFLOW FAIL is set.\n     *\n     * @param size size of signed number up to 64 bits\n     * @param offset offset created by {@link BitOffset#bit(long)} or {@link BitOffset#index(long)}\n     * @param increment increment value\n     * @return arguments object\n     */",
            "/**\n     * 添加 INCRBY 子命令，对有符号值按给定量递增并返回新值；\n     * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。\n     *\n     * @param size 有符号数位数，最多 64 位\n     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移\n     * @param increment 递增量\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Adds INCRBY subcommand for unsigned value.\n     * Increments by the given amount and returns the new value\n     * may return null if OVERFLOW FAIL is set.\n     *\n     * @param size size of unsigned number up to 63 bits\n     * @param offset offset created by {@link BitOffset#bit(long)} or {@link BitOffset#index(long)}\n     * @param increment increment value\n     * @return arguments object\n     */",
            "/**\n     * 添加 INCRBY 子命令，对无符号值按给定量递增并返回新值；\n     * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。\n     *\n     * @param size 无符号数位数，最多 63 位\n     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移\n     * @param increment 递增量\n     * @return 参数对象\n     */",
        ),
    ],
    "BitFieldInitArgs.java": [
        (
            "/**\n * Arguments object for BITFIELD command.\n *\n * @author Su Ko\n *\n */",
            "/**\n * BITFIELD 命令的初始化参数接口，用于链式构建子命令序列。\n *\n * @author Su Ko\n *\n */",
        ),
        (
            "/**\n     * Adds OVERFLOW subcommand.\n     * Sets overflow behavior for subsequent SET/INCRBY operations until the next OVERFLOW.\n     *\n     * @param overflow overflow behavior\n     * @return arguments object\n     */",
            "/**\n     * 添加 OVERFLOW 子命令；\n     * 为后续 SET/INCRBY 操作设置溢出行为，直至下一次 OVERFLOW。\n     *\n     * @param overflow 溢出处理方式\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Adds GET subcommand for signed value.\n     * Returns the value stored at the given encoding/offset.\n     *\n     * @param size size of signed number up to 64 bits\n     * @param offset offset created by {@link BitOffset#bit(long)} or {@link BitOffset#index(long)}\n     * @return arguments object\n     */",
            "/**\n     * 添加 GET 子命令，读取有符号值。\n     * 返回指定编码/偏移处存储的值。\n     *\n     * @param size 有符号数位数，最多 64 位\n     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Adds GET subcommand for unsigned value.\n     * Returns the value stored at the given encoding/offset.\n     *\n     * @param size size of unsigned number up to 63 bits\n     * @param offset offset created by {@link BitOffset#bit(long)} or {@link BitOffset#index(long)}\n     * @return arguments object\n     */",
            "/**\n     * 添加 GET 子命令，读取无符号值。\n     * 返回指定编码/偏移处存储的值。\n     *\n     * @param size 无符号数位数，最多 63 位\n     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Adds SET subcommand for signed value.\n     * Sets the value and returns the previous value\n     * may return null if OVERFLOW FAIL is set.\n     *\n     * @param size size of signed number up to 64 bits\n     * @param offset offset created by {@link BitOffset#bit(long)} or {@link BitOffset#index(long)}\n     * @param value value to set\n     * @return arguments object\n     */",
            "/**\n     * 添加 SET 子命令，写入有符号值并返回旧值；\n     * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。\n     *\n     * @param size 有符号数位数，最多 64 位\n     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移\n     * @param value 要写入的值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Adds SET subcommand for unsigned value.\n     * Sets the value and returns the previous value\n     * may return null if OVERFLOW FAIL is set.\n     *\n     * @param size size of unsigned number up to 63 bits\n     * @param offset offset created by {@link BitOffset#bit(long)} or {@link BitOffset#index(long)}\n     * @param value value to set\n     * @return arguments object\n     */",
            "/**\n     * 添加 SET 子命令，写入无符号值并返回旧值；\n     * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。\n     *\n     * @param size 无符号数位数，最多 63 位\n     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移\n     * @param value 要写入的值\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Adds INCRBY subcommand for signed value.\n     * Increments by the given amount and returns the new value\n     * may return null if OVERFLOW FAIL is set.\n     *\n     * @param size size of signed number up to 64 bits\n     * @param offset offset created by {@link BitOffset#bit(long)} or {@link BitOffset#index(long)}\n     * @param increment increment value\n     * @return arguments object\n     */",
            "/**\n     * 添加 INCRBY 子命令，对有符号值按给定量递增并返回新值；\n     * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。\n     *\n     * @param size 有符号数位数，最多 64 位\n     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移\n     * @param increment 递增量\n     * @return 参数对象\n     */",
        ),
        (
            "/**\n     * Adds INCRBY subcommand for unsigned value.\n     * Increments by the given amount and returns the new value\n     * may return null if OVERFLOW FAIL is set.\n     *\n     * @param size size of unsigned number up to 63 bits\n     * @param offset offset created by {@link BitOffset#bit(long)} or {@link BitOffset#index(long)}\n     * @param increment increment value\n     * @return arguments object\n     */",
            "/**\n     * 添加 INCRBY 子命令，对无符号值按给定量递增并返回新值；\n     * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。\n     *\n     * @param size 无符号数位数，最多 63 位\n     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移\n     * @param increment 递增量\n     * @return 参数对象\n     */",
        ),
    ],
    f"{_A}bitset/BitFieldOverflow.java": [
        (
            "/**\n * Overflow behavior for BITFIELD command.\n *\n * @author Su Ko\n *\n */",
            "/**\n * BITFIELD 命令的溢出处理方式。\n *\n * @author Su Ko\n *\n */",
        ),
        (
            "/**\n     * Wrap around on overflow/underflow. Uses modular arithmetic for unsigned and wraps signed ranges (default).\n     */",
            "/**\n     * 溢出/下溢时回绕；无符号使用模运算，有符号在取值范围内回绕（默认）。\n     */",
        ),
        (
            "/**\n     * Saturate to min/max on overflow/underflow.\n     * Clamps to the closest boundary instead of wrapping.\n     */",
            "/**\n     * 溢出/下溢时饱和到最小/最大值；\n     * 钳制到最近边界而非回绕。\n     */",
        ),
        (
            "/**\n     * Return null on overflow/underflow.\n     * No change is applied when overflow/underflow is detected.\n     */",
            "/**\n     * 溢出/下溢时返回 null；\n     * 检测到溢出/下溢时不修改原值。\n     */",
        ),
    ],
    "BitFieldOverflow.java": [
        (
            "/**\n * Overflow behavior for BITFIELD command.\n *\n * @author Su Ko\n *\n */",
            "/**\n * BITFIELD 命令的溢出处理方式。\n *\n * @author Su Ko\n *\n */",
        ),
        (
            "/**\n     * Wrap around on overflow/underflow. Uses modular arithmetic for unsigned and wraps signed ranges (default).\n     */",
            "/**\n     * 溢出/下溢时回绕；无符号使用模运算，有符号在取值范围内回绕（默认）。\n     */",
        ),
        (
            "/**\n     * Saturate to min/max on overflow/underflow.\n     * Clamps to the closest boundary instead of wrapping.\n     */",
            "/**\n     * 溢出/下溢时饱和到最小/最大值；\n     * 钳制到最近边界而非回绕。\n     */",
        ),
        (
            "/**\n     * Return null on overflow/underflow.\n     * No change is applied when overflow/underflow is detected.\n     */",
            "/**\n     * 溢出/下溢时返回 null；\n     * 检测到溢出/下溢时不修改原值。\n     */",
        ),
    ],
    f"{_A}bitset/BitFieldParams.java": [
        (
            "/**\n * Parameters for BITFIELD command.\n *\n * @author Su Ko\n *\n */",
            "/**\n * {@link BitFieldArgs} 与 {@link BitFieldInitArgs} 的默认实现，\n * 按顺序保存 BITFIELD 子命令操作列表。\n *\n * @author Su Ko\n *\n */",
        ),
        (
            "/**\n         * GET subcommand.\n         * Returns the value stored at the given encoding/offset.\n         */",
            "/**\n         * GET 子命令：读取指定编码/偏移处的值。\n         */",
        ),
        (
            "/**\n         * SET subcommand.\n         * Sets the value and returns the previous value\n         * may return null if OVERFLOW FAIL is set.\n         */",
            "/**\n         * SET 子命令：写入值并返回旧值；\n         * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。\n         */",
        ),
        (
            "/**\n         * INCRBY subcommand.\n         * Increments by the given amount and returns the new value\n         * may return null if OVERFLOW FAIL is set.\n         */",
            "/**\n         * INCRBY 子命令：按给定量递增并返回新值；\n         * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。\n         */",
        ),
        (
            "/**\n         * OVERFLOW subcommand.\n         * Sets overflow behavior for subsequent SET/INCRBY operations until the next OVERFLOW.\n         */",
            "/**\n         * OVERFLOW 子命令：为后续 SET/INCRBY 设置溢出行为，直至下一次 OVERFLOW。\n         */",
        ),
    ],
    "BitFieldParams.java": [
        (
            "/**\n * Parameters for BITFIELD command.\n *\n * @author Su Ko\n *\n */",
            "/**\n * {@link BitFieldArgs} 与 {@link BitFieldInitArgs} 的默认实现，\n * 按顺序保存 BITFIELD 子命令操作列表。\n *\n * @author Su Ko\n *\n */",
        ),
        (
            "/**\n         * GET subcommand.\n         * Returns the value stored at the given encoding/offset.\n         */",
            "/**\n         * GET 子命令：读取指定编码/偏移处的值。\n         */",
        ),
        (
            "/**\n         * SET subcommand.\n         * Sets the value and returns the previous value\n         * may return null if OVERFLOW FAIL is set.\n         */",
            "/**\n         * SET 子命令：写入值并返回旧值；\n         * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。\n         */",
        ),
        (
            "/**\n         * INCRBY subcommand.\n         * Increments by the given amount and returns the new value\n         * may return null if OVERFLOW FAIL is set.\n         */",
            "/**\n         * INCRBY 子命令：按给定量递增并返回新值；\n         * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。\n         */",
        ),
        (
            "/**\n         * OVERFLOW subcommand.\n         * Sets overflow behavior for subsequent SET/INCRBY operations until the next OVERFLOW.\n         */",
            "/**\n         * OVERFLOW 子命令：为后续 SET/INCRBY 设置溢出行为，直至下一次 OVERFLOW。\n         */",
        ),
    ],
}
