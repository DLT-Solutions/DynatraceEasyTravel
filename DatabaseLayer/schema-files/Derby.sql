-- unmodified output of [create-ddl]

    alter table Booking 
        drop constraint FK6713A03923B92753;

    alter table Booking 
        drop constraint FK6713A039D65D9B51;

    alter table Journey 
        drop constraint FKE9D47A09F8F6184;

    alter table Journey 
        drop constraint FKE9D47A0452CCD58;

    alter table Journey 
        drop constraint FKE9D47A03617A8F1;

    alter table LoginHistory 
        drop constraint FK2EBE690BD65D9B51;

    alter table LoginUser_LoginRole 
        drop constraint FK11EFE394AC93F208;

    alter table LoginUser_LoginRole 
        drop constraint FK11EFE394A043B35;

    alter table Payment 
        drop constraint FK3454C9E6F727DB53;

    drop table Booking;

    drop table CreditCard;

    drop table Journey;

    drop table Location;

    drop table LoginHistory;

    drop table LoginRole;

    drop table LoginUser;

    drop table LoginUser_LoginRole;

    drop table Payment;

    drop table Schedule;

    drop table Tenant;

    drop table hibernate_sequence;

    create table Booking (
        id varchar(255) not null,
        bookingDate timestamp,
        journey_id integer,
        user_name varchar(255),
        primary key (id)
    );

    create table CreditCard (
        cardNumber varchar(255) not null,
        username varchar(255),
        validThrough timestamp,
        primary key (cardNumber)
    );

    create table Journey (
        id integer not null,
        amount double not null,
        description varchar(255),
        fromDate timestamp,
        name varchar(255),
        content blob(2147483646),
        toDate timestamp,
        destination_name varchar(255),
        start_name varchar(255),
        tenant_name varchar(255),
        primary key (id)
    );

    create table Location (
        name varchar(255) not null,
        primary key (name)
    );

    create table LoginHistory (
        id integer not null,
        loginDate timestamp,
        user_name varchar(255),
        primary key (id)
    );

    create table LoginRole (
        name varchar(255) not null,
        primary key (name)
    );

    create table LoginUser (
        name varchar(255) not null,
        fullName varchar(255),
        lastLogin timestamp,
        password varchar(255),
        primary key (name)
    );

    create table LoginUser_LoginRole (
        LoginUser_name varchar(255) not null,
        roles_name varchar(255) not null,
        primary key (LoginUser_name, roles_name)
    );

    create table Payment (
        bookingId varchar(255) not null,
        amount double not null,
        paymentDate timestamp,
        creditCard_cardNumber varchar(255),
        primary key (bookingId)
    );

    create table Schedule (
        name varchar(255) not null,
        lastExecution timestamp,
        period bigint not null,
        primary key (name)
    );

    create table Tenant (
        name varchar(255) not null,
        description varchar(255),
        primary key (name)
    );

    alter table Booking 
        add constraint FK6713A03923B92753 
        foreign key (journey_id) 
        references Journey;

    alter table Booking 
        add constraint FK6713A039D65D9B51 
        foreign key (user_name) 
        references LoginUser;

    alter table Journey 
        add constraint FKE9D47A09F8F6184 
        foreign key (start_name) 
        references Location;

    alter table Journey 
        add constraint FKE9D47A0452CCD58 
        foreign key (destination_name) 
        references Location;

    alter table Journey 
        add constraint FKE9D47A03617A8F1 
        foreign key (tenant_name) 
        references Tenant;

    alter table LoginHistory 
        add constraint FK2EBE690BD65D9B51 
        foreign key (user_name) 
        references LoginUser;

    alter table LoginUser_LoginRole 
        add constraint FK11EFE394AC93F208 
        foreign key (LoginUser_name) 
        references LoginUser;

    alter table LoginUser_LoginRole 
        add constraint FK11EFE394A043B35 
        foreign key (roles_name) 
        references LoginRole;

    alter table Payment 
        add constraint FK3454C9E6F727DB53 
        foreign key (creditCard_cardNumber) 
        references CreditCard;

    create table hibernate_sequence (
         next_val bigint 
    );

    insert into hibernate_sequence values ( 1 );
