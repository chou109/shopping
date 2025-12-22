// UserTypeConstants.java
package com.neuedu.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户类型常量
 */
public class UserTypeConstants {

    // 用户类型
    public static final int USER = 0;          // 普通用户
    public static final int ADMIN = 1;         // 管理员
    public static final int SUPER_ADMIN = 2;   // 超级管理员
    public static final int AUDITOR = 3;       // 审核员
    public static final int OPERATOR = 4;      // 运营
    public static final int CUSTOMER_SERVICE = 5; // 客服
    public static final int FINANCE = 6;       // 财务
    public static final int DEVELOPER = 7;     // 开发者

    // 用户类型名称映射
    public static final Map<Integer, String> TYPE_NAME_MAP = new HashMap<>();

    static {
        TYPE_NAME_MAP.put(USER, "普通用户");
        TYPE_NAME_MAP.put(ADMIN, "管理员");
        TYPE_NAME_MAP.put(SUPER_ADMIN, "超级管理员");
        TYPE_NAME_MAP.put(AUDITOR, "审核员");
        TYPE_NAME_MAP.put(OPERATOR, "运营");
        TYPE_NAME_MAP.put(CUSTOMER_SERVICE, "客服");
        TYPE_NAME_MAP.put(FINANCE, "财务");
        TYPE_NAME_MAP.put(DEVELOPER, "开发者");
    }

    // 获取类型名称
    public static String getTypeName(Integer type) {
        return TYPE_NAME_MAP.getOrDefault(type, "未知类型");
    }

    // 检查是否是管理员（1及以上）
    public static boolean isAdmin(Integer userType) {
        return userType != null && userType >= ADMIN;
    }

    // 检查是否是超级管理员
    public static boolean isSuperAdmin(Integer userType) {
        return userType != null && userType == SUPER_ADMIN;
    }
}