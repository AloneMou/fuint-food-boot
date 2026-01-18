package com.fuint.openapi.v1.member.user.vo;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 员工响应VO
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "员工响应VO")
public class MtUserRespVO {

    @ApiModelProperty(value = "会员ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "会员号", example = "U20240101001")
    private String userNo;

    @ApiModelProperty(value = "会员姓名", example = "张三")
    private String name;

    @ApiModelProperty(value = "手机号码", example = "138****8000")
    private String mobile;

    @ApiModelProperty(value = "头像", example = "/uploads/avatar.jpg")
    private String avatar;

    @ApiModelProperty(value = "分组ID", example = "1")
    private Integer groupId;

    @ApiModelProperty(value = "分组名称", example = "VIP会员")
    private String groupName;

    @ApiModelProperty(value = "等级ID", example = "1")
    private String gradeId;

    @ApiModelProperty(value = "等级名称", example = "黄金会员")
    private String gradeName;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "店铺名称", example = "总店")
    private String storeName;

    @ApiModelProperty(value = "性别：0-女；1-男", example = "1")
    private Integer sex;

    @ApiModelProperty(value = "出生日期", example = "1990-01-01")
    private String birthday;

    @ApiModelProperty(value = "身份证号", example = "110101199001011234")
    private String idcard;

    @ApiModelProperty(value = "地址", example = "北京市朝阳区")
    private String address;

    @ApiModelProperty(value = "余额", example = "100.00")
    private BigDecimal balance;

    @ApiModelProperty(value = "积分", example = "100")
    private Integer point;

    @ApiModelProperty(value = "来源渠道", example = "backend_add")
    private String source;

    @ApiModelProperty(value = "状态：A-激活；N-禁用；D-删除", example = "A")
    private String status;

    @ApiModelProperty(value = "备注信息", example = "员工备注")
    private String description;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", example = "2024-01-01 12:00:00")
    private Date createTime;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间", example = "2024-01-01 12:00:00")
    private Date updateTime;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty(value = "会员开始时间", example = "2024-01-01 00:00:00")
    private Date startTime;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty(value = "会员结束时间", example = "2025-01-01 00:00:00")
    private Date endTime;

    @ApiModelProperty(value = "是否员工", example = "Y")
    private String isStaff;

    @ApiModelProperty(value = "员工ID", example = "1")
    private Integer staffId;

    @ApiModelProperty(value = "员工等级", example = "1")
    private Integer staffLevel;

//    @ApiModelProperty(value = "最后操作人", example = "openapi")
//    private String operator;

//    @ApiModelProperty(value = "最后登录时间描述", example = "2小时前")
//    private String lastLoginTime;
}
