// 标记多值输入字段的 data 属性名
const DATA_KC_MULTIVALUED = 'data-kcMultivalued';
// “添加值”按钮 id 前缀
const KC_ADD_ACTION_PREFIX = "kc-add-";
// “移除”按钮 id 前缀
const KC_REMOVE_ACTION_PREFIX = "kc-remove-";
// PatternFly 内联链接按钮样式类
const KC_ACTION_CLASS = "pf-c-button pf-m-inline pf-m-link";

// 在字段后创建“添加值”按钮，克隆最后一个同名字段
function createAddAction(element) {
    const action = createAction("Add value",
        KC_ADD_ACTION_PREFIX,
        element,
        () => {
            const name = element.getAttribute("name");
            const elements = getInputElementsByName().get(name);
            const length = elements.length;

            if (length === 0) {
                return;
            }

            const lastNode = elements[length - 1];
            const newNode = lastNode.cloneNode(true);
            newNode.setAttribute("id", name + "-" + elements.length);
            newNode.value = "";
            lastNode.after(newNode);

            // 页面加载后立即初始化多值字段 UI
render();
        });

    element.after(action);
}

// 为字段创建“移除”按钮；最后一项在文案后附加分隔符
function createRemoveAction(element, isLastElement) {
    let text = "Remove";

    if (isLastElement) {
        text = text + " | ";
    }

    const action = createAction(text, KC_REMOVE_ACTION_PREFIX, element, () => {
        removeActions(element);
        element.remove();
        render();
    });

    element.insertAdjacentElement('afterend', action);
}

// 收集所有多值字段并按 name 属性分组
function getInputElementsByName() {
    const selector = document.querySelectorAll(`[${DATA_KC_MULTIVALUED}]`);
    const elementsByName = new Map();

    for (let element of Array.from(selector.values())) {
        let name = element.getAttribute("name");
        let elements = elementsByName.get(name);

        if (!elements) {
            elements = [];
            elementsByName.set(name, elements);
        }

        elements.push(element);
    }

    return elementsByName;
}

// 移除与字段关联的添加/移除按钮
function removeActions(element) {
    for (let actionPrefix of [KC_ADD_ACTION_PREFIX, KC_REMOVE_ACTION_PREFIX]) {
        const action = document.getElementById(actionPrefix + element.getAttribute("id"));

        if (action) {
            action.remove();
        }
    }
}

// 创建带 PatternFly 样式的内联操作按钮
function createAction(text, type, element, onClick) {
    const action = document.createElement("button")
    action.setAttribute("id", type + element.getAttribute("id"));
    action.setAttribute("type", "button");
    action.innerText = text;
    action.setAttribute("class", KC_ACTION_CLASS);
    action.addEventListener("click", onClick);
    return action;
}

// 重新渲染所有多值字段的 id 及添加/移除控件
function render() {
    getInputElementsByName().forEach((elements, name) => {
        elements.forEach((element, index) => {
            removeActions(element);

            element.setAttribute("id", name + "-" + index);

            const lastNode = element === elements[elements.length - 1];

            if (lastNode) {
                createAddAction(element);
            }

            if (elements.length > 1) {
                createRemoveAction(element, lastNode);
            }
        });
    });
}

render();