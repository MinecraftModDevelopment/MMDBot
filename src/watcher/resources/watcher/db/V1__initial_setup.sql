create table role_persist
(
    user_id unsigned big int not null,
    role_id unsigned big int not null,
    primary key (user_id, role_id)
);
create table warnings
(
    user_id   unsigned big int not null,
    guild_id  unsigned big int not null,
    warn_id   string           not null,
    reason    string           not null,
    moderator unsigned big int not null,
    timestamp datetime         not null,
    primary key (user_id, guild_id, warn_id)
);
create table components
(
    feature   text      not null,
    id        text      not null,
    arguments text      not null,
    lifespan  text      not null,
    last_used timestamp not null,
    constraint pk_components primary key (feature, id)
);
