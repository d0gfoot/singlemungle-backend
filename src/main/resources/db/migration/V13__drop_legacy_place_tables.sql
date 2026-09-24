alter table reviews
	drop constraint if exists fk_reviews_place_id;
drop index if exists idx_reviews_place_id_created_at;
alter table reviews
	drop column if exists place_id;

alter table favorite_places
	drop constraint if exists fk_favorite_places_place_id;
alter table favorite_places
	drop column if exists place_id;

alter table beauty_style_simulations
	drop constraint if exists fk_beauty_style_simulations_shop_id;
alter table beauty_style_simulations
	drop column if exists shop_id;

drop table if exists place_images;
drop table if exists place_services;
drop table if exists place_business_hours;
drop table if exists beauty_styles;
drop table if exists places;
