package com.example.movie_booking_backend.common.constants;

/**
 * 权限常量
 */
public class PermissionConstants {

    // 角色常量
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    // 权限常量
    // 用户相关权限
    public static final String USER_VIEW = "user:view";
    public static final String USER_CREATE = "user:create";
    public static final String USER_UPDATE = "user:update";
    public static final String USER_DELETE = "user:delete";
    public static final String USER_STATUS = "user:status";
    public static final String USER_ROLE_MANAGE = "user:role:manage";

    // 电影相关权限
    public static final String MOVIE_VIEW = "movie:view";
    public static final String MOVIE_CREATE = "movie:create";
    public static final String MOVIE_UPDATE = "movie:update";
    public static final String MOVIE_DELETE = "movie:delete";
    public static final String MOVIE_STATUS = "movie:status";

    // 订单相关权限
    public static final String ORDER_VIEW = "order:view";
    public static final String ORDER_CREATE = "order:create";
    public static final String ORDER_UPDATE = "order:update";
    public static final String ORDER_DELETE = "order:delete";
    public static final String ORDER_REFUND = "order:refund";

    // 场次相关权限
    public static final String SESSION_VIEW = "session:view";
    public static final String SESSION_CREATE = "session:create";
    public static final String SESSION_UPDATE = "session:update";
    public static final String SESSION_DELETE = "session:delete";

    // 影厅相关权限
    public static final String HALL_VIEW = "hall:view";
    public static final String HALL_CREATE = "hall:create";
    public static final String HALL_UPDATE = "hall:update";
    public static final String HALL_DELETE = "hall:delete";

    // 支付相关权限
    public static final String PAYMENT_VIEW = "payment:view";
    public static final String PAYMENT_PROCESS = "payment:process";
    public static final String PAYMENT_REFUND = "payment:refund";

    // 系统相关权限
    public static final String SYSTEM_CONFIG = "system:config";
    public static final String SYSTEM_LOG = "system:log";
    public static final String SYSTEM_BACKUP = "system:backup";

    /**
     * 获取角色对应的权限列表
     */
    public static String[] getPermissionsByRole(String role) {
        switch (role) {
            case ROLE_USER:
                return new String[]{
                    MOVIE_VIEW, SESSION_VIEW, ORDER_VIEW, ORDER_CREATE, PAYMENT_PROCESS
                };
            case ROLE_ADMIN:
                return new String[]{
                    USER_VIEW, USER_CREATE, USER_UPDATE, USER_STATUS,
                    MOVIE_VIEW, MOVIE_CREATE, MOVIE_UPDATE, MOVIE_DELETE, MOVIE_STATUS,
                    ORDER_VIEW, ORDER_UPDATE, ORDER_REFUND,
                    SESSION_VIEW, SESSION_CREATE, SESSION_UPDATE, SESSION_DELETE,
                    HALL_VIEW, HALL_CREATE, HALL_UPDATE, HALL_DELETE,
                    PAYMENT_VIEW, PAYMENT_PROCESS, PAYMENT_REFUND
                };
            case ROLE_SUPER_ADMIN:
                return new String[]{
                    USER_VIEW, USER_CREATE, USER_UPDATE, USER_DELETE, USER_STATUS, USER_ROLE_MANAGE,
                    MOVIE_VIEW, MOVIE_CREATE, MOVIE_UPDATE, MOVIE_DELETE, MOVIE_STATUS,
                    ORDER_VIEW, ORDER_CREATE, ORDER_UPDATE, ORDER_DELETE, ORDER_REFUND,
                    SESSION_VIEW, SESSION_CREATE, SESSION_UPDATE, SESSION_DELETE,
                    HALL_VIEW, HALL_CREATE, HALL_UPDATE, HALL_DELETE,
                    PAYMENT_VIEW, PAYMENT_PROCESS, PAYMENT_REFUND,
                    SYSTEM_CONFIG, SYSTEM_LOG, SYSTEM_BACKUP
                };
            default:
                return new String[]{};
        }
    }
} 