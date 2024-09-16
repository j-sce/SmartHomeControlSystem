CREATE DATABASE IF NOT EXISTS smart_home_db;
USE smart_home_db;

create table roles (
    id bigint not null auto_increment,
    description varchar(255),
    role_name enum ('ADMIN','USER') unique,
    primary key (id)
);

create table users (
    created_at datetime(6),
    id bigint not null auto_increment,
    updated_at datetime(6),
    username varchar(50) not null unique,
    password varchar(100) not null,
    email varchar(255) not null unique,
    primary key (id)
);

create table user_role (
    role_id bigint not null,
    user_id bigint not null,
    primary key (role_id, user_id),
    foreign key (role_id) references roles (id),
    foreign key (user_id) references users (id)
);

create table device_types (
    id bigint not null auto_increment,
    name varchar(255) not null unique,
    primary key (id)
);

create table devices (
    latitude float(53) not null,
    longitude float(53) not null,
    device_type_id bigint not null,
    id bigint not null auto_increment,
    last_updated datetime(6) not null,
    device_name varchar(255) not null,
    status varchar(255) not null,
    primary key (id),
    foreign key (device_type_id) references device_types (id)
);

create table scenarios (
    device_type_id bigint not null,
    id bigint not null auto_increment,
    condition_value varchar(255),
    new_status varchar(255) not null,
    operator varchar(255) not null,
    weather_condition varchar(255) not null,
    primary key (id),
    foreign key (device_type_id) references device_types (id)
);

create table device_status_changes (
    changed_at datetime(6) not null,
    device_id bigint not null,
    id bigint not null auto_increment,
    scenario_id bigint,
    new_status varchar(255),
    old_status varchar(255),
    weather_condition varchar(255),
    primary key (id),
    foreign key (device_id) references devices (id),
    foreign key (scenario_id) references scenarios (id)
);