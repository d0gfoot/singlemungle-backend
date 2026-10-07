\set ON_ERROR_STOP on

begin;

delete from grooming_style_preferences
where user_id in (select id from users where oauth_provider_id like 'demo-seed-user-%')
	or source_simulation_id in (
		select id from beauty_style_simulations
		where pet_id in (select id from pets where memo like '[DEMO-SEED]%')
			or reference_review_image_id in (
				select id from review_images
				where review_id in (select id from reviews where content like '[DEMO-SEED:%')
			)
	);

delete from beauty_style_simulations
where user_id in (select id from users where oauth_provider_id like 'demo-seed-user-%')
	or pet_id in (select id from pets where memo like '[DEMO-SEED]%')
	or reference_review_image_id in (
		select id from review_images
		where review_id in (select id from reviews where content like '[DEMO-SEED:%')
	);

delete from review_images
where review_id in (select id from reviews where content like '[DEMO-SEED:%');

delete from reviews where content like '[DEMO-SEED:%';

delete from pet_health_records
where pet_id in (select id from pets where memo like '[DEMO-SEED]%');

delete from schedules where memo like '[DEMO-SEED]%';
delete from walk_records where memo like '[DEMO-SEED]%';
delete from favorite_places where memo like '[DEMO-SEED]%';
delete from pets where memo like '[DEMO-SEED]%';
delete from users where oauth_provider_id like 'demo-seed-user-%';

commit;

\echo 'Demo data cleanup completed.'
