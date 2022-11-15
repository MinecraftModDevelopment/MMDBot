create table auto_icons (
     guild_id unsigned big int not null primary key,
     colours text not null,
     log_channel unsigned big int not null,
     ring boolean not null,
     enabled boolean not null
);

create table day_counter (
     guild_id unsigned big int not null primary key,
     current_day int not null,
     backwards boolean not null
);
