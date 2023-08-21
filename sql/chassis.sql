select * from simple_entity ;

select * from simple_some_other ;

select * from simple_some_model ;

select * from simple_subentity ;


select * from simple_entity ; select * from simple_some_model ; SELECT * FROM simple_some_other ; select * from simple_subentity ;


SELECT "simple_entity", uuid, "name", value, prio, a_instant, a_local_date_time, some_model_object_uuid, dto_specific_prop, optimistic_lock_id, created_at, updated_at, create_user, update_user
FROM public.simple_entity;
SELECT "simple_some_model", uuid, some_name, some_value, optimistic_lock_id, created_at, updated_at, create_user, update_user
FROM public.simple_some_model;
SELECT "simple_some_other", uuid, some_name, some_value, optimistic_lock_id, created_at, updated_at, create_user, update_user
FROM public.simple_some_other;
SELECT "simple_subentity", uuid, "name", value, prio, a_instant, a_local_date_time, sub_entity_dto_specific_prop, created_at, updated_at, create_user, update_user, simple_entity_subentitys_uuid
FROM public.simple_subentity;


DELETE FROM simple_subentity ; DELETE FROM simple_entity ; DELETE FROM simple_some_model ; DELETE FROM simple_some_other ;  


drop table simple_subentity ;
drop table simple_entity ;
drop table simple_some_model ;
drop table simple_some_other ;
