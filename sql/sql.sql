create database ssm;
use ssm;

create table t_user(
id int(10) primary key auto_increment,
username varchar(20),
password varchar(20)
) comment '用户表';

insert into t_user  values(default,"admin","123");
insert into t_user  values(default,"zs","123");

select * from t_user;