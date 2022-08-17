create table rules
(
    guild_id  unsigned big int not null,
    key       text             not null,
    value     text             not null,
    primary key (guild_id, key)
);
