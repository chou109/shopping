package com.neuedu.system.mapper;

import java.util.List;
import com.neuedu.system.domain.UserFavorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
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
}
