create table doc
(
    father_id  bigint auto_increment
        primary key comment '自增长id,用来与parent相互作用形成父子级',
    parent     bigint default 0 not null comment '显示该文档的父级文档',
    name       varchar(200)     not null,
    sort       int              null,
    view_count int    default 0 null,
    vote_count int    default 0 null,
    ebook_id   bigint default 0 null,
    id         bigint           null comment '以后用来校验'
)
    comment '文档表';

