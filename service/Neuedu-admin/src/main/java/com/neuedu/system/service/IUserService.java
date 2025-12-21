package com.neuedu.system.service;

import java.math.BigDecimal;
import java.util.List;
import com.neuedu.system.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户信息Service接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
public interface IUserService extends IService<User>
{
    /**
     * 查询用户信息
     * 
     * @param id 用户信息主键
     * @return 用户信息
     */
    public User selectUserById(Long id);

    /**
     * 根据用户名查询用户
     */
    User selectUserByUserName(String userName);

    /**
     * 更新用户余额
     * @param userId 用户ID
     * @param amount 变动金额（正数为增加，负数为减少）
     * @return 是否成功
     */
    boolean updateUserBalance(Long userId, BigDecimal amount);

    /**
     * 获取用户余额
     */
    BigDecimal getUserBalance(Long userId);

    /**
     * 用户注册
     */
    boolean register(User user);

    /**
     * 修改密码
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    boolean resetPassword(String phone, String newPassword);

    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Long userId, Integer status);

    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 统计用户数量
     */
    Long countUsers();

    /**
     * 根据手机号查询用户
     */
    User selectUserByPhone(String phone);

    /**
     * 查询用户信息列表
     * 
     * @param user 用户信息
     * @return 用户信息集合
     */
    public List<User> selectUserList(User user);

    /**
     * 新增用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    public int insertUser(User user);

    /**
     * 修改用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    public int updateUser(User user);

    /**
     * 批量删除用户信息
     * 
     * @param ids 需要删除的用户信息主键集合
     * @return 结果
     */
    public int deleteUserByIds(Long[] ids);

    /**
     * 删除用户信息信息
     * 
     * @param id 用户信息主键
     * @return 结果
     */
    public int deleteUserById(Long id);

    /**
     * 检查用户名是否存在
     */
    boolean checkUserNameExist(String userName);

    /**
     * 检查手机号是否存在
     */
    boolean checkPhoneExist(String phone);
}
