alter table beauty_style_simulations alter column shop_id drop not null;
alter table beauty_style_simulations alter column style_keyword drop not null;

alter table beauty_style_simulations add column style_preset_id bigint;
alter table beauty_style_simulations add column reference_review_image_id bigint;
alter table beauty_style_simulations add column source_type varchar(30);

alter table beauty_style_simulations
	add constraint fk_style_simulations_style_preset_id
	foreign key (style_preset_id) references grooming_style_presets (id);

alter table beauty_style_simulations
	add constraint fk_style_simulations_review_image_id
	foreign key (reference_review_image_id) references review_images (id);

create index idx_style_simulations_style_preset_id
	on beauty_style_simulations (style_preset_id);

create index idx_style_simulations_review_image_id
	on beauty_style_simulations (reference_review_image_id);
