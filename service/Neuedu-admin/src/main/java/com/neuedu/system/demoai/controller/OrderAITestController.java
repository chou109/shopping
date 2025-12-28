package com.neuedu.system.demoai.controller;

import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.system.demoai.ZhipuAIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI客服订单查询测试控制器
 */
@RestController
@RequestMapping("/api/ai/order-test")
@Tag(name = "AI客服订单查询测试")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderAITestController extends BaseController {

    private final ZhipuAIService zhipuAIService;

    /**
     * 测试订单查询功能
     */
    @Operation(summary = "测试订单查询")
    @GetMapping("/query")
    public AjaxResult testOrderQuery(@RequestParam String question, 
                                     @RequestParam(required = false) Long userId) {
        try {
            String response = zhipuAIService.handleOrderQuery(question, userId);
            return AjaxResult.success(response);
        } catch (Exception e) {
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 测试是否为订单查询请求
     */
    @Operation(summary = "测试是否为订单查询请求")
    @GetMapping("/is-order-query")
    public AjaxResult isOrderQuery(@RequestParam String question) {
        boolean isOrder = zhipuAIService.isOrderQueryRequest(question);
        return AjaxResult.success(isOrder);
    }
}
