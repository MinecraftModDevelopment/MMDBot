alter table role_persist add column guild_id unsigned big int not null default 0;

drop table if exists first_join;
