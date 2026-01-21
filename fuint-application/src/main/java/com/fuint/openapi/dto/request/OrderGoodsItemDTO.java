package com.fuint.openapi.dto.request;

import com.fuint.openapi.dto.api.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Order Goods Item DTO
 * <p>
 * New architecture: Independent DTO, strict validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "OrderGoodsItemDTO", description = "Order Goods Item Data Transfer Object")
public class OrderGoodsItemDTO implements BaseDTO {

    @NotNull(message = "Goods ID cannot be null")
    @ApiModelProperty(value = "Goods ID", required = true, example = "1")
    private Integer goodsId;

    @ApiModelProperty(value = "SKU ID", example = "1")
    private Integer skuId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    @ApiModelProperty(value = "Quantity", required = true, example = "2")
    private Integer quantity;

    @ApiModelProperty(value = "Unit Price", example = "18.00")
    private BigDecimal unitPrice;
}
