package com.neuedu.system.controller;

import java.util.List;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.neuedu.common.annotation.Log;
import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.enums.BusinessType;
import com.neuedu.system.domain.Activity;
import com.neuedu.system.service.IActivityService;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 活动Controller
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@RestController
@RequestMapping("/system/activity")
@Tag(name = "【活动】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ActivityController extends BaseController
{
    private final IActivityService activityService;

    /**
     * 查询活动列表
     */
    @Operation(summary = "查询活动列表")
    @PreAuthorize("@ss.hasPermi('system:activity:list')")
    @GetMapping("/list")
    public TableDataInfo list(Activity activity)
    {
        startPage();
        List<Activity> list = activityService.selectActivityList(activity);
        return getDataTable(list);
    }

    /**
     * 导出活动列表
     */
    @Operation(summary = "导出活动列表")
    @PreAuthorize("@ss.hasPermi('system:activity:export')")
    @Log(title = "活动", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Activity activity)
    {
        List<Activity> list = activityService.selectActivityList(activity);
        ExcelUtil<Activity> util = new ExcelUtil<Activity>(Activity.class);
        util.exportExcel(response, list, "活动数据");
    }

    /**
     * 查询有效的活动列表（前台展示）
     */
    @Operation(summary = "查询有效的活动列表")
    @GetMapping("/valid/list")
    public AjaxResult validList() {
        List<Activity> list = activityService.selectValidActivityList();
        return success(list);
    }

    /**
     * 查询进行中的活动列表
     */
    @Operation(summary = "查询进行中的活动列表")
    @GetMapping("/ongoing/list")
    public AjaxResult ongoingList() {
        List<Activity> list = activityService.selectOngoingActivityList();
        return success(list);
    }

    /**
     * 根据时间范围查询活动
     */
    @Operation(summary = "根据时间范围查询活动")
    @PreAuthorize("@ss.hasPermi('system:activity:list')")
    @GetMapping("/timeRange")
    public AjaxResult getByTimeRange(@RequestParam String startTime,
                                     @RequestParam String endTime) {
        try {
            LocalDate start = parseDate(startTime);
            LocalDate end = parseDate(endTime);
            if (start.isAfter(end)) return AjaxResult.error(400, "开始日期不能晚于结束日期");
            ZoneId zone = ZoneId.systemDefault();
            Date startDate = Date.from(start.atStartOfDay(zone).toInstant());
            Date endDate = Date.from(end.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant());
            return success(activityService.selectActivityByTimeRange(startDate, endDate));
        } catch (DateTimeException | NullPointerException exception) {
            return AjaxResult.error(400, "日期格式必须为 yyyy-MM-dd");
        }
    }


    /**
     * 获取活动详细信息
     */
    @Operation(summary = "获取活动详细信息")
    @PreAuthorize("@ss.hasPermi('system:activity:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(activityService.selectActivityById(id));
    }

    /**
     * 新增活动
     */
    @Operation(summary = "新增活动")
    @PreAuthorize("@ss.hasPermi('system:activity:add')")
    @Log(title = "活动", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Activity activity)
    {
        // 验证活动时间
        if (!activityService.validateActivityTime(activity.getStartTime(), activity.getEndTime())) {
            return error("活动开始时间不能晚于结束时间");
        }
        return toAjax(activityService.insertActivity(activity));
    }

    /**
     * 修改活动
     */
    @Operation(summary = "修改活动")
    @PreAuthorize("@ss.hasPermi('system:activity:edit')")
    @Log(title = "活动", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Activity activity)
    {

        // 验证活动时间
        if (!activityService.validateActivityTime(activity.getStartTime(), activity.getEndTime())) {
            return error("活动开始时间不能晚于结束时间");
        }
        return toAjax(activityService.updateActivity(activity));
    }

    /**
     * 删除活动
     */
    @Operation(summary = "删除活动")
    @PreAuthorize("@ss.hasPermi('system:activity:remove')")
    @Log(title = "活动", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable( name = "ids" ) Long[] ids) 
    {
        return toAjax(activityService.deleteActivityByIds(ids));
    }

    /**
     * 更新活动状态
     */
    @Operation(summary = "更新活动状态")
    @PreAuthorize("@ss.hasPermi('system:activity:edit')")
    @PostMapping("/{id}/updateStatus")
    public AjaxResult updateStatus(@PathVariable Long id) {
        return toAjax(activityService.updateActivityStatus(id));
    }

    /**
     * 批量更新活动状态（定时任务调用）
     */
    @Operation(summary = "批量更新活动状态")
    @PreAuthorize("@ss.hasPermi('system:activity:edit')")
    @PostMapping("/batchUpdateStatus")
    public AjaxResult batchUpdateStatus() {
        int updatedCount = activityService.batchUpdateActivityStatus();
        return success("成功更新 " + updatedCount + " 个活动的状态");
    }

    /**
     * 取消活动
     */
    @Operation(summary = "取消活动")
    @PreAuthorize("@ss.hasPermi('system:activity:edit')")
    @PostMapping("/{id}/cancel")
    public AjaxResult cancelActivity(@PathVariable Long id) {
        return toAjax(activityService.cancelActivity(id));
    }

    /**
     * 验证活动时间
     */
    @Operation(summary = "验证活动时间")
    @PreAuthorize("@ss.hasAnyPermi('system:activity:add,system:activity:edit')")
    @PostMapping("/validateTime")
    public AjaxResult validateTime(@RequestParam String startTime,
                                   @RequestParam String endTime) {
        try {
            LocalDate start = parseDate(startTime);
            LocalDate end = parseDate(endTime);
            if (start.isAfter(end)) return AjaxResult.error(400, "开始时间不能晚于结束时间");
            ZoneId zone = ZoneId.systemDefault();
            Date startDate = Date.from(start.atStartOfDay(zone).toInstant());
            Date endDate = Date.from(end.atStartOfDay(zone).toInstant());
            return success(activityService.validateActivityTime(startDate, endDate));
        } catch (DateTimeException | NullPointerException exception) {
            return AjaxResult.error(400, "日期格式必须为 yyyy-MM-dd");
        }
    }

    private LocalDate parseDate(String value) {
        return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
