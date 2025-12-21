package com.neuedu.system.service;

import java.util.Date;
import java.util.List;
import com.neuedu.system.domain.Activity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 活动Service接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
public interface IActivityService extends IService<Activity>
{
    /**
     * 查询活动
     * 
     * @param id 活动主键
     * @return 活动
     */
    public Activity selectActivityById(Long id);

    /**
     * 查询活动列表
     * 
     * @param activity 活动
     * @return 活动集合
     */
    public List<Activity> selectActivityList(Activity activity);

    /**
     * 查询有效的活动列表（前台展示）
     */
    List<Activity> selectValidActivityList();

    /**
     * 查询进行中的活动列表
     */
    List<Activity> selectOngoingActivityList();

    /**
     * 根据时间范围查询活动
     */
    List<Activity> selectActivityByTimeRange(Date startTime, Date endTime);

    /**
     * 新增活动
     * 
     * @param activity 活动
     * @return 结果
     */
    public int insertActivity(Activity activity);

    /**
     * 修改活动
     * 
     * @param activity 活动
     * @return 结果
     */
    public int updateActivity(Activity activity);

    /**
     * 批量删除活动
     * 
     * @param ids 需要删除的活动主键集合
     * @return 结果
     */
    public int deleteActivityByIds(Long[] ids);

    /**
     * 删除活动信息
     * 
     * @param id 活动主键
     * @return 结果
     */
    public int deleteActivityById(Long id);

    /**
     * 验证活动时间
     */
    boolean validateActivityTime(Date startTime, Date endTime);

    /**
     * 更新活动状态（根据时间自动更新）
     */
    int updateActivityStatus(Long id);

    /**
     * 批量更新活动状态
     */
    int batchUpdateActivityStatus();

    /**
     * 取消活动
     */
    int cancelActivity(Long id);

    /**
     * 获取活动下的商品数量
     */
    int countProductsInActivity(Long activityId);


}
