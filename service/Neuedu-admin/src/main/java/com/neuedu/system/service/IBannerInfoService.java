package com.neuedu.system.service;

import java.util.List;
import com.neuedu.system.domain.BannerInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 【请填写功能名称】Service接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
public interface IBannerInfoService extends IService<BannerInfo>
{
    /**
     * 查询【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    public BannerInfo selectBannerInfoById(Long id);

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param bannerInfo 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<BannerInfo> selectBannerInfoList(BannerInfo bannerInfo);

    /**
     * 新增【请填写功能名称】
     * 
     * @param bannerInfo 【请填写功能名称】
     * @return 结果
     */
    public int insertBannerInfo(BannerInfo bannerInfo);

    /**
     * 修改【请填写功能名称】
     * 
     * @param bannerInfo 【请填写功能名称】
     * @return 结果
     */
    public int updateBannerInfo(BannerInfo bannerInfo);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的【请填写功能名称】主键集合
     * @return 结果
     */
    public int deleteBannerInfoByIds(Long[] ids);

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteBannerInfoById(Long id);
}
