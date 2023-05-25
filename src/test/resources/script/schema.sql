drop table if exists `Data`;
drop table if exists `Data_Information`;
drop table if exists `Data_Detail`;

create table `Data_Information`
(
    `data_information_id` bigint auto_increment primary key,
    `created_at` DATETIME not null
);

create table `Data_Detail`
(
    `data_detail_id` bigint auto_increment primary key,
    `value` varchar(100) not null
);

create table `Data`
(
    `data_id`   bigint auto_increment primary key,
    `name` varchar(100) null,
    `data_information_id` bigint not null,
    `data_detail_id` bigint not null,
    foreign key (`data_information_id`) references `Data_Information`("data_information_id"),
    foreign key (`data_detail_id`) references `Data_Detail`("data_detail_id")
);

merge into `Data_Information` values (1, now());
merge into `Data_Information` values (2, now());

merge into `Data_Detail` values (1, 'String for AAA');
merge into `Data_Detail` values (2, 'String for BBB');

merge into `Data` values (1, 'AAA', 1, 1);
merge into `Data` values (2, 'BBB', 2, 2);

