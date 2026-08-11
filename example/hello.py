"""示例模块：向用户打招呼。

该模块演示了一个简单问候程序的基本结构，
包含类型提示、文档字符串与主入口守卫。
"""


def greet(name: str) -> str:
    """构造一条问候语。

    根据传入的名字生成形如 ``Hello, {name}`` 的问候消息。

    Args:
        name: 要问候的名字。

    Returns:
        构造好的问候语字符串。
    """
    return f"Hello, {name}"


def main() -> None:
    """程序入口，执行示例问候。"""
    message = greet("Claude")
    print(message)


if __name__ == "__main__":
    main()
