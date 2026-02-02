package com.fuint.common.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * Webhook回调日志数据传输对象
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class WebhookLogDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("事件ID")
    private String eventId;

    @ApiModelProperty("链路ID")
    private String traceId;

    @ApiModelProperty("事件类型")
    private String eventType;

    @ApiModelProperty("商户ID")
    private Integer merchantId;

    @ApiModelProperty("应用ID")
    private String appId;

    @ApiModelProperty("回调URL")
    private String callbackUrl;

    @ApiModelProperty("请求方式")
    private String requestPath;

    @ApiModelProperty("请求头")
    private String requestHeaders;

    @ApiModelProperty("请求体")
    private String requestBody;

    @ApiModelProperty("响应状态码")
    private Integer responseCode;

    @ApiModelProperty("响应内容")
    private String responseBody;

    @ApiModelProperty("发送状态：0-进行中，1-成功，2-失败")
    private Integer status;

    @ApiModelProperty("重试次数")
    private Integer retryCount;

    @ApiModelProperty("下次重试时间")
    private Date nextRetryTime;

    @ApiModelProperty("异常信息")
    private String errorMsg;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;
}