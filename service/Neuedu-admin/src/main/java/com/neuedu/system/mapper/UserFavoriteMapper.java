package com.neuedu.system.mapper;

import java.util.List;
import com.neuedu.system.domain.UserFavorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户收藏Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite>
{
    /**
     * 查询用户收藏
     * 
     * @param favoriteId 用户收藏主键
     * @return 用户收藏
     */
    public UserFavorite selectUserFavoriteByFavoriteId(Long favoriteId);

    /**
     * 查询用户收藏列表
     * 
     * @param userFavorite 用户收藏
     * @return 用户收藏集合
     */
    public List<UserFavorite> selectUserFavoriteList(UserFavorite userFavorite);

    /**
     * 新增用户收藏
     * 
     * @param userFavorite 用户收藏
     * @return 结果
     */
    public int insertUserFavorite(UserFavorite userFavorite);

    /**
     * 修改用户收藏
     * 
     * @param userFavorite 用户收藏
     * @return 结果
     */
    public int updateUserFavorite(UserFavorite userFavorite);

    /**
     * 删除用户收藏
     * 
     * @param favoriteId 用户收藏主键
     * @return 结果
     */
    public int deleteUserFavoriteByFavoriteId(Long favoriteId);

    /**
     * 批量删除用户收藏
     * 
     * @param favoriteIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserFavoriteByFavoriteIds(Long[] favoriteIds);


    // ============ 新增的方法 ============

    /**
     * 根据用户ID和收藏目标查询收藏
     */
    UserFavorite selectByUserAndTarget(@Param("userId") Long userId,
                                       @Param("targetType") Integer targetType,
                                       @Param("targetId") Long targetId);

    /**
     * 查询用户的收藏列表（按类型）
     */
    List<UserFavorite> selectUserFavorites(@Param("userId") Long userId,
                                           @Param("targetType") Integer targetType);

    /**
     * 检查用户是否已收藏
     */
    boolean existsFavorite(@Param("userId") Long userId,
                           @Param("targetType") Integer targetType,
                           @Param("targetId") Long targetId);

    /**
     * 删除用户的收藏
     */
    int deleteByUserAndTarget(@Param("userId") Long userId,
                              @Param("targetType") Integer targetType,
                              @Param("targetId") Long targetId);

    /**
     * 统计用户的收藏数量
     */
    Long countUserFavorites(@Param("userId") Long userId,
                            @Param("targetType") Integer targetType);

    /**
     * 统计目标的被收藏数量
     */
    Long countTargetFavorites(@Param("targetType") Integer targetType,
                              @Param("targetId") Long targetId);
}
