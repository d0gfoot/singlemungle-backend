CREATE TABLE walk_records (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              pet_id BIGINT NOT NULL,
                              started_at TIMESTAMP,
                              ended_at TIMESTAMP,
                              distance_km DOUBLE PRECISION,
                              duration_minutes INTEGER,
                              calories INTEGER,
                              route_data JSONB,
                              memo VARCHAR(255),
                              created_at TIMESTAMP NOT NULL
);