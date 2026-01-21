package com.fuint.openapi.dto.response;

import com.fuint.common.dto.UserOrderDto;
import com.fuint.openapi.dto.api.BaseResponse;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order Detail Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "OrderDetailRespDTO", description = "Order details with queue information")
public class OrderDetailRespDTO implements BaseResponse {

    @ApiModelProperty(value = "Order Information")
    private UserOrderDto order;

    @ApiModelProperty(value = "Queue Count")
    private Integer queueCount;

    @ApiModelProperty(value = "Estimated Wait Time (minutes)")
    private Integer estimatedWaitTime;
}
