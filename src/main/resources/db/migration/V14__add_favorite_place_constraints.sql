alter table favorite_places
	alter column user_id set not null;

alter table favorite_places
	add constraint fk_favorite_places_user_id
	foreign key (user_id) references users (id);

create unique index uk_favorite_places_user_google_place_id
	on favorite_places (user_id, google_place_id);
