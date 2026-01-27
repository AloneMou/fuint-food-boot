package com.fuint.openapi.v1.member.grade;

import com.fuint.common.service.UserGradeService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Miao
 * @date 2026/1/27
 */
@Slf4j
@Validated
@Api(tags = "OpenApi-员工管理相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/v1/member/grade")
public class OpenUserGradeController {

    @Resource
    private UserGradeService userGradeService;
}
