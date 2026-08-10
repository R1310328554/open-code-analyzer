// @ts-check
/**
 * keycloak.v2 用户资料表单：MutationObserver 驱动的 data-* 注解注册。
 * @typedef {Object} AnnotationDescriptor
 * @property {string} name - 要注册的字段名（如 `numberFormat`）。
 * @property {(element: HTMLElement) => (() => void) | void} onAdd - 元素加入 DOM 时调用的初始化函数。
 */

// 监听 body 子树 DOM 变更
// 监听 body 子树 DOM 变更
const observer = new MutationObserver(onMutate);
observer.observe(document.body, { childList: true, subtree: true });

/** @type {AnnotationDescriptor[]} */
const descriptors = [];

/** @type {WeakMap<HTMLElement, () => void>} */
const cleanupFunctions = new WeakMap();

/**
 * 注册注解描述符并扫描已有匹配 input 元素。
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
 * 处理节点增删：清理移除项、为新增 input 绑定注解。
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
      const input = node.querySelector('input');
      if (input.hasAttribute(`data-${descriptor.name}`)) {
        handleNewElement(input, descriptor);
      }
    }
  }
}

/**
 * 对新元素执行 onAdd 并保存可选清理回调。
 * @param {HTMLElement} element
 * @param {AnnotationDescriptor} descriptor
 */
function handleNewElement(element, descriptor) {
  const cleanup = descriptor.onAdd(element);

  if (cleanup) {
    cleanupFunctions.set(element, cleanup);
  }
}
