create table spaces(
    space_id int primary key,
    name varchar(255) not null,
    owner varchar(30) not null
);

create table messages(
    space_id int not null references spaces(space_id),
    msg_id int primary key,
    author varchar(30) not null,
    msg_time timestamp not null default current_timestamp,
    msg_text varchar(1024) not null
);

create sequence space_id_seq;
create sequence msg_id_seq;
create index msg_timestamp_idx on messages(msg_time);
create unique index space_name_idx on spaces(name);

create user natter_api_user password 'password';
grant select, insert on spaces, messages to natter_api_user;