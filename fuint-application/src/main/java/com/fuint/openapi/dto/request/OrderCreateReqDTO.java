package com.fuint.openapi.dto.request;

import com.fuint.common.enums.OrderTypeEnum;
import com.fuint.openapi.dto.api.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Order Creation Request DTO
 * Redesigned for explicit validation and structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "OrderCreateReqDTO", description = "New architecture order creation request")
public class OrderCreateReqDTO implements BaseRequest {

    @NotNull(message = "User ID cannot be null")
    @Min(value = 1, message = "User ID must be positive")
    @ApiModelProperty(value = "User ID", required = true, example = "1")
    private Integer userId;

    @ApiModelProperty(value = "Merchant ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "Store ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "Order Type", example = "googs")
    private OrderTypeEnum type;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    @ApiModelProperty(value = "List of order items")
    private List<OrderItemDTO> items;

    @ApiModelProperty(value = "User Coupon ID", example = "1")
    private Integer userCouponId;

    @Min(value = 0, message = "Points cannot be negative")
    @ApiModelProperty(value = "Points to use", example = "100")
    private Integer usePoint;

    @ApiModelProperty(value = "Order Mode: express/oneself", example = "oneself")
    private String orderMode;

    @ApiModelProperty(value = "Remark", example = "Less sugar")
    private String remark;

    @ApiModelProperty(value = "Table ID", example = "1")
    private Integer tableId;

    @ApiModelProperty(value = "Platform", example = "MP-WEIXIN")
    private String platform;

    @NotNull(message = "Pre-calculated total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @ApiModelProperty(value = "Pre-calculated total amount for validation", required = true, example = "100.00")
    private BigDecimal preTotalAmount;

    @ApiModelProperty(value = "Payment Type: BALANCE, JSAPI, ALISCAN", example = "BALANCE")
    private String payType;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "OrderItemDTO")
    public static class OrderItemDTO {
        @NotNull(message = "Goods ID is required")
        private Integer goodsId;

        @NotNull(message = "SKU ID is required")
        private Integer skuId;

        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer num;
        
        @ApiModelProperty(value = "Specification IDs")
        private String specIds;
    }
}
