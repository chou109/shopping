package com.neuedu.system.mapper;

import java.math.BigDecimal;
import java.util.List;
import com.neuedu.system.domain.Shop;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 店铺信息Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
@Mapper
public interface ShopMapper extends BaseMapper<Shop>
{
    /**
     * 查询店铺信息
     * 
     * @param id 店铺信息主键
     * @return 店铺信息
     */
    public Shop selectShopById(Long id);

    /**
     * 查询店铺信息列表
     *
     * @param shop 店铺信息
     * @return 店铺信息集合
     */
    public List<Shop> selectShopList(Shop shop);

    /**
     * 新增店铺信息
     * 
     * @param shop 店铺信息
     * @return 结果
     */
    public int insertShop(Shop shop);

    /**
     * 修改店铺信息
     * 
     * @param shop 店铺信息
     * @return 结果
     */
    public int updateShop(Shop shop);

    /**
     * 删除店铺信息
     * 
     * @param id 店铺信息主键
     * @return 结果
     */
    public int deleteShopById(Long id);

    /**
     * 批量删除店铺信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteShopByIds(Long[] ids);

    /**
     * 根据用户ID查询店铺
     */
    Shop selectShopByUserId(@Param("userId") Long userId);

    /**
     * 增加店铺销量
     */
    int increaseSales(@Param("shopId") Long shopId, @Param("quantity") Long quantity);

    /**
     * 增加店铺粉丝数
     */
    int increaseFollower(@Param("shopId") Long shopId, @Param("count") Long count);

    /**
     * 减少店铺粉丝数
     */
    int decreaseFollower(@Param("shopId") Long shopId, @Param("count") Long count);

    /**
     * 更新店铺评分
     */
    int updateShopScore(@Param("shopId") Long shopId, @Param("score") BigDecimal score);

    /**
     * 更新店铺状态
     */
    int updateShopStatus(@Param("shopId") Long shopId, @Param("status") Integer status, @Param("updateUser") Long updateUser);

    /**
     * 逻辑删除店铺
     */
    int deleteShopLogicById(@Param("id") Long id);

    /**
     * 批量逻辑删除店铺
     */
    int deleteShopLogicByIds(@Param("ids") Long[] ids);

    /**
     * 热门店铺列表
     */
    List<Shop> selectHotShopList(@Param("limit") Integer limit);

    /**
     * 搜索店铺
     */
    List<Shop> searchShops(@Param("keyword") String keyword, @Param("status") Integer status);
}
