package com.neuedu.system.service.impl;
import lombok.RequiredArgsConstructor;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.UserFavoriteMapper;
import com.neuedu.system.domain.UserFavorite;
import com.neuedu.system.service.IUserFavoriteService;

/**
 * 用户收藏Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements IUserFavoriteService
{
    private final UserFavoriteMapper userFavoriteMapper;

    /**
     * 查询用户收藏
     * 
     * @param favoriteId 用户收藏主键
     * @return 用户收藏
     */
    @Override
    public UserFavorite selectUserFavoriteByFavoriteId(Long favoriteId)
    {
        return userFavoriteMapper.selectUserFavoriteByFavoriteId(favoriteId);
    }

    /**
     * 查询用户收藏列表
     * 
     * @param userFavorite 用户收藏
     * @return 用户收藏
     */
    @Override
    public List<UserFavorite> selectUserFavoriteList(UserFavorite userFavorite)
    {
        return userFavoriteMapper.selectUserFavoriteList(userFavorite);
    }

    /**
     * 新增用户收藏
     * 
     * @param userFavorite 用户收藏
     * @return 结果
     */
    @Override
    public int insertUserFavorite(UserFavorite userFavorite)
    {
        userFavorite.setCreateTime(DateUtils.getNowDate());
        return userFavoriteMapper.insertUserFavorite(userFavorite);
    }

    /**
     * 修改用户收藏
     * 
     * @param userFavorite 用户收藏
     * @return 结果
     */
    @Override
    public int updateUserFavorite(UserFavorite userFavorite)
    {
        return userFavoriteMapper.updateUserFavorite(userFavorite);
    }

    /**
     * 批量删除用户收藏
     * 
     * @param favoriteIds 需要删除的用户收藏主键
     * @return 结果
     */
    @Override
    public int deleteUserFavoriteByFavoriteIds(Long[] favoriteIds)
    {
        return userFavoriteMapper.deleteUserFavoriteByFavoriteIds(favoriteIds);
    }

    /**
     * 删除用户收藏信息
     * 
     * @param favoriteId 用户收藏主键
     * @return 结果
     */
    @Override
    public int deleteUserFavoriteByFavoriteId(Long favoriteId)
    {
        return userFavoriteMapper.deleteUserFavoriteByFavoriteId(favoriteId);
    }
}
