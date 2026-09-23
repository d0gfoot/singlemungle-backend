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

insert into places (
	id,
	provider,
	external_place_id,
	place_type,
	name,
	address,
	road_address,
	latitude,
	longitude,
	phone,
	place_url,
	reservation_url,
	is_verified,
	created_at,
	updated_at
) values
	(
		301,
		'KAKAO',
		'beauty-shop-1',
		'BEAUTY',
		'몽실몽실펫살롱',
		'서울 강남구 역삼동 3',
		'서울 강남구 테헤란로 30',
		37.4990000,
		127.0300000,
		'02-5555-6666',
		'https://place.example.com/beauty-1',
		'https://reservation.example.com/beauty-1',
		true,
		current_timestamp,
		current_timestamp
	),
	(
		302,
		'KAKAO',
		'hospital-1',
		'HOSPITAL',
		'사랑동물병원',
		'서울 강남구 역삼동 1',
		'서울 강남구 테헤란로 1',
		37.4983000,
		127.0276000,
		'02-1111-2222',
		'https://place.example.com/hospital-1',
		'https://reservation.example.com/hospital-1',
		true,
		current_timestamp,
		current_timestamp
	);

insert into beauty_styles (
	id,
	place_id,
	name,
	description,
	image_url
) values
	(401, 301, '곰돌이컷', '얼굴을 둥글게 다듬는 스타일', 'https://image.example.com/style-bear.jpg'),
	(402, 301, '스포팅컷', '활동성을 살린 짧은 스타일', 'https://image.example.com/style-sporting.jpg');
