package com.neuedu.system.mapper;

import java.util.List;
import com.neuedu.system.domain.UserFootprint;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户足迹Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-22
 */
@Mapper
public interface UserFootprintMapper extends BaseMapper<UserFootprint>
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
     * 删除用户足迹
     * 
     * @param id 用户足迹主键
     * @return 结果
     */
    public int deleteUserFootprintById(Long id);

    /**
     * 批量删除用户足迹
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserFootprintByIds(Long[] ids);


    // ============ 新增的方法 ============

    /**
     * 根据用户ID和目标查询足迹
     *
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 用户足迹
     */
    public UserFootprint selectFootprintByUserAndTarget(@Param("userId") Long userId,
                                                        @Param("targetType") Integer targetType,
                                                        @Param("targetId") Long targetId);

    /**
     * 查询用户的足迹列表
     *
     * @param userId 用户ID
     * @return 足迹列表
     */
    public List<UserFootprint> selectFootprintByUserId(@Param("userId") Long userId,
                                                       @Param("targetType") Integer targetType);

    /**
     * 查询用户最近浏览的足迹
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 足迹列表
     */
    public List<UserFootprint> selectRecentFootprint(@Param("userId") Long userId,
                                                     @Param("limit") Integer limit);

    /**
     * 更新浏览次数
     *
     * @param id 足迹ID
     * @param browseCount 浏览次数
     * @param lastBrowseTime 最后浏览时间
     * @return 结果
     */
    public int updateBrowseInfo(@Param("id") Long id,
                                @Param("browseCount") Long browseCount,
                                @Param("lastBrowseTime") java.util.Date lastBrowseTime);

    /**
     * 清空用户足迹
     *
     * @param userId 用户ID
     * @return 结果
     */
    public int clearUserFootprint(@Param("userId") Long userId);

    public int deleteUserFootprintByIdForUser(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 统计用户足迹数量
     *
     * @param userId 用户ID
     * @return 足迹数量
     */
    public int countUserFootprint(@Param("userId") Long userId);

    /**
     * 删除指定目标的足迹
     *
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 结果
     */
    public int deleteSpecificFootprint(@Param("userId") Long userId,
                                       @Param("targetType") Integer targetType,
                                       @Param("targetId") Long targetId);
}
