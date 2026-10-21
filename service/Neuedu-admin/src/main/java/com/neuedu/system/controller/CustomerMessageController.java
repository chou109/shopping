package com.neuedu.system.controller;

import java.util.List;
import java.util.Map;

import com.neuedu.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.neuedu.common.annotation.Log;
import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.enums.BusinessType;
import com.neuedu.system.domain.CustomerMessage;
import com.neuedu.system.service.ICustomerMessageService;
import com.neuedu.system.service.IShopService;
import org.springframework.security.access.AccessDeniedException;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 客服消息Controller
 * 
 * @author neuedu
 * @date 2025-12-26
 */
@RestController
@RequestMapping("/system/customerMessage")
@Tag(name = "【客服消息】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomerMessageController extends BaseController
{
    private final ICustomerMessageService customerMessageService;
    private final IShopService shopService;

    /**
     * 查询客服消息列表（管理端）
     */
    @Operation(summary = "查询客服消息列表")
    @PreAuthorize("@ss.hasPermi('system:customerMessage:list')")
    @GetMapping("/list")
    public TableDataInfo list(CustomerMessage customerMessage)
    {
        startPage();
        List<CustomerMessage> list = customerMessageService.selectCustomerMessageList(customerMessage);
        return getDataTable(list);
    }

    /**
     * 获取客服消息详细信息
     */
    @Operation(summary = "获取客服消息详细信息")
    @PreAuthorize("@ss.hasPermi('system:customerMessage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(customerMessageService.selectCustomerMessageById(id));
    }

    /**
     * 删除客服消息
     */
    @Operation(summary = "删除客服消息")
    @PreAuthorize("@ss.hasPermi('system:customerMessage:remove')")
    @Log(title = "客服消息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(customerMessageService.deleteCustomerMessageByIds(ids));
    }

    // ============ 用户端接口 ============

    /**
     * 用户发送消息给商家
     */
    @Operation(summary = "用户发送消息给商家")
    @PostMapping("/user/send")
    public AjaxResult userSendMessage(@RequestBody Map<String, Object> params)
    {
        Long userId = getUserId();
        String userName = getUsername();
        String content = (String) params.get("content");
        Integer messageType = params.get("messageType") != null ? 
            Integer.parseInt(params.get("messageType").toString()) : CustomerMessage.MESSAGE_TYPE_TEXT;
        Long receiverId = params.get("receiverId") != null ? 
            Long.parseLong(params.get("receiverId").toString()) : null;
        
        if (content == null || content.trim().isEmpty()) {
            return error("消息内容不能为空");
        }
        
        CustomerMessage message = customerMessageService.sendMessageToAdmin(
            userId, userName, null, content.trim(), messageType, receiverId);
        return success(message);
    }

    /**
     * 获取用户的消息列表
     */
    @Operation(summary = "获取用户的消息列表")
    @GetMapping("/user/messages")
    public AjaxResult getUserMessages(@RequestParam(required = false) Long receiverId)
    {
        Long userId = getUserId();
        List<CustomerMessage> messages = customerMessageService.getUserMessages(userId, receiverId);
        return success(messages);
    }

    /**
     * 获取用户未读消息数量
     */
    @Operation(summary = "获取用户未读消息数量")
    @GetMapping("/user/unread/count")
    public AjaxResult getUserUnreadCount()
    {
        Long userId = getUserId();
        Long count = customerMessageService.countUnreadByUserId(userId);
        return success(count);
    }

    /**
     * 用户标记消息为已读
     */
    @Operation(summary = "用户标记消息为已读")
    @PutMapping("/user/read")
    public AjaxResult userMarkAsRead(@RequestParam(required = false) Long receiverId)
    {
        Long userId = getUserId();
        String sessionId;
        if (receiverId != null && receiverId > 0) {
            sessionId = "CS_" + userId + "_TO_" + receiverId;
        } else {
            sessionId = customerMessageService.generateSessionId(userId);
        }
        int result = customerMessageService.markAsRead(sessionId, CustomerMessage.SENDER_TYPE_USER);
        return success(result);
    }

    /**
     * 获取用户的会话列表
     */
    @Operation(summary = "获取用户的会话列表")
    @GetMapping("/user/sessions")
    public AjaxResult getUserSessions()
    {
        Long userId = getUserId();
        List<Map<String, Object>> sessions = customerMessageService.getUserSessionList(userId);
        return success(sessions);
    }

    // ============ 商家端接口 ============

    /**
     * 商家回复消息给用户
     */
    @Operation(summary = "商家回复消息给用户")
    @PostMapping("/admin/reply")
    public AjaxResult adminReplyMessage(@RequestBody Map<String, Object> params)
    {
        Long userId = getUserId();
        Long adminId = requireAdminOrShopOwner();
        String sessionId = (String) params.get("sessionId");
        String content = (String) params.get("content");
        Integer messageType = params.get("messageType") != null ? 
            Integer.parseInt(params.get("messageType").toString()) : CustomerMessage.MESSAGE_TYPE_TEXT;
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return error("会话ID不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return error("消息内容不能为空");
        }
        if (!customerMessageService.isAdminSessionOwned(sessionId, adminId)) {
            return AjaxResult.error(403, "无权回复此客服会话");
        }

        String adminName = getUsername();
        
        CustomerMessage message = customerMessageService.sendMessageToUser(
            sessionId, userId, adminName, content.trim(), messageType);
        return success(message);
    }

    /**
     * 获取商家的会话列表（只获取发给当前登录商家的消息）
     */
    @Operation(summary = "获取商家的会话列表")
    @GetMapping("/admin/sessions")
    public AjaxResult getAdminSessions()
    {
        Long adminId = requireAdminOrShopOwner();
        List<Map<String, Object>> sessions = customerMessageService.getAdminSessionList(adminId);
        return success(sessions);
    }

    /**
     * 获取指定会话的消息列表
     */
    @Operation(summary = "获取指定会话的消息列表")
    @GetMapping("/admin/session/{sessionId}")
    public AjaxResult getSessionMessages(@PathVariable String sessionId)
    {
        Long adminId = requireAdminOrShopOwner();
        if (!customerMessageService.isAdminSessionOwned(sessionId, adminId)) {
            return AjaxResult.error(403, "无权访问此客服会话");
        }
        List<CustomerMessage> messages = customerMessageService.getAdminSessionMessages(sessionId, adminId);
        return success(messages);
    }

    /**
     * 获取商家未读消息数量
     */
    @Operation(summary = "获取商家未读消息数量")
    @GetMapping("/admin/unread/count")
    public AjaxResult getAdminUnreadCount()
    {
        Long adminId = requireAdminOrShopOwner();
        Long count = customerMessageService.countUnreadByAdmin(adminId);
        return success(count);
    }

    /**
     * 商家标记会话消息为已读
     */
    @Operation(summary = "商家标记会话消息为已读")
    @PutMapping("/admin/read/{sessionId}")
    public AjaxResult adminMarkAsRead(@PathVariable String sessionId)
    {
        Long adminId = requireAdminOrShopOwner();
        if (!customerMessageService.isAdminSessionOwned(sessionId, adminId)) {
            return AjaxResult.error(403, "无权操作此客服会话");
        }
        int result = customerMessageService.markAdminSessionAsRead(sessionId, adminId);
        return success(result);
    }

    private Long requireAdminOrShopOwner() {
        Long userId = getUserId();
        if (SecurityUtils.isAdmin(userId)) return null;
        if (shopService.selectShopByUserId(userId) == null) {
            throw new AccessDeniedException("仅管理员或店铺负责人可以处理客服会话");
        }
        return userId;
    }
}
