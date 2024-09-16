create table device_status_changes (
    changed_at datetime(6) not null,
    device_id bigint not null,
    id bigint not null auto_increment,
    scenario_id bigint, new_status varchar(255),
    old_status varchar(255),
    weather_condition varchar(255),
    primary key (id)
)
engine=InnoDB;

create table device_types (
    id bigint not null auto_increment,
    name varchar(255) not null,
    primary key (id)
)
engine=InnoDB;

create table devices (
    latitude float(53) not null,
    longitude float(53) not null,
    device_type_id bigint not null,
    id bigint not null auto_increment,
    last_updated datetime(6) not null,
    device_name varchar(255) not null,
    status varchar(255) not null,
    primary key (id)
)
engine=InnoDB;

alter table device_types add constraint UKjvdt9wkmcgqi4bcd2im1dm71w unique (name);
alter table device_status_changes add constraint FKdgbu1n1kxloddgp1i715i46rg foreign key (device_id) references devices (id);
alter table devices add constraint FKthsup9yv35eehh6hkt0jj3naw foreign key (device_type_id) references device_types (id);
