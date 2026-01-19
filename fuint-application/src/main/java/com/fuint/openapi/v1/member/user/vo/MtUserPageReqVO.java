package com.fuint.openapi.v1.member.user.vo;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fuint.framework.pojo.PageParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 员工分页查询请求VO
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "员工分页查询请求VO")
public class MtUserPageReqVO extends PageParams {

    @ApiModelProperty(value = "会员ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "会员姓名（模糊搜索）", example = "张三")
    private String name;

    @ApiModelProperty(value = "手机号码（模糊搜索）", example = "138")
    private String mobile;

    @ApiModelProperty(value = "会员号（精确匹配）", example = "U20240101001")
    private String userNo;

    @ApiModelProperty(value = "分组ID", example = "1")
    private Integer groupId;

    @ApiModelProperty(value = "等级ID", example = "1")
    private String gradeId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1,2,3")
    private Integer storeId;

    @ApiModelProperty(value = "状态：A-激活；N-禁用", example = "A")
    private String status;

    @ApiModelProperty(value = "是否员工", example = "Y")
    private String isStaff;

    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty(value = "创建开始时间", example = "2024-01-01 00:00:00")
    private Date startTime;

    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty(value = "创建结束时间", example = "2024-12-31 23:59:59")
    private Date endTime;
}
