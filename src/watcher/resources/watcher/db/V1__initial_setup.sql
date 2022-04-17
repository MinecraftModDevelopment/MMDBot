create table first_join (
    user_id unsigned big int unique not null primary key,
    first_joined_at datetime not null
);

create table role_persist (
    user_id unsigned big int not null,
    role_id unsigned big int not null,
    primary key (user_id, role_id)
);
