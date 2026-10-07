\set ON_ERROR_STOP on

begin;

select exists (
	select 1 from users where id = :owner_user_id::bigint and deleted_at is null
) as demo_owner_exists \gset
\if :demo_owner_exists
\else
	\echo 'Active DEMO_OWNER_USER_ID does not exist.'
	\quit 3
\endif

create temp table demo_people (
	seq integer primary key,
	provider_id varchar(100) not null,
	nickname varchar(50) not null,
	pet_name varchar(50) not null,
	breed_code varchar(100) not null,
	breed_name varchar(100) not null,
	style_name varchar(100) not null
) on commit drop;

insert into demo_people values
	(1,  'demo-seed-user-01', '김민지', '코코', 'POMERANIAN', '포메라니안', '곰돌이컷'),
	(2,  'demo-seed-user-02', '이서준', '베리', 'POMERANIAN', '포메라니안', '곰돌이컷'),
	(3,  'demo-seed-user-03', '박지우', '후추', 'POMERANIAN', '포메라니안', '곰돌이컷'),
	(4,  'demo-seed-user-04', '최수아', '토리', 'POMERANIAN', '포메라니안', '곰돌이컷'),
	(5,  'demo-seed-user-05', '정하준', '모카', 'POMERANIAN', '포메라니안', '물개컷'),
	(6,  'demo-seed-user-06', '한예린', '보리', 'POMERANIAN', '포메라니안', '물개컷'),
	(7,  'demo-seed-user-07', '윤도윤', '나비', 'POMERANIAN', '포메라니안', '여우컷'),
	(8,  'demo-seed-user-08', '송지안', '꼬미', 'POMERANIAN', '포메라니안', '전체 다듬기'),
	(9,  'demo-seed-user-09', '김유진', '두부', 'MALTESE', '말티즈', '곰돌이컷'),
	(10, 'demo-seed-user-10', '이민서', '설기', 'MALTESE', '말티즈', '곰돌이컷'),
	(11, 'demo-seed-user-11', '박하윤', '우유', 'MALTESE', '말티즈', '곰돌이컷'),
	(12, 'demo-seed-user-12', '오지훈', '소금', 'MALTESE', '말티즈', '물개컷'),
	(13, 'demo-seed-user-13', '유서연', '마루', 'MALTESE', '말티즈', '물개컷'),
	(14, 'demo-seed-user-14', '임재민', '구름', 'MALTESE', '말티즈', '전체 다듬기'),
	(15, 'demo-seed-user-15', '강수빈', '롯데', 'POODLE', '푸들', '곰돌이컷'),
	(16, 'demo-seed-user-16', '남현우', '초코', 'POODLE', '푸들', '곰돌이컷'),
	(17, 'demo-seed-user-17', '신가은', '라떼', 'POODLE', '푸들', '물개컷'),
	(18, 'demo-seed-user-18', '문준호', '호두', 'POODLE', '푸들', '라이언컷'),
	(19, 'demo-seed-user-19', '백예지', '아몬드', 'POODLE', '푸들', '라이언컷'),
	(20, 'demo-seed-user-20', '조성민', '탄이', 'POODLE', '푸들', '전체 다듬기'),
	(21, 'demo-seed-user-21', '김다은', '송이', 'BICHON_FRISE', '비쇑 프리제', '곰돌이컷'),
	(22, 'demo-seed-user-22', '이준영', '포도', 'BICHON_FRISE', '비쇑 프리제', '곰돌이컷'),
	(23, 'demo-seed-user-23', '박은채', '사탕', 'BICHON_FRISE', '비쇑 프리제', '곰돌이컷'),
	(24, 'demo-seed-user-24', '최도현', '찌뽕', 'BICHON_FRISE', '비쇑 프리제', '물개컷'),
	(25, 'demo-seed-user-25', '정소율', '밤비', 'BICHON_FRISE', '비쇑 프리제', '전체 다듬기');

insert into users (oauth_provider, oauth_provider_id, email, nickname, role, created_at, updated_at)
select 'GOOGLE', provider_id, provider_id || '@demo.singlemungle.local', nickname, 'USER', current_timestamp, current_timestamp
from demo_people
on conflict (oauth_provider, oauth_provider_id) do update
set nickname = excluded.nickname, updated_at = current_timestamp, deleted_at = null;

insert into pets (user_id, name, breed, gender, birth_date, weight, memo, is_representative, created_at, updated_at)
select u.id, d.pet_name, d.breed_name, case when d.seq % 2 = 0 then 'FEMALE' else 'MALE' end,
	current_date - (interval '1 year' * (2 + d.seq % 5)), 2.5 + (d.seq % 7) * 0.6,
	'[DEMO-SEED] 견종별 스타일 통계용 반려견', true, current_timestamp, current_timestamp
from demo_people d
join users u on u.oauth_provider = 'GOOGLE' and u.oauth_provider_id = d.provider_id
where not exists (select 1 from pets p where p.user_id = u.id and p.memo like '[DEMO-SEED]%');

insert into beauty_style_simulations (
	user_id, pet_id, style_keyword, style_preset_id, source_type, source_image_url,
	generated_image_url, storage_provider, status, expires_at, created_at, google_place_id
)
select u.id, p.id, d.style_name, preset.id, 'PRESET', '/demo/stat-source-' || d.seq || '.jpg',
	'/demo/stat-result-' || d.seq || '.jpg', 'LOCAL', 'COMPLETED', current_timestamp + interval '365 days',
	current_timestamp - (interval '1 day' * (d.seq % 20)), :'place_id_1'
from demo_people d
join users u on u.oauth_provider = 'GOOGLE' and u.oauth_provider_id = d.provider_id
join pets p on p.user_id = u.id and p.memo like '[DEMO-SEED]%'
join grooming_style_presets preset on preset.name = d.style_name
where not exists (
	select 1 from beauty_style_simulations s
	where s.user_id = u.id and s.source_image_url = '/demo/stat-source-' || d.seq || '.jpg'
);

insert into grooming_style_preferences (
	user_id, breed_code, breed_name, style_preset_id, source_simulation_id, created_at, updated_at
)
select u.id, d.breed_code, d.breed_name, preset.id, simulation.id, current_timestamp, current_timestamp
from demo_people d
join users u on u.oauth_provider = 'GOOGLE' and u.oauth_provider_id = d.provider_id
join grooming_style_presets preset on preset.name = d.style_name
join beauty_style_simulations simulation
	on simulation.user_id = u.id and simulation.source_image_url = '/demo/stat-source-' || d.seq || '.jpg'
on conflict (user_id, breed_code) do update set
	breed_name = excluded.breed_name,
	style_preset_id = excluded.style_preset_id,
	source_simulation_id = excluded.source_simulation_id,
	updated_at = current_timestamp;

insert into reviews (user_id, google_place_id, rating, content, grooming_style_name, created_at, updated_at)
select u.id, review_data.google_place_id, review_data.rating, review_data.content,
	review_data.style_name, current_timestamp - review_data.age, current_timestamp - review_data.age
from (values
	('demo-seed-user-01', :'place_id_1', 5, '[DEMO-SEED:R01] 원하던 곰돌이컷 모양으로 둥글게 잘 나왔어요.', '곰돌이컷', interval '2 days'),
	('demo-seed-user-05', :'place_id_1', 5, '[DEMO-SEED:R02] 상담도 친절하고 얼굴과 가슴 라인이 자연스러워요.', '물개컷', interval '6 days'),
	('demo-seed-user-09', :'place_id_1', 4, '[DEMO-SEED:R03] 말티즈 눈가를 깔끔하게 정리해 주셨습니다.', '곰돌이컷', interval '11 days'),
	('demo-seed-user-15', :'place_id_1', 5, '[DEMO-SEED:R04] 미용 시간도 예정대로였고 향도 너무 강하지 않았어요.', '전체 다듬기', interval '17 days'),
	('demo-seed-user-18', :'place_id_2', 5, '[DEMO-SEED:R05] 풍성한 목털은 살리고 몸통은 시원하게 정리됐어요.', '라이언컷', interval '3 days'),
	('demo-seed-user-21', :'place_id_2', 4, '[DEMO-SEED:R06] 비쇑 털이 엉키지 않게 마무리해 주셨어요.', '곰돌이컷', interval '8 days'),
	('demo-seed-user-24', :'place_id_2', 5, '[DEMO-SEED:R07] 반려견이 낯을 가리는데 편안하게 진행해 주셨습니다.', '물개컷', interval '14 days'),
	('demo-seed-user-12', :'place_id_2', 4, '[DEMO-SEED:R08] 전체적으로 깔끔하고 발바닥 정리도 만족스러워요.', '전체 다듬기', interval '23 days')
) as review_data(provider_id, google_place_id, rating, content, style_name, age)
join users u on u.oauth_provider = 'GOOGLE' and u.oauth_provider_id = review_data.provider_id
where not exists (select 1 from reviews r where r.content = review_data.content);

\if :seed_review_images
insert into review_images (review_id, image_url, ai_reference_allowed, created_at)
select r.id, :'image_base_url' || '/' || image_data.file_name, true, r.created_at
from (values
	('[DEMO-SEED:R01]%', 'demo-review-01.jpg'),
	('[DEMO-SEED:R02]%', 'demo-review-02.jpg'),
	('[DEMO-SEED:R05]%', 'demo-review-03.jpg'),
	('[DEMO-SEED:R07]%', 'demo-review-04.jpg')
) as image_data(content_pattern, file_name)
join reviews r on r.content like image_data.content_pattern
where not exists (
	select 1 from review_images i where i.review_id = r.id and i.image_url = :'image_base_url' || '/' || image_data.file_name
);
\endif

insert into pets (
	user_id, name, breed, gender, birth_date, weight, memo, is_representative, created_at, updated_at, neutered
)
select :owner_user_id::bigint, '몽글이', '포메라니안', 'MALE', current_date - interval '3 years 4 months',
	4.2, '[DEMO-SEED] 시연용 반려견 - 밝은 크림색 털, 발목 주변은 짧게 정리 필요', true,
	current_timestamp, current_timestamp, true
where not exists (
	select 1 from pets where user_id = :owner_user_id::bigint and memo like '[DEMO-SEED] 시연용%'
);

insert into pet_health_records (
	pet_id, record_type, title, hospital_name, record_date, description, next_schedule_date, created_at, updated_at
)
select pet.id, data.record_type, data.title, data.hospital, current_date - data.days_ago,
	data.description, current_date + data.next_days, current_timestamp, current_timestamp
from pets pet
cross join (values
	('VACCINE', '종합백신 접종', '싱글멍글 동물병원', 120, '접종 후 특이 반응 없음', 245),
	('CHECKUP', '정기 건강검진', '싱글멍글 동물병원', 45, '체중과 치아 상태 양호', 320),
	('MEDICATION', '심장사충 예방약', '한강 동물의료센터', 12, '월 1회 예방약 복용', 18)
) as data(record_type, title, hospital, days_ago, description, next_days)
where pet.user_id = :owner_user_id::bigint and pet.memo like '[DEMO-SEED] 시연용%'
and not exists (select 1 from pet_health_records h where h.pet_id = pet.id and h.title = data.title);

insert into schedules (
	user_id, pet_id, schedule_type, title, start_at, end_at, location, memo, is_alarm, created_at, updated_at
)
select :owner_user_id::bigint, pet.id, data.schedule_type, data.title,
	current_date + data.starts_in, current_date + data.ends_in, data.location, data.memo, true,
	current_timestamp, current_timestamp
from pets pet
cross join (values
	('BEAUTY', '곰돌이컷 미용 예약', interval '3 days 14 hours', interval '3 days 16 hours', '멍살롱', '[DEMO-SEED] 예약 10분 전 도착'),
	('HOSPITAL', '심장사충 예방약', interval '18 days 10 hours', interval '18 days 10 hours 30 minutes', '한강 동물의료센터', '[DEMO-SEED] 아침 식사 후 복용'),
	('WALK', '주말 한강 산책', interval '5 days 18 hours', interval '5 days 19 hours', '안양천 산책로', '[DEMO-SEED] 물과 배변봉투 챙기기')
) as data(schedule_type, title, starts_in, ends_in, location, memo)
where pet.user_id = :owner_user_id::bigint and pet.memo like '[DEMO-SEED] 시연용%'
and not exists (select 1 from schedules s where s.user_id = :owner_user_id::bigint and s.memo = data.memo);

insert into walk_records (
	user_id, pet_id, started_at, ended_at, distance_km, duration_minutes, calories, route_data, memo, created_at
)
select :owner_user_id::bigint, pet.id,
	current_timestamp - data.age - (interval '1 minute' * data.minutes), current_timestamp - data.age,
	data.distance, data.minutes, data.calories,
	jsonb_build_array(
		jsonb_build_object('latitude', 37.500053, 'longitude', 126.867993),
		jsonb_build_object('latitude', 37.502100 + data.seq * 0.0001, 'longitude', 126.870100 + data.seq * 0.0001)
	), '[DEMO-SEED] ' || data.memo, current_timestamp - data.age
from pets pet
cross join (values
	(1, interval '2 days', 2.4, 38, 118, '저녁 산책'),
	(2, interval '5 days', 3.1, 47, 145, '안양천 코스'),
	(3, interval '9 days', 1.8, 29, 92, '아침 산책'),
	(4, interval '14 days', 4.2, 61, 188, '주말 긴 산책'),
	(5, interval '20 days', 2.7, 42, 131, '공원 산책'),
	(6, interval '27 days', 3.5, 53, 162, '저녁 운동'),
	(7, interval '35 days', 2.1, 34, 104, '동네 산책')
) as data(seq, age, distance, minutes, calories, memo)
where pet.user_id = :owner_user_id::bigint and pet.memo like '[DEMO-SEED] 시연용%'
and not exists (
	select 1 from walk_records w where w.user_id = :owner_user_id::bigint and w.memo = '[DEMO-SEED] ' || data.memo
);

insert into favorite_places (user_id, google_place_id, memo, created_at)
values
	(:owner_user_id::bigint, :'place_id_1', '[DEMO-SEED] 가장 자주 가는 미용샵', current_timestamp),
	(:owner_user_id::bigint, :'place_id_2', '[DEMO-SEED] 주말 예약 후보', current_timestamp)
on conflict (user_id, google_place_id) do nothing;

commit;

\echo 'Demo seed completed.'
