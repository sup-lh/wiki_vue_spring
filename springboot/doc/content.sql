create table content
(
    id      bigint     not null comment '文档id',
    content mediumtext not null,
    constraint content_pk
        primary key (id)
);

