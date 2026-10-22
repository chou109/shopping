-- 普通消费者角色不应获得管理后台菜单和权限。
-- 仅移除 common 角色的菜单关联；不修改用户、订单及其他角色数据。
DELETE common_menu
FROM sys_role_menu AS common_menu
INNER JOIN sys_role AS common_role ON common_role.role_id = common_menu.role_id
WHERE common_role.role_key = 'common';
