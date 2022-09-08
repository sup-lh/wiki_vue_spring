package com.suplin.demo.mapper;

import com.suplin.demo.resp.StatisticResp;

import java.util.List;

public interface EbookSnapshotMapperCust {
    void genSnapshot();

    List<StatisticResp> getStatistic();

    List<StatisticResp> get30Statistic();
}
