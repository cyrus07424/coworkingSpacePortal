# Update equipment table schema

# --- !Ups

alter table equipment alter column purchase_price decimal(10,2);
alter table equipment alter column description varchar(1000);
alter table equipment alter column category varchar(50);

alter table equipment_reservation alter column status varchar(20);

create index ix_equipment_reservation_date on equipment_reservation (reservation_date);
create index ix_equipment_reservation_status on equipment_reservation (status);

# --- !Downs

drop index if exists ix_equipment_reservation_status;
drop index if exists ix_equipment_reservation_date;