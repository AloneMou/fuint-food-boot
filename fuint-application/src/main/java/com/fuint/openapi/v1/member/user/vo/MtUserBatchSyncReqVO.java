package com.fuint.openapi.v1.member.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 员工批量数据同步请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "员工批量数据同步请求VO")
public class MtUserBatchSyncReqVO {

    @NotEmpty(message = "员工列表不能为空")
    @Size(min = 1, max = 100, message = "每次最多同步100条员工数据")
    @Valid
    @ApiModelProperty(value = "员工数据列表", required = true)
    private List<MtUserSyncReqVO> users;
}
