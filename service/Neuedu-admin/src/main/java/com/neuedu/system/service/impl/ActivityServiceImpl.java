package com.neuedu.system.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neuedu.system.constants.ActivityConstants;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.ActivityMapper;
import com.neuedu.system.domain.Activity;
import com.neuedu.system.service.IActivityService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 活动Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements IActivityService
{
    private final ActivityMapper activityMapper;

    /**
     * 查询活动
     * 
     * @param id 活动主键
     * @return 活动
     */
    @Override
    public Activity selectActivityById(Long id)
    {
        Activity activity = activityMapper.selectActivityById(id);
        if (activity != null) {
            enrichActivityInfo(activity);
        }
        return activity;

    }

    /**
     * 查询活动列表
     * 
     * @param activity 活动
     * @return 活动
     */
    @Override
    public List<Activity> selectActivityList(Activity activity)
    {

        List<Activity> list = activityMapper.selectActivityList(activity);
        list.forEach(this::enrichActivityInfo);
        return list;
    }

    /**
     * 查询有效的活动列表（前台展示）
     */
    @Override
    public List<Activity> selectValidActivityList() {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_flag", ActivityConstants.DELETE_FLAG_NO)
                .eq("status", ActivityConstants.STATUS_ONGOING)
                .le("start_time", new Date())  // 已经开始
                .ge("end_time", new Date())    // 尚未结束
                .orderByDesc("create_time");

        List<Activity> list = list(queryWrapper);
        list.forEach(this::enrichActivityInfo);
        return list;
    }

    /**
     * 查询进行中的活动列表
     */
    @Override
    public List<Activity> selectOngoingActivityList() {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_flag", ActivityConstants.DELETE_FLAG_NO)
                .eq("status", ActivityConstants.STATUS_ONGOING)
                .orderByDesc("create_time");

        List<Activity> list = list(queryWrapper);
        list.forEach(this::enrichActivityInfo);
        return list;
    }

    /**
     * 根据时间范围查询活动
     */
    @Override
    public List<Activity> selectActivityByTimeRange(Date startTime, Date endTime) {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_flag", ActivityConstants.DELETE_FLAG_NO)
                .le("start_time", endTime)
                .ge("end_time", startTime)
                .orderByAsc("start_time");

        List<Activity> list = list(queryWrapper);
        list.forEach(this::enrichActivityInfo);
        return list;
    }

    /**
     * 新增活动
     * 
     * @param activity 活动
     * @return 结果
     */
    @Override
    public int insertActivity(Activity activity)
    {
        // 1. 验证活动时间
        if (!validateActivityTime(activity.getStartTime(), activity.getEndTime())) {
            throw new RuntimeException("活动开始时间不能晚于结束时间");
        }

        // 2. 设置默认值
        activity.setCreateTime(DateUtils.getNowDate());
        activity.setDeleteFlag(ActivityConstants.DELETE_FLAG_NO);

        // 3. 自动计算状态
        Integer calculatedStatus = ActivityConstants.calculateStatus(
                activity.getStartTime(),
                activity.getEndTime(),
                activity.getStatus()
        );
        activity.setStatus(calculatedStatus);

        return activityMapper.insertActivity(activity);
    }

    /**
     * 修改活动
     * 
     * @param activity 活动
     * @return 结果
     */
    @Override
    public int updateActivity(Activity activity)
    {
        // 1. 验证活动时间
        if (!validateActivityTime(activity.getStartTime(), activity.getEndTime())) {
            throw new RuntimeException("活动开始时间不能晚于结束时间");
        }

        // 2. 自动计算状态
        Integer calculatedStatus = ActivityConstants.calculateStatus(
                activity.getStartTime(),
                activity.getEndTime(),
                activity.getStatus()
        );
        activity.setStatus(calculatedStatus);

        activity.setUpdateTime(DateUtils.getNowDate());
        return activityMapper.updateActivity(activity);
    }

    /**
     * 批量删除活动
     * 
     * @param ids 需要删除的活动主键
     * @return 结果
     */
    @Override
    public int deleteActivityByIds(Long[] ids)
    {
        // 检查是否有进行中的活动
        for (Long id : ids) {
            Activity activity = selectActivityById(id);
            if (activity != null && activity.getStatus() == ActivityConstants.STATUS_ONGOING) {
                throw new RuntimeException("活动ID[" + id + "]正在进行中，不能删除");
            }
        }

        // 软删除
        return activityMapper.deleteActivityByIds(ids);
    }

    /**
     * 删除活动信息
     * 
     * @param id 活动主键
     * @return 结果
     */
    @Override
    public int deleteActivityById(Long id)
    {
        // 检查是否是进行中的活动
        Activity activity = selectActivityById(id);
        if (activity != null && activity.getStatus() == ActivityConstants.STATUS_ONGOING) {
            throw new RuntimeException("活动正在进行中，不能删除");
        }

        // 软删除
        return activityMapper.deleteActivityById(id);
    }

    /**
     * 验证活动时间
     */
    @Override
    public boolean validateActivityTime(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return true; // 允许为空，前端验证
        }

        return !startTime.after(endTime);
    }

    /**
     * 更新活动状态（根据时间自动更新）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateActivityStatus(Long id) {
        Activity activity = selectActivityById(id);
        if (activity == null) {
            return 0;
        }

        Integer newStatus = ActivityConstants.calculateStatus(
                activity.getStartTime(),
                activity.getEndTime(),
                activity.getStatus()
        );

        if (!newStatus.equals(activity.getStatus())) {
            activity.setStatus(newStatus);
            activity.setUpdateTime(DateUtils.getNowDate());
            return activityMapper.updateActivity(activity);
        }

        return 0;
    }

    /**
     * 批量更新活动状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateActivityStatus() {
        // 查询所有未删除的活动
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_flag", ActivityConstants.DELETE_FLAG_NO)
                .ne("status", ActivityConstants.STATUS_CANCELLED); // 已取消的不更新

        List<Activity> activities = list(queryWrapper);
        int updatedCount = 0;

        for (Activity activity : activities) {
            Integer newStatus = ActivityConstants.calculateStatus(
                    activity.getStartTime(),
                    activity.getEndTime(),
                    activity.getStatus()
            );

            if (!newStatus.equals(activity.getStatus())) {
                activity.setStatus(newStatus);
                activity.setUpdateTime(DateUtils.getNowDate());
                activityMapper.updateActivity(activity);
                updatedCount++;
            }
        }

        return updatedCount;
    }

    /**
     * 取消活动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelActivity(Long id) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setStatus(ActivityConstants.STATUS_CANCELLED);
        activity.setUpdateTime(DateUtils.getNowDate());
        return activityMapper.updateActivity(activity);
    }

    /**
     * 获取活动下的商品数量
     */
    @Override
    public int countProductsInActivity(Long activityId) {
        // 这里需要调用商品Mapper，统计该活动下的商品数量
        // 暂时返回0，实际项目中需要实现
        return 0;
    }

    /**
     * 丰富活动信息
     */
    private void enrichActivityInfo(Activity activity) {
        // 调用初始化方法设置扩展字段
        activity.initExtraFields();

        // 计算关联商品数量
        Integer productCount = countProductsInActivity(activity.getId());
        activity.setProductCount(productCount);
    }


}
