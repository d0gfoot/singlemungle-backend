insert into users (
	id,
	nickname,
	phone,
	created_at,
	updated_at,
	deleted_at
) values
	(1, '보호자1', '010-1111-1111', current_timestamp, current_timestamp, null),
	(2, '보호자2', '010-2222-2222', current_timestamp, current_timestamp, null);

insert into pets (
	id,
	user_id,
	name,
	breed,
	gender,
	birth_date,
	weight,
	profile_image_url,
	memo,
	is_representative,
	created_at,
	updated_at
) values
	(101, 1, '초코', '푸들', 'MALE', date '2020-01-01', 4.5, null, '활발함', true, current_timestamp, current_timestamp),
	(102, 2, '라떼', '말티즈', 'FEMALE', date '2021-02-02', 3.2, null, '낯가림', true, current_timestamp, current_timestamp);

insert into pet_health_records (
	id,
	pet_id,
	record_type,
	title,
	hospital_name,
	record_date,
	description,
	next_schedule_date,
	created_at,
	updated_at
) values
	(201, 101, 'VACCINE', '종합백신', '사랑동물병원', date '2026-06-01', '종합백신 접종', date '2026-12-01', current_timestamp, current_timestamp),
	(202, 102, 'HOSPITAL', '진료', '튼튼동물병원', date '2026-06-02', '소화불량 진료', null, current_timestamp, current_timestamp);

insert into schedules (
	id,
	user_id,
	pet_id,
	schedule_type,
	title,
	start_at,
	end_at,
	location,
	memo,
	is_alarm,
	created_at,
	updated_at
) values
	(301, 1, 101, 'HOSPITAL', '정기 검진', timestamp '2026-07-01 10:00:00', null, '사랑동물병원', '공복 필요', true, current_timestamp, current_timestamp),
	(302, 2, 102, 'BEAUTY', '미용 예약', timestamp '2026-07-02 11:00:00', null, '몽실몽실펫살롱', null, true, current_timestamp, current_timestamp);
