create table ebook_snapshot
(
    id            bigint auto_increment comment 'id',
    ebook_id      bigint default 0 not null comment '电子书id',
    date          date             not null comment '快照日期',
    view_count    int    default 0 not null comment '阅读数',
    vote_count    int    default 0 not null comment '点赞数',
    view_increase int    default 0 not null comment '阅读增长',
    vote_increase int    default 0 not null comment '点赞增长',
    constraint ebook_snapshot_pk
        primary key (id)
)
    comment '电子书快照表';
