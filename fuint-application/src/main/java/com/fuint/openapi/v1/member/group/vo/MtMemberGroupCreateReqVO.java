package com.fuint.openapi.v1.member.group.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 会员分组创建请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "会员分组创建请求VO")
public class MtMemberGroupCreateReqVO {

    @NotBlank(message = "分组名称不能为空")
    @ApiModelProperty(value = "分组名称", required = true, example = "VIP会员")
    private String name;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "父级分组ID", example = "0")
    private Integer parentId;

    @ApiModelProperty(value = "分组描述", example = "高级会员分组")
    private String description;
}
