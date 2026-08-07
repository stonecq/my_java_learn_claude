package com.agent.permission;

import java.util.Map;

/**
 * 用户审批回调。
 * <p>
 * 关键设计：权限模块不直接读取 System.in，而是把"如何询问用户"通过该接口注入。
 * - CLI 入口注入控制台交互实现（读 stdin）
 * - 测试时注入 Mock 实现（自动返回 allow 或 deny）
 * - 后续可注入 GUI 弹窗实现
 *
 * @return true 表示用户允许，false 表示用户拒绝
 */
@FunctionalInterface
public interface UserApprovalCallback {

    boolean ask(String toolName, Map<String, Object> input, String reason);
}
