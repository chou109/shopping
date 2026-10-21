package com.neuedu.system.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.neuedu.system.domain.RechargeInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 【请填写功能名称】Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Mapper
public interface RechargeInfoMapper extends BaseMapper<RechargeInfo>
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
     * 根据支付流水号查询
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

    Map<String, Object> selectRechargeStatistics(@Param("beginDate") String beginDate,
            @Param("endDate") String endDate);



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
     * 删除【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteRechargeInfoById(Long id);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRechargeInfoByIds(Long[] ids);

    /**
     * 更新充值状态
     *
     * @param id 充值记录ID
     * @param status 状态
     * @param paymentTime 支付时间
     * @param paymentNo 支付流水号
     * @return 结果
     */
    public int updateRechargeStatus(@Param("id") Long id,
                                    @Param("status") Integer status,
                                    @Param("paymentTime") java.util.Date paymentTime,
                                    @Param("paymentNo") String paymentNo);

}
