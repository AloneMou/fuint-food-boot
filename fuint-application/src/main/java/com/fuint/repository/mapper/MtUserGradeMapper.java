package com.fuint.repository.mapper;

import com.fuint.common.enums.StatusEnum;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.repository.model.MtUserGrade;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Collection;
import java.util.List;

/**
 * Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtUserGradeMapper extends BaseMapper<MtUserGrade> {
    // empty

    // 查询启用的会员等级
    default List<MtUserGrade> selectEnabledListByIds(Collection<Integer> ids) {
        return selectList(new LambdaQueryWrapperX<MtUserGrade>()
                .eq(MtUserGrade::getStatus, StatusEnum.ENABLED.getKey())
                .inIfPresent(MtUserGrade::getId, ids)
        );
    }
}
