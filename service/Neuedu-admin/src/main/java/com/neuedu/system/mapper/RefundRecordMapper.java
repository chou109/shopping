package com.neuedu.system.mapper;

import java.util.List;
import com.neuedu.system.domain.RefundRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
/**
 * 退款记录Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-16
 */
@Mapper
public interface RefundRecordMapper extends BaseMapper<RefundRecord>
{
    /**
     * 查询退款记录
     * 
     * @param id 退款记录主键
     * @return 退款记录
     */
    public RefundRecord selectRefundRecordById(Long id);

    /**
     * 查询退款记录列表
     * 
     * @param refundRecord 退款记录
     * @return 退款记录集合
     */
    public List<RefundRecord> selectRefundRecordList(RefundRecord refundRecord);

    /**
     * 根据退款单号查询退款记录
     *
     * @param refundNo 退款单号
     * @return 退款记录
     */
    RefundRecord selectRefundRecordByRefundNo(String refundNo);

    /**
     * 新增退款记录
     * 
     * @param refundRecord 退款记录
     * @return 结果
     */
    public int insertRefundRecord(RefundRecord refundRecord);

    /**
     * 修改退款记录
     * 
     * @param refundRecord 退款记录
     * @return 结果
     */
    public int updateRefundRecord(RefundRecord refundRecord);

    /**
     * 删除退款记录
     * 
     * @param id 退款记录主键
     * @return 结果
     */
    public int deleteRefundRecordById(Long id);

    /**
     * 批量删除退款记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRefundRecordByIds(Long[] ids);
}
