create table if not exists address
(
    id int auto_increment primary key,
    postal_code smallint not null,
    city varchar(255) null,
    street_address varchar(255) not null
);

create table if not exists location
(
    id int auto_increment primary key,
    address_id int not null,
    room_name varchar(255) not null,
    exposure enum ('SHADE', 'SEMI-SHADE', 'FULL_SUN') default 'SEMI-SHADE' not null,
    side enum ('NORTH', 'SOUTH', 'EST', 'WEST') default 'SOUTH' not null,
    description text null,
    constraint location_ibfk_1 foreign key (address_id) references address (id)
);

create index if not exists address_id on location (address_id);

create table if not exists plant
(
    id int auto_increment primary key,
    location_id int not null,
    common_name varchar(255) not null,
    constraint plant_ibfk_1 foreign key (location_id) references location (id)
);

create index if not exists location_id on plant (location_id);

