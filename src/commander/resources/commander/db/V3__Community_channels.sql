create table community_channels
(
    id                    unsigned big int not null primary key,
    owner                 unsigned big int not null,
    ignore_archival_until unsigned big int,
    saved_from_archival   boolean
);
