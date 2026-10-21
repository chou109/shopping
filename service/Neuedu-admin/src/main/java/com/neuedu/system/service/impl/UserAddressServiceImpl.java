package com.neuedu.system.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neuedu.common.utils.StringUtils;
import com.neuedu.system.constants.AddressConstants;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.UserAddressMapper;
import com.neuedu.system.domain.UserAddress;
import com.neuedu.system.service.IUserAddressService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户地址Service业务层处理
 *
 * @author ruoyi
 * @date 2025-12-13
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements IUserAddressService
{
    private final UserAddressMapper userAddressMapper;
    // 直辖市列表
    private static final Set<String> MUNICIPALITIES = new HashSet<>(Arrays.asList(
            "北京市", "上海市", "天津市", "重庆市",
            "北京", "上海", "天津", "重庆"
    ));
    /**
     * 查询用户地址
     *
     * @param id 用户地址主键
     * @return 用户地址
     */
    @Override
    public UserAddress selectUserAddressById(Long id)
    {
        UserAddress address = userAddressMapper.selectUserAddressById(id);
        if (address != null) {
            enrichAddressInfo(address);
        }
        return address;
    }

    /**
     * 根据地址ID查询地址
     *
     * @param addressId 地址ID
     * @return 地址信息
     */
    @Override
    public UserAddress selectUserAddressByAddressId(Long addressId)
    {
        UserAddress address = userAddressMapper.selectUserAddressByAddressId(addressId);
        if (address != null) {
            enrichAddressInfo(address);
        }
        return address;
    }

    /**
     * 查询用户的默认地址 - 修改为使用 create_user
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    @Override
    public UserAddress selectDefaultAddressByUserId(Long userId)
    {
        UserAddress address = userAddressMapper.selectDefaultAddressByUserId(userId);
        if (address != null) {
            enrichAddressInfo(address);
        }
        return address;
    }

    /**
     * 查询用户地址列表
     *
     * @param userAddress 用户地址
     * @return 用户地址
     */
    @Override
    public List<UserAddress> selectUserAddressList(UserAddress userAddress)
    {
        List<UserAddress> list = userAddressMapper.selectUserAddressList(userAddress);
        list.forEach(this::enrichAddressInfo);
        return list;
    }

    /**
     * 查询用户的所有地址列表 - 修改为使用 create_user
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    @Override
    public List<UserAddress> selectUserAddressListByUserId(Long userId) {
        UserAddress query = new UserAddress();
        query.setCreateUser(userId);
        List<UserAddress> list = userAddressMapper.selectUserAddressList(query);
        list.forEach(address -> {
            // 设置userId到对象中供前端使用
            address.setUserId(userId);
            enrichAddressInfo(address);
        });
        return list;
    }


    /**
     * 新增用户地址
     *
     * @param userAddress 地址信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUserAddress(UserAddress userAddress)
    {
        // 1. 验证地址信息
        if (!validateAddress(userAddress)) {
            throw new RuntimeException("地址信息不完整");
        }

        // 2. 生成 address_id（如果没有提供）
        if (userAddress.getAddressId() == null) {
            // 使用雪花算法或简单的时间戳生成唯一ID
            userAddress.setAddressId(System.currentTimeMillis());

            // 或者使用随机数
            // userAddress.setAddressId(IdUtil.getSnowflakeNextId());
        }

        // 3. 设置默认值
        userAddress.setCreateTime(DateUtils.getNowDate());
        userAddress.setDeleteFlag(AddressConstants.DELETE_FLAG_NO);

        // 4. 将userId设置到createUser字段
        if (userAddress.getUserId() != null) {
            userAddress.setCreateUser(userAddress.getUserId());
        }

        // 5. 如果是设置默认地址，需要取消其他默认地址
        if (userAddress.getDefaultFlag() != null &&
                userAddress.getDefaultFlag() == AddressConstants.DEFAULT_FLAG_YES) {
            cancelOtherDefaultAddress(userAddress.getUserId());
        } else {
            // 如果用户没有地址，则第一个地址自动设为默认
            int addressCount = countUserAddress(userAddress.getUserId());
            if (addressCount == 0) {
                userAddress.setDefaultFlag(AddressConstants.DEFAULT_FLAG_YES);
            } else {
                userAddress.setDefaultFlag(AddressConstants.DEFAULT_FLAG_NO);
            }
        }

        return userAddressMapper.insertUserAddress(userAddress);
    }


    /**
     * 设置默认地址 - 修改为使用 create_user
     *
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setDefaultAddress(Long addressId, Long userId)
    {
        // 1. 检查地址是否存在且属于该用户
        UserAddress address = userAddressMapper.selectUserAddressById(addressId);
        if (address == null || !address.getCreateUser().equals(userId)) {
            throw new RuntimeException("地址不存在或不属于该用户");
        }

        // 2. 取消所有默认地址
        int cancelResult = userAddressMapper.cancelAllDefaultAddress(userId);

        // 3. 设置新的默认地址
        int setResult = userAddressMapper.setDefaultAddress(addressId, userId);

        return cancelResult + setResult;
    }


    /**
     * 取消其他默认地址 - 修改为使用 create_user
     */
    private void cancelOtherDefaultAddress(Long userId) {
        userAddressMapper.cancelAllDefaultAddress(userId);
    }

    /**
     * 修改用户地址
     *
     * @param userAddress 用户地址
     * @return 结果
     */
    @Override
    public int updateUserAddress(UserAddress userAddress)
    {
        // 1. 验证地址信息
        if (!validateAddress(userAddress)) {
            throw new RuntimeException("地址信息不完整");
        }

        // 2. 如果是设置默认地址，需要取消其他默认地址
        if (userAddress.getDefaultFlag() != null &&
                userAddress.getDefaultFlag() == AddressConstants.DEFAULT_FLAG_YES) {
            // 先查询当前地址信息
            UserAddress existingAddress = selectUserAddressById(userAddress.getId());
            if (existingAddress != null &&
                    existingAddress.getDefaultFlag() != AddressConstants.DEFAULT_FLAG_YES) {
                cancelOtherDefaultAddress(existingAddress.getUserId());
            }
        }

        userAddress.setUpdateTime(DateUtils.getNowDate());
        return userAddressMapper.updateUserAddress(userAddress);
    }

    /**
     * 批量删除用户地址
     *
     * @param ids 需要删除的用户地址主键
     * @return 结果
     */
    @Override
    public int deleteUserAddressByIds(Long[] ids)
    {
        // 检查是否有默认地址，默认地址不能删除
        for (Long id : ids) {
            UserAddress address = selectUserAddressById(id);
            if (address != null && address.getDefaultFlag() == AddressConstants.DEFAULT_FLAG_YES) {
                throw new RuntimeException("默认地址不能删除，请先设置其他地址为默认");
            }
        }

        return userAddressMapper.deleteUserAddressByIds(ids);
    }

    /**
     * 删除用户地址信息
     *
     * @param id 用户地址主键
     * @return 结果
     */
    @Override
    public int deleteUserAddressById(Long id)
    {
        UserAddress address = selectUserAddressById(id);
        if (address != null && address.getDefaultFlag() == AddressConstants.DEFAULT_FLAG_YES) {
            throw new RuntimeException("默认地址不能删除，请先设置其他地址为默认");
        }

        return userAddressMapper.deleteUserAddressById(id);
    }


    /**
     * 获取用户的地址数量 - 修改为使用 create_user
     *
     * @param userId 用户ID
     * @return 地址数量
     */
    @Override
    public int countUserAddress(Long userId) {
        return userAddressMapper.countByUserId(userId);
    }

    /**
     * 丰富地址信息（添加扩展字段）
     */
    private void enrichAddressInfo(UserAddress address) {
        // 调用初始化方法设置扩展字段
        if (address != null) {
            // 设置userId到对象中供前端使用
            address.setUserId(address.getCreateUser());
            address.initExtraFields();
        }
    }


    /**
     * 验证地址信息是否完整（严格模式）- 修改后版本
     * 注意：这个方法是旧版本，建议使用灵活模式
     *
     * @param userAddress 地址信息
     * @return 验证结果
     */
    @Override
    public boolean validateAddress(UserAddress userAddress) {
        // 重定向到灵活模式
        return validateAddressFlexible(userAddress);
    }

    /**
     * 验证地址信息是否完整（灵活模式）
     * 1. 对于直辖市：需要省份和区，城市可选（可以为空或与省份相同）
     * 2. 对于普通省份：需要省份和城市，区可选
     *
     * @param userAddress 地址信息
     * @return 验证结果
     */
    @Override
    public boolean validateAddressFlexible(UserAddress userAddress) {
        if (userAddress == null) {
            return false;
        }

        // 1. 验证收货人姓名
        if (StringUtils.isEmpty(userAddress.getReceiveName())) {
            return false;
        }

        // 2. 验证手机号
        if (StringUtils.isEmpty(userAddress.getReceivePhone())) {
            return false;
        }

        // 手机号格式验证（简单验证）
        String phone = userAddress.getReceivePhone();
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return false;
        }

        // 3. 验证省份（必填）
        if (StringUtils.isEmpty(userAddress.getProvince())) {
            return false;
        }

        String province = userAddress.getProvince().trim();
        String city = userAddress.getCity() != null ? userAddress.getCity().trim() : "";
        String area = userAddress.getArea() != null ? userAddress.getArea().trim() : "";

        // 4. 根据是否为直辖市进行不同的验证逻辑
        boolean isMunicipality = isMunicipality(province);

        if (isMunicipality) {
            // 直辖市验证逻辑
            // 直辖市可以没有城市字段，或者城市字段与省份相同（如北京-北京）
            // 如果有城市字段，需要与省份匹配
            if (StringUtils.isNotEmpty(city) && !city.equals(province) && !city.contains(province)) {
                // 直辖市下的城市名应该与省份名相同或包含省份名
                // 例如：北京 -> 北京市，上海 -> 上海市
                return false;
            }

            // 直辖市必须有区级信息
            if (StringUtils.isEmpty(area)) {
                return false;
            }
        } else {
            // 普通省份验证逻辑
            // 必须有城市信息
            if (StringUtils.isEmpty(city)) {
                return false;
            }

            // 普通省份的区级信息可选，但建议有
            // 这里不强制要求区级信息
        }

        // 5. 验证详细地址（必填）
        if (StringUtils.isEmpty(userAddress.getStreet())) {
            return false;
        }

        // 6. 验证详细地址长度
        String street = userAddress.getStreet().trim();
        if (street.length() < 5 || street.length() > 200) {
            return false;
        }

        // 7. 邮政编码可选，如果有则验证格式
        if (StringUtils.isNotEmpty(userAddress.getPostalCode())) {
            String postalCode = userAddress.getPostalCode();
            if (!postalCode.matches("^\\d{6}$")) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查是否为直辖市
     *
     * @param province 省份名称
     * @return 是否为直辖市
     */
    @Override
    public boolean isMunicipality(String province) {
        if (StringUtils.isEmpty(province)) {
            return false;
        }

        String normalizedProvince = province.trim();

        // 直接匹配
        if (MUNICIPALITIES.contains(normalizedProvince)) {
            return true;
        }

        // 模糊匹配（包含关系）
        for (String municipality : MUNICIPALITIES) {
            if (normalizedProvince.contains(municipality) || municipality.contains(normalizedProvince)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取地址的完整格式
     *
     * @param userAddress 地址信息
     * @return 完整地址字符串
     */
    @Override
    public String getFullAddress(UserAddress userAddress) {
        if (userAddress == null) {
            return "";
        }

        StringBuilder fullAddress = new StringBuilder();

        // 省份
        if (StringUtils.isNotEmpty(userAddress.getProvince())) {
            fullAddress.append(userAddress.getProvince());
        }

        // 城市
        if (StringUtils.isNotEmpty(userAddress.getCity())) {
            // 如果是直辖市且城市与省份相同，不重复添加
            if (!(isMunicipality(userAddress.getProvince()) &&
                    userAddress.getCity().equals(userAddress.getProvince()))) {
                if (fullAddress.length() > 0) {
                    fullAddress.append(" ");
                }
                fullAddress.append(userAddress.getCity());
            }
        }

        // 区县
        if (StringUtils.isNotEmpty(userAddress.getArea())) {
            if (fullAddress.length() > 0) {
                fullAddress.append(" ");
            }
            fullAddress.append(userAddress.getArea());
        }

        // 详细地址
        if (StringUtils.isNotEmpty(userAddress.getStreet())) {
            if (fullAddress.length() > 0) {
                fullAddress.append(" ");
            }
            fullAddress.append(userAddress.getStreet());
        }

        return fullAddress.toString();
    }
}
