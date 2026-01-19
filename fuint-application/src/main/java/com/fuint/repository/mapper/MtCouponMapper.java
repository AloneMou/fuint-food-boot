package com.fuint.repository.mapper;

import com.fuint.repository.model.MtCoupon;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fuint.openapi.v1.marketing.coupon.vo.MtCouponPageReqVO;
import com.fuint.openapi.v1.marketing.coupon.vo.MtCouponRespVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡券信息表 Mapper 接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtCouponMapper extends BaseMapper<MtCoupon> {

    Long queryNumByGroupId(@Param("groupId") Integer groupId);

    List<MtCoupon> queryByGroupId(@Param("groupId") Integer groupId);

    /**
     * 分页查询优惠券列表
     *
     * @param page 分页参数
     * @param req  查询参数
     * @return 分页结果
     */
    IPage<MtCouponRespVO> selectCouponPage(IPage<MtCouponRespVO> page, @Param("req") MtCouponPageReqVO req);

}
