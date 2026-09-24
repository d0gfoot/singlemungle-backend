delete from beauty_style_simulations;
delete from review_images;
delete from reviews;
delete from beauty_styles;
delete from favorite_places;
delete from place_images;
delete from place_services;
delete from place_business_hours;
delete from places;

alter table reviews alter column place_id drop not null;
alter table reviews add column google_place_id varchar(255) not null;
create index idx_reviews_google_place_id_created_at
	on reviews (google_place_id, created_at desc);

alter table favorite_places add column google_place_id varchar(255);
alter table favorite_places alter column google_place_id set not null;
create index idx_favorite_places_google_place_id
	on favorite_places (google_place_id);

alter table beauty_style_simulations add column google_place_id varchar(255);
create index idx_style_simulations_google_place_id
	on beauty_style_simulations (google_place_id);
