package com.fuint.openapi.v1.member.group.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 会员分组分页查询请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "会员分组分页查询请求VO")
public class MtMemberGroupPageReqVO {

    @ApiModelProperty(value = "当前页码", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "分组名称（模糊搜索）", example = "VIP")
    private String name;

    @ApiModelProperty(value = "分组ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "状态：A-正常；N-禁用；D-删除", example = "A")
    private String status;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;
}
