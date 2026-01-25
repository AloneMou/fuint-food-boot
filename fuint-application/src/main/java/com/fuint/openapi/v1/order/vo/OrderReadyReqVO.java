package com.fuint.openapi.v1.order.vo;

import com.fuint.common.enums.TakeStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 标记订单可取餐请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "标记订单可取餐请求VO")
public class OrderReadyReqVO {

    @ApiModelProperty(value = "订单ID", required = true, example = "1")
    @NotNull(message = "订单ID不能为空")
    private Integer orderId;

    @ApiModelProperty(value = "商户ID（用于权限验证）", example = "1")
    private Integer merchantId;

    @NotNull(message = "取餐状态不能为空")
    @ApiModelProperty(value = "取餐状态", allowableValues = "WAIT_CONFIRM,CONFIRM_SUCCESS,MAKING,MAKE_SUCCESS")
    private TakeStatusEnum takeStatus;
}
