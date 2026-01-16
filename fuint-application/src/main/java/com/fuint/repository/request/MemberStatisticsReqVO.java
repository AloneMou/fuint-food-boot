package com.fuint.repository.request;

import cn.hutool.core.date.DatePattern;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author AgoniMou
 * @date 2024/12/4
 */
@Data
public class MemberStatisticsReqVO {

    @ApiModelProperty("商户ID")
    private Integer merchantId;

    @ApiModelProperty("店铺ID")
    private Integer storeId;

    @ApiModelProperty("开始时间")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date startTime;

    @ApiModelProperty("结束时间")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date endTime;

    @ApiModelProperty("会员名称")
    private String name;

    @ApiModelProperty("会员号")
    private String userNo;

    @ApiModelProperty("手机号")
    private String mobile;
}
