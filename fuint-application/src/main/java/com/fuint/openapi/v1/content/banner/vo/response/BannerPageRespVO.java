package com.fuint.openapi.v1.content.banner.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel(value = "Banner分页响应VO")
public class BannerPageRespVO {
    @ApiModelProperty(value = "Banner列表")
    private List<BannerRespVO> list;

    @ApiModelProperty(value = "总记录数", example = "100")
    private Long total;

    @ApiModelProperty(value = "总页数", example = "10")
    private Integer totalPages;

    @ApiModelProperty(value = "当前页码", example = "1")
    private Integer currentPage;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize;
}
