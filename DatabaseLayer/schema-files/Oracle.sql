-- unmodified output of [create-ddl]

    drop table Booking cascade constraints;

    drop table CreditCard cascade constraints;

    drop table Journey cascade constraints;

    drop table Location cascade constraints;

    drop table LoginHistory cascade constraints;

    drop table LoginRole cascade constraints;

    drop table LoginUser cascade constraints;

    drop table LoginUser_LoginRole cascade constraints;

    drop table Payment cascade constraints;

    drop table Schedule cascade constraints;

    drop table Tenant cascade constraints;

    drop sequence hibernate_sequence;

    create table Booking (
        id varchar2(255) not null,
        bookingDate date,
        journey_id number(10,0),
        user_name varchar2(255),
        primary key (id),
        unique (journey_id, user_name)
    );

    create table CreditCard (
        cardNumber varchar2(255) not null,
        username varchar2(255),
        validThrough date,
        primary key (cardNumber)
    );

    create table Journey (
        id number(10,0) not null,
        amount double precision not null,
        description varchar2(255),
        fromDate date,
        name varchar2(255),
        content blob,
        toDate date,
        destination_name varchar2(255),
        start_name varchar2(255),
        tenant_name varchar2(255),
        primary key (id),
        unique (name, start_name, destination_name, fromDate, toDate)
    );

    create table Location (
        name varchar2(255) not null,
        primary key (name)
    );

    create table LoginHistory (
        id number(10,0) not null,
        loginDate date,
        user_name varchar2(255),
        primary key (id),
        unique (user_name, loginDate)
    );

    create table LoginRole (
        name varchar2(255) not null,
        primary key (name)
    );

    create table LoginUser (
        name varchar2(255) not null,
        fullName varchar2(255),
        lastLogin date,
        password varchar2(255),
        primary key (name)
    );

    create table LoginUser_LoginRole (
        LoginUser_name varchar2(255) not null,
        roles_name varchar2(255) not null,
        primary key (LoginUser_name, roles_name)
    );

    create table Payment (
        bookingId varchar2(255) not null,
        amount double precision not null,
        paymentDate date,
        creditCard_cardNumber varchar2(255),
        primary key (bookingId)
    );

    create table Schedule (
        name varchar2(255) not null,
        lastExecution date,
        period number(19,0) not null,
        primary key (name)
    );

    create table Tenant (
        name varchar2(255) not null,
        description varchar2(255),
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

    create sequence hibernate_sequence start with 1 increment by 1;
