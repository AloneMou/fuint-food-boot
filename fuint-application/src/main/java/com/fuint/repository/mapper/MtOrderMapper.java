package com.fuint.repository.mapper;

import com.fuint.common.enums.OrderStatusEnum;
import com.fuint.common.enums.TakeStatusEnum;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.order.vo.OrderListReqVO;
import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.model.MtOrder;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.aspectj.weaver.ast.Or;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单表 Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight <a href="https://www.fuint.cn">...</a>
 */
public interface MtOrderMapper extends BaseMapperX<MtOrder> {

    BigDecimal getOrderCount(@Param("merchantId") Integer merchantId);

    BigDecimal getStoreOrderCount(@Param("storeId") Integer storeId);

    BigDecimal getOrderCountByTime(@Param("merchantId") Integer merchantId, @Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    BigDecimal getStoreOrderCountByTime(@Param("storeId") Integer storeId, @Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    MtOrder findByOrderSn(@Param("orderSn") String orderSn);

    BigDecimal getPayMoney(@Param("merchantId") Integer merchantId);

    BigDecimal getPayMoneyByTime(@Param("merchantId") Integer merchantId, @Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    BigDecimal getStorePayMoneyByTime(@Param("storeId") Integer storeId, @Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    BigDecimal getStorePayMoney(@Param("storeId") Integer storeId);

    Integer getPayUserCount(@Param("merchantId") Integer merchantId);

    Integer getStorePayUserCount(@Param("storeId") Integer storeId);

    BigDecimal getUserPayMoney(@Param("userId") Integer userId);

    Integer getUserPayOrderCount(@Param("userId") Integer userId);

    List<MtOrder> getTobeCommissionOrderList(@Param("dateTime") String dateTime);

    /**
     * 分页查询订单列表（使用 MyBatis Plus）
     *
     * @param pageReqVO 分页查询参数
     * @return 分页结果
     */
    default PageResult<MtOrder> selectOrderPage(OrderListReqVO pageReqVO) {
        LambdaQueryWrapperX<MtOrder> queryWrapper = new LambdaQueryWrapperX<MtOrder>()
                .eqIfPresent(MtOrder::getUserId, pageReqVO.getUserId())
                .eqIfPresent(MtOrder::getMerchantId, pageReqVO.getMerchantId())
                .eqIfPresent(MtOrder::getStoreId, pageReqVO.getStoreId())
                .eqIfPresent(MtOrder::getStatus, pageReqVO.getStatus())
                .eqIfPresent(MtOrder::getPayStatus, pageReqVO.getPayStatus())
                .geIfPresent(MtOrder::getCreateTime, pageReqVO.getStartTime())
                .leIfPresent(MtOrder::getCreateTime, pageReqVO.getEndTime())
                .orderByDesc(MtOrder::getCreateTime)
                .orderByDesc(MtOrder::getId);
        return selectPage(pageReqVO, queryWrapper);
    }

    /**
     * 获取待制作的餐品数量
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @param orderTime  订单时间
     * @return 待制作的餐品数量
     */
    default Integer selectToMakeCount(Integer merchantId, Integer storeId, Date orderTime, Integer orderId) {
        return selectCount(new LambdaQueryWrapperX<MtOrder>()
                .eqIfPresent(MtOrder::getMerchantId, merchantId)
                .eqIfPresent(MtOrder::getStoreId, storeId)
                .inIfPresent(MtOrder::getStatus,
                        OrderStatusEnum.PAID.getKey(), OrderStatusEnum.DELIVERY.getKey(),
                        OrderStatusEnum.DELIVERED.getKey(), OrderStatusEnum.RECEIVED.getKey())
                .in(MtOrder::getTakeStatus, TakeStatusEnum.MAKING.getKey(), TakeStatusEnum.WAIT_CONFIRM.getKey(), TakeStatusEnum.MAKING.getKey())
                .le(MtOrder::getPayTime, orderTime)
                .ne(MtOrder::getId, orderId));
    }

}
