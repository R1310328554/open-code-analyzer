// @ts-check
/**
 * 用户资料表单：基于 MutationObserver 为带 data-* 标记的 DOM 元素注册注解处理器。
 * @typedef {Object} AnnotationDescriptor
 * @property {string} name - 要注册的字段名（如 `numberFormat`）。
 * @property {(element: HTMLElement) => (() => void) | void} onAdd - 元素加入 DOM 时调用的初始化函数，可返回清理回调。
 */

// 监听 body 子树变更，动态处理新增/移除的注解元素
const observer = new MutationObserver(onMutate);
observer.observe(document.body, { childList: true, subtree: true });

/** @type {AnnotationDescriptor[]} */
const descriptors = [];

/** @type {WeakMap<HTMLElement, () => void>} */
const cleanupFunctions = new WeakMap();

/**
 * 注册一种 data-${name} 注解描述符，并扫描页面上已有匹配元素。
 * @param {AnnotationDescriptor} descriptor
 */
export function registerElementAnnotatedBy(descriptor) {
  descriptors.push(descriptor);

  document.querySelectorAll(`[data-${descriptor.name}]`).forEach((element) => {
    if (element instanceof HTMLElement) {
      handleNewElement(element, descriptor);
    }
  });
}

/**
 * DOM 变更回调：清理已移除元素的处理器，并为新增匹配元素绑定注解。
 * @type {MutationCallback}
 */
function onMutate(mutations) {
  const removedNodes = mutations.flatMap((mutation) => Array.from(mutation.removedNodes));

  for (const node of removedNodes) {
    if (!(node instanceof HTMLElement)) {
      continue;
    }

    const handleRemovedElement = cleanupFunctions.get(node);

    if (handleRemovedElement) {
      handleRemovedElement();
    }

    cleanupFunctions.delete(node);
  }

  const addedNodes = mutations.flatMap((mutation) => Array.from(mutation.addedNodes));

  for (const descriptor of descriptors) {
    for (const node of addedNodes) {
      if (node instanceof HTMLElement && node.hasAttribute(`data-${descriptor.name}`)) {
        handleNewElement(node, descriptor);
      }
    }
  }
}

/**
 * 对新元素执行 onAdd 并保存可选的清理函数。
 * @param {HTMLElement} element
 * @param {AnnotationDescriptor} descriptor
 */
function handleNewElement(element, descriptor) {
  const cleanup = descriptor.onAdd(element);

  if (cleanup) {
    cleanupFunctions.set(element, cleanup);
  }
}
