package com.neuedu.online.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.core.domain.entity.SysRole;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.common.utils.SecurityUtils;
import com.neuedu.common.utils.StringUtils;
import com.neuedu.online.domain.OnlineMb;
import com.neuedu.online.service.IOnlineMbService;
import com.neuedu.online.utils.SqlMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 在线接口
 * 
 * @author Dftre
 * @date 2024-01-26
 */
@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/online")
public class OnLineController extends BaseController {
    @Autowired
    private IOnlineMbService onlineMbService;


    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    public Map<String, Object> getParams(HashMap<String, Object> params, HashMap<String, Object> data) {
        Map<String, Object> object = new HashMap<>();
        HashMap<String, Object> object_params = new HashMap<String, Object>();
        String keyregex = "params\\[(.*?)\\]";
        Pattern pattern = Pattern.compile(keyregex);
        if (params != null) {
            params.keySet().forEach(key -> {
                Matcher matcher = pattern.matcher(key);
                if (matcher.find()) {
                    object_params.put(matcher.group(1), params.get(key));
                } else {
                    object.put(key, params.get(key));
                }
            });
        }
        if (data != null) {
            if (data.containsKey("params")) {
                object_params.putAll((HashMap<String, Object>) data.get("params"));
                data.remove("params");
            }
            object.putAll(data);
        }
        object.put("params", object_params);
        return object;
    }

    public Boolean checkPermission(String permissionType,String permissionValue) {
        if (StringUtils.isEmpty(permissionType) || "无".equals(permissionType.trim()))
            return true;
        if (StringUtils.isEmpty(permissionValue)) return false;
        LoginUser loginUser = SecurityUtils.getLoginUser();
        switch (permissionType.trim()){
            case "hasPermi":
                return hasPermission(loginUser, permissionValue.trim());
            case "lacksPermi":
                return !hasPermission(loginUser, permissionValue.trim());
            case "hasAnyPermi":
                return anyPermission(loginUser, permissionValue);
            case "hasRole":
                return hasRole(loginUser, permissionValue.trim());
            case "lacksRole":
                return !hasRole(loginUser, permissionValue.trim());
            case "hasAnyRoles":
                for (String role : permissionValue.split(",")) {
                    if (hasRole(loginUser, role.trim())) return true;
                }
                return false;
            default:
                return false;
        }

    }

    private boolean hasPermission(LoginUser loginUser, String permission) {
        return loginUser.getPermissions() != null
                && (loginUser.getPermissions().contains("*:*:*") || loginUser.getPermissions().contains(permission));
    }

    private boolean anyPermission(LoginUser loginUser, String permissions) {
        for (String permission : permissions.split(",")) {
            if (hasPermission(loginUser, permission.trim())) return true;
        }
        return false;
    }

    private boolean hasRole(LoginUser loginUser, String role) {
        if (loginUser.getUser() == null || loginUser.getUser().getRoles() == null) return false;
        for (SysRole sysRole : loginUser.getUser().getRoles()) {
            if (sysRole != null && ("admin".equals(sysRole.getRoleKey()) || role.equals(sysRole.getRoleKey()))) return true;
        }
        return false;
    }

    public Object processingMapper(String sqlContext, String actuatot, Map<String, Object> params) {
        String sql = "<script>\n" + sqlContext + "\n</script>";
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            SqlMapper sqlMapper = new SqlMapper(sqlSession);
            Object result;
            switch (actuatot){
                case "selectList":
                    result = success(sqlMapper.selectList(sql, params));
                    break;
                case "selectOne":
                    result = success(sqlMapper.selectOne(sql, params));
                    break;
                case "insert":
                    result = toAjax(sqlMapper.insert(sql, params));
                    sqlSession.commit();
                    break;
                case "update":
                    result = toAjax(sqlMapper.update(sql, params));
                    sqlSession.commit();
                    break;
                case "delete":
                    result = toAjax(sqlMapper.delete(sql, params));
                    sqlSession.commit();
                    break;
                default:
                    return AjaxResult.error(400, "不支持的在线接口执行器");
        }
            return result;
        } catch (RuntimeException error) {
            sqlSession.rollback();
            throw error;
        } finally {
            sqlSession.close();
        }
    }

    @RequestMapping("/api/**")
    public Object api(@RequestParam(required = false) HashMap<String, Object> params,
            @RequestBody(required = false) HashMap<String, Object> data, HttpServletRequest request,
            HttpServletResponse response) {
        OnlineMb selectOnlineMb = new OnlineMb();
        selectOnlineMb.setPath(request.getRequestURI().replace("/online/api", ""));
        selectOnlineMb.setMethod(request.getMethod());

        Map<String, Object> object = getParams(params, data);

        List<OnlineMb> selectOnlineMbList = onlineMbService.selectOnlineMbList(selectOnlineMb);
        if (selectOnlineMbList.size() == 0) {
            return AjaxResult.error("没有资源" + selectOnlineMb.getPath());
        } else if (selectOnlineMbList.size() > 1) {
            return AjaxResult.error(500, "系统错误，在线接口重复");
        } else {
            OnlineMb onlineMb = selectOnlineMbList.get(0);
            if (!checkPermission(onlineMb.getPermissionType(),onlineMb.getPermissionValue()))
                return AjaxResult.error(403, "没有权限，请联系管理员授权");

            if (onlineMb.getDeptId() != null && onlineMb.getDeptId().equals("1")) {
                object.put("deptId", SecurityUtils.getDeptId());
            }
            if (onlineMb.getUserId() != null && onlineMb.getUserId().equals("1")) {
                object.put("userId", SecurityUtils.getUserId());
            }

            return processingMapper(onlineMb.getSql(), onlineMb.getActuator(), object);
        }
    }

}
