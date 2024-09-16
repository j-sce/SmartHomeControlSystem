create table roles (
    id bigint not null auto_increment,
    description varchar(255),
    role_name enum ('ADMIN','USER'),
    primary key (id)
)
engine=InnoDB;

create table user_role (
    role_id bigint not null,
    user_id bigint not null,
    primary key (role_id, user_id)
)
engine=InnoDB;

create table users (
    created_at datetime(6),
    id bigint not null auto_increment check ((id>=1) and (id<=9223372036854775807)),
    updated_at datetime(6),
    username varchar(50) not null,
    password varchar(100) not null,
    email varchar(255) not null,
    primary key (id)
)
engine=InnoDB;

alter table roles add constraint UK716hgxp60ym1lifrdgp67xt5k unique (role_name);
alter table users add constraint UKr43af9ap4edm43mmtq01oddj6 unique (username);
alter table user_role add constraint FKt7e7djp752sqn6w22i6ocqy6q foreign key (role_id) references roles (id);
alter table user_role add constraint FKj345gk1bovqvfame88rcx7yyx foreign key (user_id) references users (id);
