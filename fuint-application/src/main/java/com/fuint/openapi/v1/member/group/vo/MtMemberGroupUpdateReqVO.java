package com.fuint.openapi.v1.member.group.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 会员分组更新请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "会员分组更新请求VO")
public class MtMemberGroupUpdateReqVO {

    @NotNull(message = "分组ID不能为空")
    @ApiModelProperty(value = "分组ID", required = true, example = "1")
    private Integer id;

    @ApiModelProperty(value = "分组名称", example = "VIP会员")
    private String name;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "父级分组ID", example = "0")
    private Integer parentId;

    @ApiModelProperty(value = "分组描述", example = "高级会员分组")
    private String description;

    @ApiModelProperty(value = "状态：A-正常；N-禁用；D-删除", example = "A")
    private String status;
}
