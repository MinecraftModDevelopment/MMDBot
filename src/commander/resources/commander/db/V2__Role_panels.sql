create table role_panels
(
    channel   unsigned big int not null,
    message   unsigned big int not null,
    emote     text             not null,
    role      unsigned big int not null,
    permanent boolean          not null,
    constraint pk_role_panels primary key (channel, message, role)
);
