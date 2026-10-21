package com.neuedu.system.service;

import java.util.List;
import com.neuedu.system.domain.UserFootprint;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户足迹Service接口
 * 
 * @author ruoyi
 * @date 2025-12-22
 */
public interface IUserFootprintService extends IService<UserFootprint>
{
    /**
     * 查询用户足迹
     * 
     * @param id 用户足迹主键
     * @return 用户足迹
     */
    public UserFootprint selectUserFootprintById(Long id);

    /**
     * 查询用户足迹列表
     * 
     * @param userFootprint 用户足迹
     * @return 用户足迹集合
     */
    public List<UserFootprint> selectUserFootprintList(UserFootprint userFootprint);

    /**
     * 新增用户足迹
     * 
     * @param userFootprint 用户足迹
     * @return 结果
     */
    public int insertUserFootprint(UserFootprint userFootprint);

    /**
     * 修改用户足迹
     * 
     * @param userFootprint 用户足迹
     * @return 结果
     */
    public int updateUserFootprint(UserFootprint userFootprint);

    /**
     * 批量删除用户足迹
     * 
     * @param ids 需要删除的用户足迹主键集合
     * @return 结果
     */
    public int deleteUserFootprintByIds(Long[] ids);

    /**
     * 删除用户足迹信息
     * 
     * @param id 用户足迹主键
     * @return 结果
     */
    public int deleteUserFootprintById(Long id);


    // ============ 新增的业务方法 ============

    /**
     * 记录用户足迹（核心方法）
     *
     * @param userId 用户ID
     * @param targetType 目标类型 1-商品 2-店铺 3-活动
     * @param targetId 目标ID
     * @return 结果
     */
    public int recordFootprint(Long userId, Integer targetType, Long targetId);

    /**
     * 查询用户的足迹列表（按最后浏览时间倒序）
     *
     * @param userId 用户ID
     * @return 足迹列表
     */
    public List<UserFootprint> selectFootprintByUserId(Long userId, Integer targetType);

    /**
     * 查询用户最近浏览的足迹（前N条）
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 足迹列表
     */
    public List<UserFootprint> selectRecentFootprint(Long userId, Integer limit);

    /**
     * 清空用户的足迹
     *
     * @param userId 用户ID
     * @return 结果
     */
    public int clearUserFootprint(Long userId);

    /**
     * 统计用户足迹数量
     *
     * @param userId 用户ID
     * @return 足迹数量
     */
    public int countUserFootprint(Long userId);

    /**
     * 删除指定目标的足迹
     *
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 结果
     */
    public int deleteSpecificFootprint(Long userId, Integer targetType, Long targetId);

}
