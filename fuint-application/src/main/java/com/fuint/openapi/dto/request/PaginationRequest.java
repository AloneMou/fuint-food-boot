package com.fuint.openapi.dto.request;

import com.fuint.openapi.dto.api.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Standard Pagination Request Component.
 * Intended to be used via composition, not inheritance.
 */
@Data
@ApiModel(description = "Pagination Request Component")
public class PaginationRequest implements BaseRequest {

    @ApiModelProperty(value = "Page Number", example = "1", required = true)
    @NotNull(message = "Page number is required")
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "Page Size", example = "10", required = true)
    @NotNull(message = "Page size is required")
    @Min(value = 1, message = "Page size must be at least 1")
    private Integer pageSize = 10;
}
