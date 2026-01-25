package com.fuint.openapi.v1.order.vo;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fuint.common.dto.*;
import com.fuint.common.enums.*;
import com.fuint.repository.model.MtRefund;
import com.fuint.repository.model.MtStaff;
import com.fuint.repository.model.MtStore;
import com.fuint.repository.model.MtTable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/22 22:58
 */
@Data
public class UserOrderRespVO {

    @ApiModelProperty("自增ID")
    private Integer id;

    @ApiModelProperty(value = "取餐状态", allowableValues = "WAIT_CONFIRM,CONFIRM_SUCCESS,MAKING,MAKE_SUCCESS")
    private TakeStatusEnum takeStatus;

    @ApiModelProperty("前面还有多少杯")
    private Integer queueCount;

    @ApiModelProperty("预计取餐时间/分钟")
    private Integer estimatedWaitTime;

    @ApiModelProperty("商户ID")
    private Integer merchantId;

    @ApiModelProperty("订单号")
    private String orderSn;

    @ApiModelProperty(value = "订单类型", allowableValues = "GOODS,PAYMENT,RECHARGE,PRESTORE,MEMBER")
    private OrderTypeEnum type;

    @ApiModelProperty("订单类型名称")
    private String typeName;

    @ApiModelProperty(value = "支付类型", allowableValues = "CASH,JSAPI,MICROPAY,BALANCE,ALISCAN,OPEN_API")
    private PayTypeEnum payType;

    @ApiModelProperty(value = "订单模式", allowableValues = "EXPRESS,ONESELF")
    private OrderModeEnum orderMode;

    @ApiModelProperty("是否核销")
    private Boolean isVerify;

    @ApiModelProperty("卡券ID")
    private Integer couponId;

    @ApiModelProperty("会员ID")
    private Integer userId;

    @ApiModelProperty("是否游客")
    private String isVisitor;

    @ApiModelProperty("核销码")
    private String verifyCode;

    @ApiModelProperty("员工ID")
    private Integer staffId;

    @ApiModelProperty("总金额")
    private BigDecimal amount;

    @ApiModelProperty("支付金额")
    private BigDecimal payAmount;

    @ApiModelProperty("优惠金额")
    private BigDecimal discount;

    @ApiModelProperty("配送费用")
    private BigDecimal deliveryFee;

    @ApiModelProperty("使用积分")
    private Integer usePoint;

    @ApiModelProperty("积分金额")
    private BigDecimal pointAmount;

    @ApiModelProperty("订单参数")
    private String param;

    @ApiModelProperty("备注信息")
    private String remark;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty("创建时间")
    private Date createTime;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty("更新时间")
    private Date updateTime;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty("支付时间")
    private Date payTime;

    @ApiModelProperty(value = "订单状态", allowableValues = "CREATED,PAID,CANCEL,DELIVERY,DELIVERED,RECEIVED,DELETED,REFUND")
    private OrderStatusEnum status;

    @ApiModelProperty(value = "支付状态", allowableValues = "WAIT,SUCCESS")
    private PayStatusEnum payStatus;

    @ApiModelProperty(value = "结算状态", allowableValues = "WAIT,COMPLETE")
    private SettleStatusEnum settleStatus;

    @ApiModelProperty("状态说明")
    private String statusText;

    @ApiModelProperty("最后操作人")
    private String operator;

    @ApiModelProperty("订单商品列表")
    private List<OrderGoodsDto> goods;

    @ApiModelProperty("下单用户信息")
    private OrderUserRespVO userInfo;

    @ApiModelProperty("配送地址")
    private AddressRespVO address;

    @ApiModelProperty("物流信息")
    private ExpressRespVO expressInfo;

    @ApiModelProperty("所属店铺信息")
    private OrderStoreRespVO storeInfo;

    @ApiModelProperty("所属桌码信息")
    private OrderTableRespVO tableInfo;

    @ApiModelProperty("售后订单")
    private OrderRefundRespVO refundInfo;

    @ApiModelProperty("使用卡券")
    private UserCouponDto couponInfo;

    @ApiModelProperty("所属员工")
    private OrderStaffRespVO staffInfo;

    @Data
    public static class OrderStaffRespVO {

        @ApiModelProperty(value = "员工ID")
        private Integer id;

        @ApiModelProperty(value = "员工姓名")
        private String realName;

        @ApiModelProperty(value = "员工手机号")
        private String mobile;

        @ApiModelProperty("商户ID")
        private Integer merchantId;

        @ApiModelProperty("店铺ID")
        private Integer storeId;

        @ApiModelProperty("用户ID")
        private Integer userId;
    }

    @Data
    public static class OrderRefundRespVO {

        @ApiModelProperty(value = "售后ID")
        private Integer id;

        @ApiModelProperty("订单ID")
        private Integer orderId;

        @ApiModelProperty("商户ID")
        private Integer merchantId;

        @ApiModelProperty("店铺ID")
        private Integer storeId;

        @ApiModelProperty("会员ID")
        private Integer userId;

        @ApiModelProperty("退款金额")
        private BigDecimal amount;

        @ApiModelProperty("售后类型")
        private String type;

        @ApiModelProperty("退款备注")
        private String remark;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        @ApiModelProperty("创建时间")
        private Date createTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        @ApiModelProperty("更新时间")
        private Date updateTime;

        @ApiModelProperty("状态")
        private String status;

        @ApiModelProperty("图片")
        private String images;

        @ApiModelProperty("最后操作人")
        private String operator;
    }

    @Data
    public static class OrderTableRespVO {

        @ApiModelProperty("自增ID")
        private Integer id;

        @ApiModelProperty("桌子编码")
        private String code;

        @ApiModelProperty("所属商户ID")
        private Integer merchantId;

        @ApiModelProperty("所属店铺ID")
        private Integer storeId;

        @ApiModelProperty("人数上限")
        private Integer maxPeople;

        @ApiModelProperty("备注信息")
        private String description;

    }

    @Data
    public static class OrderStoreRespVO {

        @TableId(value = "ID", type = IdType.AUTO)
        private Integer id;

        @ApiModelProperty("所属商户ID")
        private Integer merchantId;

        @ApiModelProperty("店铺名称")
        private String name;

        @ApiModelProperty("商户logo")
        private String logo;

        @ApiModelProperty("店铺二维码")
        private String qrCode;

        @ApiModelProperty("是否默认")
        private String isDefault;

        @ApiModelProperty("联系人姓名")
        private String contact;

        @ApiModelProperty("联系电话")
        private String phone;

        @ApiModelProperty("地址")
        private String address;

        @ApiModelProperty("经度")
        private Double latitude;

        @ApiModelProperty("维度")
        private Double longitude;

        @ApiModelProperty("营业时间")
        private String hours;

        @ApiModelProperty("营业执照")
        private String license;

        @ApiModelProperty("统一社会信用代码")
        private String creditCode;

        @ApiModelProperty("备注信息")
        private String description;
    }

    @Data
    public static class OrderUserRespVO {

        @ApiModelProperty("会员ID")
        private Integer id;

        @ApiModelProperty("会员号")
        private String no;

        @ApiModelProperty("会员姓名")
        private String name;

        @ApiModelProperty("会员手机")
        private String mobile;
    }


    @Data
    public static class ExpressRespVO {

        @ApiModelProperty("物流公司")
        private String expressCompany;

        @ApiModelProperty("物流单号")
        private String expressNo;

        @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
        @ApiModelProperty("发货时间")
        private Date expressTime;

    }

    @Data
    public static class AddressRespVO {

        @ApiModelProperty("账户主键ID")
        private Integer id;

        @ApiModelProperty("会员ID")
        private Integer userId;

        @ApiModelProperty("会员名称")
        private String name;

        @ApiModelProperty("会员手机号")
        private String mobile;

        @ApiModelProperty("省份ID")
        private Integer provinceId;

        @ApiModelProperty("省份名称")
        private String provinceName;

        @ApiModelProperty("城市ID")
        private Integer cityId;

        @ApiModelProperty("城市名称")
        private String cityName;

        @ApiModelProperty("区ID")
        private Integer regionId;

        @ApiModelProperty("区名称")
        private String regionName;

        @ApiModelProperty("详细地址")
        private String detail;

        @ApiModelProperty("是否默认地址")
        private String isDefault;
    }
}
