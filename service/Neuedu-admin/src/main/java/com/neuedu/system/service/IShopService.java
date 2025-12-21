package com.neuedu.system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.neuedu.system.domain.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 店铺信息Service接口
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
public interface IShopService extends IService<Shop>
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
     * 批量删除店铺信息
     * 
     * @param ids 需要删除的店铺信息主键集合
     * @return 结果
     */
    public int deleteShopByIds(Long[] ids);

    /**
     * 删除店铺信息信息
     * 
     * @param id 店铺信息主键
     * @return 结果
     */
    public int deleteShopById(Long id);



    /**
     * 根据用户ID查询店铺
     */
    Shop selectShopByUserId(Long userId);

    /**
     * 获取我的店铺
     */
    Shop getMyShop();

    /**
     * 增加店铺销量
     */
    int increaseSales(Long shopId, Long quantity);

    /**
     * 增加店铺粉丝数
     */
    int increaseFollower(Long shopId);

    /**
     * 减少店铺粉丝数
     */
    int decreaseFollower(Long shopId);

    /**
     * 更新店铺评分
     */
    int updateShopScore(Long shopId, BigDecimal newScore);

    /**
     * 更新店铺状态
     */
    int updateShopStatus(Long shopId, Integer status);

    /**
     * 逻辑删除店铺
     */
    int deleteShopLogicById(Long id);

    /**
     * 批量逻辑删除店铺
     */
    int deleteShopLogicByIds(Long[] ids);

    /**
     * 热门店铺列表
     */
    List<Shop> getHotShopList(Integer limit);

    /**
     * 搜索店铺
     */
    List<Shop> searchShops(String keyword, Integer status);

    /**
     * 获取店铺统计信息
     */
    Map<String, Object> getShopStatistics(Long shopId);
}
