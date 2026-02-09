package com.fuint.repository.mapper;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponPageReqVO;
import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.bean.CouponNumBean;
import com.fuint.repository.model.MtUserCoupon;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会员卡券表 Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtUserCouponMapper extends BaseMapperX<MtUserCoupon> {

    Boolean updateExpireTime(@Param("couponId") Integer couponId, @Param("expireTime") String expireTime);

    Long getSendNum(@Param("couponId") Integer couponId);

    CouponNumBean getPeopleNumByCouponId(@Param("couponId") Integer couponId);

    List<MtUserCoupon> getUserCouponList(@Param("userId") Integer userId, @Param("statusList") List<String> statusList);

    List<MtUserCoupon> getUserCouponListByCouponId(@Param("userId") Integer userId, @Param("couponId") Integer couponId, @Param("statusList") List<String> statusList);

    MtUserCoupon findByCode(@Param("code") String code);

    int removeUserCoupon(@Param("uuid") String uuid, @Param("couponIds") List<Integer> couponIds, @Param("operator") String operator);

    List<MtUserCoupon> queryExpireNumByGroupId(@Param("groupId") Integer groupId);

    List<Integer> getCouponIdsByUuid(@Param("uuid") String uuid);

    List<MtUserCoupon> findUserCouponDetail(@Param("couponId") Integer couponId, @Param("userId") Integer userId);

    List<MtUserCoupon> getUserCouponListByExpireTime(@Param("userId") Integer userId, @Param("status") String status, @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 分页查询用户优惠券列表（使用 MyBatis Plus）
     *
     * @param pageReqVO 分页查询参数
     * @return 分页结果
     */
    default PageResult<MtUserCoupon> selectUserCouponPage(UserCouponPageReqVO pageReqVO) {
        LambdaQueryWrapper<MtUserCoupon> wrapper = new LambdaQueryWrapperX<MtUserCoupon>()
                .eqIfPresent(MtUserCoupon::getUserId, pageReqVO.getUserId())
                .inIfPresent(MtUserCoupon::getStatus, pageReqVO.getStatus())
                .inIfPresent(MtUserCoupon::getType, pageReqVO.getCouponType())
                .eqIfPresent(MtUserCoupon::getCouponId, pageReqVO.getCouponId())
                .eqIfPresent(MtUserCoupon::getUuid, pageReqVO.getUuid())
                .ne(MtUserCoupon::getStatus, StatusEnum.DISABLE.getKey());
        if (CollUtil.isEmpty(pageReqVO.getSortingFields())) {
            wrapper.orderByDesc(MtUserCoupon::getAmount)
                    .orderByDesc(MtUserCoupon::getCreateTime)
                    .orderByDesc(MtUserCoupon::getId);
        }
        return selectPage(pageReqVO, wrapper);
    }
}
