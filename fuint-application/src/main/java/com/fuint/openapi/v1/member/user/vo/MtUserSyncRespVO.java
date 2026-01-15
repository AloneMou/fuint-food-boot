package com.fuint.openapi.v1.member.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 员工同步响应VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "员工同步响应VO")
public class MtUserSyncRespVO {

    @ApiModelProperty(value = "会员ID", example = "1")
    private Integer userId;

    @ApiModelProperty(value = "手机号码", example = "13800138000")
    private String mobile;

    @ApiModelProperty(value = "操作类型：create-创建；update-更新", example = "create")
    private String operationType;

    @ApiModelProperty(value = "是否成功", example = "true")
    private Boolean success;

    @ApiModelProperty(value = "失败原因", example = "手机号格式不正确")
    private String message;
}
