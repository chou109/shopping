package com.neuedu.system.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.neuedu.common.annotation.Log;
import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.enums.BusinessType;
import com.neuedu.system.domain.OrderStatusRecord;
import com.neuedu.system.service.IOrderStatusRecordService;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 订单记录Controller
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@RestController
@RequestMapping("/system/record")
@Tag(name = "【订单记录】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderStatusRecordController extends BaseController
{
    private final IOrderStatusRecordService orderStatusRecordService;

    /**
     * 查询订单记录列表
     */
    @Operation(summary = "查询订单记录列表")
    @PreAuthorize("@ss.hasPermi('system:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(OrderStatusRecord orderStatusRecord)
    {
        startPage();
        List<OrderStatusRecord> list = orderStatusRecordService.selectOrderStatusRecordList(orderStatusRecord);
        return getDataTable(list);
    }

    /**
     * 导出订单记录列表
     */
    @Operation(summary = "导出订单记录列表")
    @PreAuthorize("@ss.hasPermi('system:record:export')")
    @Log(title = "订单记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OrderStatusRecord orderStatusRecord)
    {
        List<OrderStatusRecord> list = orderStatusRecordService.selectOrderStatusRecordList(orderStatusRecord);
        ExcelUtil<OrderStatusRecord> util = new ExcelUtil<OrderStatusRecord>(OrderStatusRecord.class);
        util.exportExcel(response, list, "订单记录数据");
    }

    /**
     * 获取订单记录详细信息
     */
    @Operation(summary = "获取订单记录详细信息")
    @PreAuthorize("@ss.hasPermi('system:record:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(orderStatusRecordService.selectOrderStatusRecordById(id));
    }

    /**
     * 新增订单记录
     */
    @Operation(summary = "新增订单记录")
    @PreAuthorize("@ss.hasPermi('system:record:add')")
    @Log(title = "订单记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OrderStatusRecord orderStatusRecord)
    {
        return toAjax(orderStatusRecordService.insertOrderStatusRecord(orderStatusRecord));
    }

    /**
     * 修改订单记录
     */
    @Operation(summary = "修改订单记录")
    @PreAuthorize("@ss.hasPermi('system:record:edit')")
    @Log(title = "订单记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OrderStatusRecord orderStatusRecord)
    {
        return toAjax(orderStatusRecordService.updateOrderStatusRecord(orderStatusRecord));
    }

    /**
     * 删除订单记录
     */
    @Operation(summary = "删除订单记录")
    @PreAuthorize("@ss.hasPermi('system:record:remove')")
    @Log(title = "订单记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable( name = "ids" ) Long[] ids) 
    {
        return toAjax(orderStatusRecordService.deleteOrderStatusRecordByIds(ids));
    }
}
