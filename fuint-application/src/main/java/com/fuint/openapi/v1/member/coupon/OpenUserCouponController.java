package com.fuint.openapi.v1.member.coupon;

import com.fuint.common.enums.CouponExpireTypeEnum;
import com.fuint.common.service.UserCouponService;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponRespVO;
import com.fuint.repository.model.MtCoupon;
import com.fuint.repository.model.MtUserCoupon;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/18 22:32
 */
@Validated
@Api(tags = "OpenApi-会员优惠券相关接口")
@RestController
@RequestMapping(value = "/api/v1/member/coupon")
public class OpenUserCouponController {

    @Resource
    private UserCouponService userCouponService;



//
//    /**
//     * 转换MtUserCoupon为响应VO
//     */
//    private UserCouponRespVO convertUserCouponToRespVO(MtUserCoupon userCoupon) {
//        UserCouponRespVO respVO = new UserCouponRespVO();
//        respVO.setUserCouponId(userCoupon.getId());
//        respVO.setCouponId(userCoupon.getCouponId());
//        respVO.setCode(userCoupon.getCode());
//        respVO.setStatus(userCoupon.getStatus());
//        respVO.setAmount(userCoupon.getAmount());
//        respVO.setBalance(userCoupon.getBalance());
//        respVO.setCreateTime(userCoupon.getCreateTime());
//        respVO.setUsedTime(userCoupon.getUsedTime());
//
//        // 获取优惠券详情
//        try {
//            MtCoupon couponInfo = couponService.queryCouponById(userCoupon.getCouponId());
//            if (couponInfo != null) {
//                respVO.setCouponName(couponInfo.getName());
//                respVO.setCouponType(couponInfo.getType());
//
//                // 设置使用门槛说明
//                if (StringUtils.isEmpty(couponInfo.getOutRule()) || couponInfo.getOutRule().equals("0")) {
//                    respVO.setDescription("无使用门槛");
//                } else {
//                    respVO.setDescription("满" + couponInfo.getOutRule() + "元可用");
//                }
//
//                // 设置有效期
//                if (couponInfo.getExpireType().equals(CouponExpireTypeEnum.FIX.getKey())) {
//                    respVO.setEffectiveStartTime(couponInfo.getBeginTime());
//                    respVO.setEffectiveEndTime(couponInfo.getEndTime());
//                } else if (couponInfo.getExpireType().equals(CouponExpireTypeEnum.FLEX.getKey())) {
//                    respVO.setEffectiveStartTime(userCoupon.getCreateTime());
//                    respVO.setEffectiveEndTime(userCoupon.getExpireTime());
//                }
//            }
//        } catch (Exception e) {
//            // 忽略异常，继续处理
//        }
//
//        return respVO;
//    }

}
