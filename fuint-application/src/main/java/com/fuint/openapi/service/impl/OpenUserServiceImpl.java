package com.fuint.openapi.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.fuint.common.util.CommonUtil;
import com.fuint.common.util.PhoneFormatCheckUtils;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.util.SeqUtil;
import com.fuint.openapi.service.OpenUserService;
import com.fuint.repository.mapper.MtUserMapper;
import com.fuint.repository.model.MtUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Miao
 * @date 2026/1/23
 */
@Slf4j
@Service
public class OpenUserServiceImpl implements OpenUserService {

    @Resource
    private MtUserMapper mtUserMapper;

    @Override
    public List<MtUser> getUserLsByMobiles(List<String> mobiles) {
        if (CollUtil.isEmpty(mobiles)) {
            return Collections.emptyList();
        }
        return mtUserMapper.selectUserLsByMobiles(mobiles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMember(MtUser mtUser) {
        mtUserMapper.updateById(mtUser);
    }
}
