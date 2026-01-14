package com.fuint.openapi.v1.member.group.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 会员分组响应VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "会员分组响应VO")
public class MtMemberGroupRespVO {

    @ApiModelProperty(value = "分组ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "分组名称", example = "VIP会员")
    private String name;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "父级分组ID", example = "0")
    private Integer parentId;

    @ApiModelProperty(value = "子分组列表")
    private List<MtMemberGroupRespVO> children;

    @ApiModelProperty(value = "会员数量", example = "100")
    private Long memberNum;

    @ApiModelProperty(value = "分组描述", example = "高级会员分组")
    private String description;

    @ApiModelProperty(value = "状态：A-正常；N-禁用；D-删除", example = "A")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", example = "2024-01-01 12:00:00")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间", example = "2024-01-01 12:00:00")
    private Date updateTime;

    @ApiModelProperty(value = "操作人", example = "admin")
    private String operator;
}
