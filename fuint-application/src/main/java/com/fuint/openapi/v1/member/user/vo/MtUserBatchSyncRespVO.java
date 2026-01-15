package com.fuint.openapi.v1.member.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 员工批量同步响应VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "员工批量同步响应VO")
public class MtUserBatchSyncRespVO {

    @ApiModelProperty(value = "同步结果列表")
    private List<MtUserSyncRespVO> results;

    @ApiModelProperty(value = "成功数量", example = "10")
    private Integer successCount;

    @ApiModelProperty(value = "失败数量", example = "2")
    private Integer failureCount;

    @ApiModelProperty(value = "总数量", example = "12")
    private Integer totalCount;
}
