create table if not exists advancement
(
    id int auto_increment primary key,
    identifier varchar(512) not null,
    frame varchar(32) not null,
    title varchar(512) not null,
    description varchar(1024) not null,
    constraint advancement_identifier_uindex unique (identifier)
);

create table if not exists entity
(
    id int auto_increment primary key,
    translation_key varchar(512) not null,
    constraint entity_translation_key_uindex unique (translation_key)
);

create table if not exists item
(
    id int auto_increment primary key,
    translation_key varchar(512) not null,
    constraint item_translation_key_uindex unique (translation_key)
);

create table if not exists player
(
    id int auto_increment primary key,
    uuid  varchar(255) not null,
    name  varchar(255) not null,
    money float default 0 not null,
    constraint player_uuid_key_uindex unique (uuid)
);

create table if not exists mined_block_reward
(
    id int auto_increment primary key,
    item_id int not null,
    amount float null,
    constraint mined_block_reward_item__fk foreign key (item_id) references item (id),
    constraint mined_block_reward_item_id_uindex unique (item_id)
);

create table if not exists entity_killed_reward
(
    id int auto_increment primary key,
    entity_id int not null,
    amount float null,
    constraint entity_killed_reward_entity__fk foreign key (entity_id) references entity (id),
    constraint entity_killed_reward_entity_id_uindex unique (entity_id)
);

create table if not exists advancement_reward
(
    id int auto_increment primary key,
    advancement_id int not null,
    amount float null,
    constraint advancement_reward_advancement__fk foreign key (advancement_id) references advancement (id),
    constraint advancement_reward_advancement_id_uindex unique (advancement_id)
);
