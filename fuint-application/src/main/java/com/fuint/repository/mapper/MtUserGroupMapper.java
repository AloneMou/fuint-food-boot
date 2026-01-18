package com.fuint.repository.mapper;

import com.fuint.common.enums.StatusEnum;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.repository.model.MtUserGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 会员分组 Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtUserGroupMapper extends BaseMapper<MtUserGroup> {
    Long getMemberNum(@Param("groupIds") List<Integer> groupIds);

    default List<MtUserGroup> selectUserGroupListByIds(Collection<Integer> ids) {
        return selectList(new LambdaQueryWrapperX<MtUserGroup>()
                .eq(MtUserGroup::getStatus, StatusEnum.ENABLED.getKey())
                .inIfPresent(MtUserGroup::getId, ids)
        );
    }
}