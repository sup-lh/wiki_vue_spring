package com.suplin.demo.mapper;

import org.apache.ibatis.annotations.Param;

public interface DocMapperCust {
    void increaseViewCount(@Param("id") Long id);

    void increaseVoteCount(@Param("id") Long id);

    void updateEbookInfo();

}

