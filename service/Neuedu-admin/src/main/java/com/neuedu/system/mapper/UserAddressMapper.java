package com.neuedu.system.mapper;

import java.util.List;
import com.neuedu.system.domain.UserAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
/**
 * 用户地址Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress>
{
    /**
     * 查询用户地址
     * 
     * @param id 用户地址主键
     * @return 用户地址
     */
    public UserAddress selectUserAddressById(Long id);

    /**
     * 根据地址ID查询地址
     *
     * @param addressId 地址ID
     * @return 地址信息
     */
    UserAddress selectUserAddressByAddressId(Long addressId);

    /**
     * 查询用户地址列表
     * 
     * @param userAddress 用户地址
     * @return 用户地址集合
     */
    public List<UserAddress> selectUserAddressList(UserAddress userAddress);

    /**
     * 查询用户的默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    UserAddress selectDefaultAddressByUserId(Long userId);

    /**
     * 新增用户地址
     * 
     * @param userAddress 用户地址
     * @return 结果
     */
    public int insertUserAddress(UserAddress userAddress);

    /**
     * 修改用户地址
     * 
     * @param userAddress 用户地址
     * @return 结果
     */
    public int updateUserAddress(UserAddress userAddress);

    /**
     * 删除用户地址
     * 
     * @param id 用户地址主键
     * @return 结果
     */
    public int deleteUserAddressById(Long id);

    /**
     * 批量删除用户地址
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserAddressByIds(Long[] ids);

    /**
     * 取消所有默认地址
     *
     * @param userId 用户ID
     * @return 结果
     */
    int cancelAllDefaultAddress(Long userId);

    /**
     * 设置默认地址
     *
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 结果
     */
    int setDefaultAddress(@Param("addressId") Long addressId, @Param("userId") Long userId);

    /**
     * 根据用户ID统计地址数量
     *
     * @param userId 用户ID
     * @return 地址数量
     */
    int countByUserId(@Param("userId") Long userId);
}
