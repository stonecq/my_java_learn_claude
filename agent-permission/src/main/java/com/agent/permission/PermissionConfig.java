package com.agent.permission;

import com.agent.permission.rule.DenyListRule;
import com.agent.permission.rule.DestructiveCommandRule;
import com.agent.permission.rule.PathBoundaryRule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * 权限配置工厂：用内置默认配置（permission-default.json）组装一个可直接使用的 PermissionPipeline。
 * 调用方只需 PermissionConfig.withDefaults(workdir, callback)，无需关心具体规则类。
 */
@Setter
public class PermissionConfig {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private List<String> denyList;
    private List<String> destructiveCommands;

    /** 工厂：用内置默认规则组装 PermissionPipeline */
    public static PermissionPipeline withDefaults(Path workdir, UserApprovalCallback callback) {
        PermissionConfig defaults = readDefault();
        return new PermissionPipeline(
                List.of(new DenyListRule(patterns(defaults.denyList))),
                List.of(new PathBoundaryRule(workdir),
                        new DestructiveCommandRule(patterns(defaults.destructiveCommands))),
                callback);
    }

    private static List<String> patterns(List<String> patterns) {
        return patterns == null ? List.of() : patterns;
    }

    private static PermissionConfig readDefault() {
        try (InputStream in = PermissionConfig.class.getResourceAsStream("/permission-default.json")) {
            if (in == null) {
                return new PermissionConfig();
            }
            return MAPPER.readValue(in, PermissionConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException("读取默认权限配置失败", e);
        }
    }
}
