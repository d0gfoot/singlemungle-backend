insert into places (
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
		'KAKAO',
		'12345',
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
	),
	(
		'KAKAO',
		'67890',
		'HOSPITAL',
		'튼튼동물병원',
		'서울 강남구 역삼동 2',
		'서울 강남구 테헤란로 20',
		37.5017000,
		127.0396000,
		'02-3333-4444',
		'https://place.example.com/hospital-2',
		'https://reservation.example.com/hospital-2',
		true,
		current_timestamp,
		current_timestamp
	),
	(
		'KAKAO',
		'mock-beauty-1',
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
	);

insert into place_business_hours (
	place_id,
	day_of_week,
	open_time,
	close_time,
	break_start_time,
	break_end_time,
	is_closed
)
select id, day_of_week, open_time, close_time, break_start_time, break_end_time, is_closed
from places
cross join (
	values
		('MONDAY', time '09:00:00', time '18:00:00', time '12:00:00', time '13:00:00', false),
		('TUESDAY', time '09:00:00', time '18:00:00', time '12:00:00', time '13:00:00', false),
		('WEDNESDAY', time '09:00:00', time '18:00:00', time '12:00:00', time '13:00:00', false),
		('THURSDAY', time '09:00:00', time '18:00:00', time '12:00:00', time '13:00:00', false),
		('FRIDAY', time '09:00:00', time '18:00:00', time '12:00:00', time '13:00:00', false),
		('SATURDAY', time '10:00:00', time '16:00:00', null, null, false),
		('SUNDAY', null, null, null, null, true)
) as business_hours(day_of_week, open_time, close_time, break_start_time, break_end_time, is_closed)
where external_place_id in ('12345', '67890');

insert into place_services (
	place_id,
	service_name,
	description,
	created_at,
	updated_at
)
select id, '일반 진료', '반려동물 일반 진료', current_timestamp, current_timestamp
from places
where external_place_id = '12345';

insert into place_services (
	place_id,
	service_name,
	description,
	created_at,
	updated_at
)
select id, '예방접종', '반려동물 예방접종', current_timestamp, current_timestamp
from places
where external_place_id = '12345';

insert into place_services (
	place_id,
	service_name,
	description,
	created_at,
	updated_at
)
select id, '건강검진', '반려동물 건강검진', current_timestamp, current_timestamp
from places
where external_place_id = '67890';

insert into place_images (
	place_id,
	image_url,
	sort_order,
	created_at
)
select id, 'https://image.example.com/hospital-1-main.jpg', 0, current_timestamp
from places
where external_place_id = '12345';

insert into place_images (
	place_id,
	image_url,
	sort_order,
	created_at
)
select id, 'https://image.example.com/hospital-1-sub.jpg', 1, current_timestamp
from places
where external_place_id = '12345';
