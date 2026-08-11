// SelectBox.js — 双栏多选框的选项缓存、过滤与左右移动
"use strict";
{
    const getOptionGroupName = (option) => option.parentElement.label;
    // 管理 filtered horizontal/vertical 多选 widget 的选项状态
    const SelectBox = {
        cache: {},
        // 从原生 select 元素初始化选项缓存
        init: function (id) {
            const box = document.getElementById(id);
            SelectBox.cache[id] = [];
            const cache = SelectBox.cache[id];
            for (const node of box.options) {
                const group = getOptionGroupName(node);
                cache.push({
                    group,
                    value: node.value,
                    text: node.text,
                    displayed: 1,
                });
            }
            // Only sort if there are any groups (to preserve existing behavior for non-grouped selects)
            if (cache.some((item) => item.group)) {
                SelectBox.sort(id);
            }
        },
        // 根据缓存重建 select 的 option/optgroup DOM
        redisplay: function (id) {
            // Repopulate HTML select box from cache
            const box = document.getElementById(id);
            const scroll_value_from_top = box.scrollTop;
            box.innerHTML = "";
            let node = box;
            let currentOptgroup = null;
            for (const option of SelectBox.cache[id]) {
                if (option.displayed) {
                    // Create a new optgroup when the group changes
                    if (option.group && option.group !== currentOptgroup) {
                        currentOptgroup = option.group;
                        node = document.createElement("optgroup");
                        node.setAttribute("label", option.group);
                        box.appendChild(node);
                    } else if (!option.group && currentOptgroup !== null) {
                        // Back to ungrouped options
                        currentOptgroup = null;
                        node = box;
                    }
                    const new_option = new Option(
                        option.text,
                        option.value,
                        false,
                        false,
                    );
                    // Shows a tooltip when hovering over the option
                    new_option.title = option.text;
                    node.appendChild(new_option);
                }
            }
            box.scrollTop = scroll_value_from_top;
        },
        // 按空格分词 AND 匹配过滤可见选项
        filter: function (id, text) {
            // Redisplay the HTML select box, displaying only the choices containing ALL
            // the words in text. (It's an AND search.)
            const tokens = text.toLowerCase().split(/\s+/);
            for (const node of SelectBox.cache[id]) {
                node.displayed = 1;
                const node_text = node.text.toLowerCase();
                for (const token of tokens) {
                    if (!node_text.includes(token)) {
                        node.displayed = 0;
                        break; // Once the first token isn't found we're done
                    }
                }
            }
            SelectBox.redisplay(id);
        },
        get_hidden_node_count(id) {
            const cache = SelectBox.cache[id] || [];
            return cache.filter((node) => node.displayed === 0).length;
        },
        delete_from_cache: function (id, value) {
            let delete_index = null;
            const cache = SelectBox.cache[id];
            for (const [i, node] of cache.entries()) {
                if (node.value === value) {
                    delete_index = i;
                    break;
                }
            }
            cache.splice(delete_index, 1);
        },
        add_to_cache: function (id, option) {
            SelectBox.cache[id].push({
                group: option.group,
                value: option.value,
                text: option.text,
                displayed: 1,
            });
        },
        cache_contains: function (id, value) {
            // Check if an item is contained in the cache
            for (const node of SelectBox.cache[id]) {
                if (node.value === value) {
                    return true;
                }
            }
            return false;
        },
        // 将 from 中选中的项移入 to 的缓存
        move: function (from, to) {
            const from_box = document.getElementById(from);
            for (const option of from_box.options) {
                const option_value = option.value;
                if (
                    option.selected &&
                    SelectBox.cache_contains(from, option_value)
                ) {
                    const group = getOptionGroupName(option);
                    SelectBox.add_to_cache(to, {
                        group,
                        value: option_value,
                        text: option.text,
                        displayed: 1,
                    });
                    SelectBox.delete_from_cache(from, option_value);
                }
            }
            // Only sort if there are any groups (to preserve existing behavior for non-grouped selects)
            if (SelectBox.cache[to].some((item) => item.group)) {
                SelectBox.sort(to);
            }
            SelectBox.redisplay(from);
            SelectBox.redisplay(to);
        },
        // 将 from 中全部项移入 to
        move_all: function (from, to) {
            const from_box = document.getElementById(from);
            for (const option of from_box.options) {
                const option_value = option.value;
                if (SelectBox.cache_contains(from, option_value)) {
                    const group = getOptionGroupName(option);
                    SelectBox.add_to_cache(to, {
                        group,
                        value: option_value,
                        text: option.text,
                        displayed: 1,
                    });
                    SelectBox.delete_from_cache(from, option_value);
                }
            }
            // Only sort if there are any groups (to preserve existing behavior for non-grouped selects)
            if (SelectBox.cache[to].some((item) => item.group)) {
                SelectBox.sort(to);
            }
            SelectBox.redisplay(from);
            SelectBox.redisplay(to);
        },
        sort: function (id) {
            SelectBox.cache[id].sort(function (a, b) {
                a =
                    ((a.group && a.group.toLowerCase()) || "") +
                    a.text.toLowerCase();
                b =
                    ((b.group && b.group.toLowerCase()) || "") +
                    b.text.toLowerCase();
                if (a > b) {
                    return 1;
                }
                if (a < b) {
                    return -1;
                }
                return 0;
            });
        },
        select_all: function (id) {
            const box = document.getElementById(id);
            for (const option of box.options) {
                option.selected = true;
            }
        },
    };
    window.SelectBox = SelectBox;
}
