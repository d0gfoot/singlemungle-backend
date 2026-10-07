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
	(102, 2, '라떼', 'Poodle', 'FEMALE', date '2021-02-02', 3.2, null, '낯가림', true, current_timestamp, current_timestamp);
