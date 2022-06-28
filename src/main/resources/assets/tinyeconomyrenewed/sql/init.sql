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
    name  varchar(255) not null,
    money float default 0 not null,
    constraint player_name_uindex unique (name)
);

create table if not exists mined_block_reward
(
    id int auto_increment primary key,
    item_id int not null,
    amount float null,
    constraint mined_block_reward_item__fk foreign key (item_id) references item (id),
    constraint mined_block_reward_item_id_uindex unique (item_id)
);
