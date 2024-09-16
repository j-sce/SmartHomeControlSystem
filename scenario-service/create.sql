create table scenarios (
    device_type_id bigint not null,
    id bigint not null auto_increment,
    condition_value varchar(255),
    new_status varchar(255) not null,
    operator varchar(255) not null,
    weather_condition varchar(255) not null,
    primary key (id)
)
engine=InnoDB;
