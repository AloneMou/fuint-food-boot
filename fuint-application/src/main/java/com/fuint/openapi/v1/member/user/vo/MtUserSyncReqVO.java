package com.fuint.openapi.v1.member.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 员工数据同步请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "员工数据同步请求VO")
public class MtUserSyncReqVO {

    @NotBlank(message = "手机号码不能为空")
    @ApiModelProperty(value = "手机号码（作为唯一标识）", required = true, example = "13800138000")
    private String mobile;

    @ApiModelProperty(value = "会员姓名", example = "张三")
    private String name;

    @ApiModelProperty(value = "会员号", example = "U20240101001")
    private String userNo;

    @ApiModelProperty(value = "分组ID", example = "1")
    private Integer groupId;

    @ApiModelProperty(value = "等级ID", example = "1")
    private String gradeId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "性别：0-女；1-男", example = "1")
    private Integer sex;

    @ApiModelProperty(value = "出生日期", example = "1990-01-01")
    private String birthday;

    @ApiModelProperty(value = "身份证号", example = "110101199001011234")
    private String idcard;

    @ApiModelProperty(value = "地址", example = "北京市朝阳区")
    private String address;

    @ApiModelProperty(value = "头像", example = "/uploads/avatar.jpg")
    private String avatar;

    @ApiModelProperty(value = "余额", example = "100.00")
    private BigDecimal balance;

    @ApiModelProperty(value = "积分", example = "100")
    private Integer point;

    @ApiModelProperty(value = "状态：A-激活；N-禁用；D-删除", example = "A")
    private String status;

    @ApiModelProperty(value = "备注信息", example = "员工备注")
    private String description;
}
