package com.neuedu.system.service;

import java.math.BigDecimal;
import java.util.List;
import com.neuedu.system.domain.RechargeInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 【请填写功能名称】Service接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
public interface IRechargeInfoService extends IService<RechargeInfo>
{
    /**
     * 查询【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    public RechargeInfo selectRechargeInfoById(Long id);

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param rechargeInfo 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<RechargeInfo> selectRechargeInfoList(RechargeInfo rechargeInfo);

    /**
     * 根据用户ID查询充值记录
     *
     * @param userId 用户ID
     * @return 充值记录列表
     */
    public List<RechargeInfo> selectRechargeInfoByUserId(Long userId);

    /**
     * 根据订单号查询充值记录
     *
     * @param orderNo 订单编号
     * @return 充值记录
     */
    public RechargeInfo selectRechargeInfoByOrderNo(String orderNo);

    /**
     * 根据支付流水号查询充值记录
     *
     * @param paymentNo 支付流水号
     * @return 充值记录
     */
    public RechargeInfo selectRechargeInfoByPaymentNo(String paymentNo);

    /**
     * 统计用户充值总金额
     *
     * @param userId 用户ID
     * @return 总金额
     */
    public BigDecimal selectTotalRechargeByUserId(Long userId);

    /**
     * 处理支付成功回调
     *
     * @param orderNo 订单编号
     * @param paymentNo 支付流水号
     * @param paymentType 支付方式
     * @return 是否成功
     */
    public boolean handlePaymentSuccess(String orderNo, String paymentNo, Integer paymentType);

    /**
     * 处理支付失败
     *
     * @param orderNo 订单编号
     * @param paymentNo 支付流水号
     * @return 是否成功
     */
    public boolean handlePaymentFailed(String orderNo, String paymentNo);

    /**
     * 退款处理
     *
     * @param id 充值记录ID
     * @return 是否成功
     */
    public boolean handleRefund(Long id);


    /**
     * 创建充值订单
     *
     * @param rechargeInfo 充值信息
     * @return 充值记录ID
     */
    public Long createRechargeOrder(RechargeInfo rechargeInfo);

    /**
     * 新增【请填写功能名称】
     * 
     * @param rechargeInfo 【请填写功能名称】
     * @return 结果
     */
    public int insertRechargeInfo(RechargeInfo rechargeInfo);


    /**
     * 修改【请填写功能名称】
     * 
     * @param rechargeInfo 【请填写功能名称】
     * @return 结果
     */
    public int updateRechargeInfo(RechargeInfo rechargeInfo);

    /**
     * 更新充值状态
     *
     * @param id 充值记录ID
     * @param status 状态
     * @param paymentTime 支付时间
     * @param paymentNo 支付流水号
     * @return 结果
     */
    public int updateRechargeStatus(Long id, Integer status, java.util.Date paymentTime, String paymentNo);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的【请填写功能名称】主键集合
     * @return 结果
     */
    public int deleteRechargeInfoByIds(Long[] ids);

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteRechargeInfoById(Long id);
}
