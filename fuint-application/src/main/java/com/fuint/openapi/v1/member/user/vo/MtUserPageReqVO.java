package com.fuint.openapi.v1.member.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 员工分页查询请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "员工分页查询请求VO")
public class MtUserPageReqVO {

    @ApiModelProperty(value = "当前页码", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "会员ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "会员姓名（模糊搜索）", example = "张三")
    private String name;

    @ApiModelProperty(value = "手机号码（模糊搜索）", example = "138")
    private String mobile;

    @ApiModelProperty(value = "会员号（精确匹配）", example = "U20240101001")
    private String userNo;

    @ApiModelProperty(value = "分组ID", example = "1")
    private String groupIds;

    @ApiModelProperty(value = "等级ID", example = "1")
    private String gradeId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID列表（逗号分隔）", example = "1,2,3")
    private String storeIds;

    @ApiModelProperty(value = "状态：A-激活；N-禁用；D-删除", example = "A")
    private String status;

    @ApiModelProperty(value = "优惠券状态（精确匹配）：A-未使用；B-已使用；C-已过期；D-已删除；E-未领取", example = "A")
    private String couponStatus;

    @ApiModelProperty(value = "注册开始时间", example = "2024-01-01 00:00:00")
    private String startTime;

    @ApiModelProperty(value = "注册结束时间", example = "2024-12-31 23:59:59")
    private String endTime;
}
