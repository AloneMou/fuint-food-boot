package com.fuint.openapi.v1.member.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 员工分页响应VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "员工分页响应VO")
public class MtUserPageRespVO {

    @ApiModelProperty(value = "员工列表")
    private List<MtUserRespVO> list;

    @ApiModelProperty(value = "总记录数", example = "100")
    private Long total;

    @ApiModelProperty(value = "总页数", example = "10")
    private Integer totalPages;

    @ApiModelProperty(value = "当前页码", example = "1")
    private Integer currentPage;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize;
}
