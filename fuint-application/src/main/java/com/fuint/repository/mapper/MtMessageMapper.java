package com.fuint.repository.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuint.repository.model.MtMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息 Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtMessageMapper extends BaseMapper<MtMessage> {

    List<MtMessage> findNewMessage(@Param("userId") Integer userId, @Param("type") String type);

    List<MtMessage> findNeedSendMessage(@Param("type") String type);

    default List<MtMessage> findNeedSendMessageList(List<String> types) {
        return selectList(new LambdaQueryWrapper<MtMessage>()
                .in(MtMessage::getType, types)
                .eq(MtMessage::getIsSend, "N")
                .orderByAsc(MtMessage::getSendTime)
        );
    }
}
