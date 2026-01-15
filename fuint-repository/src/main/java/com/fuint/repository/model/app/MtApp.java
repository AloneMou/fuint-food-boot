package com.fuint.repository.model.app;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/14 22:46
 */

@Getter
@Setter
@TableName("mt_app")
@ApiModel(value = "MtApp对象", description = "应用表")
public class MtApp implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("应用ID")
    private String appId;

    @ApiModelProperty("应用密钥")
    private String appSecret;

    @ApiModelProperty("请求白名单/IP")
    private String whiteList;

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("应用回调地址")
    private String callbackUrl;

    @ApiModelProperty("应用状态")
    private String status;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;
}
