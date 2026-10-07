#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ACTION="${1:-seed}"
MODE="${2:-docker}"

if [[ -f "$ROOT_DIR/.env" ]]; then
	set -a
	# shellcheck disable=SC1091
	source "$ROOT_DIR/.env"
	set +a
fi

: "${DB_NAME:=singlemungle}"
: "${DB_USERNAME:=singlemungle}"
: "${DB_HOST:=localhost}"
: "${DB_PORT:=5432}"
: "${POSTGRES_CONTAINER_NAME:=singlemungle-postgres}"

run_psql() {
	if [[ "$MODE" == "docker" ]]; then
		docker exec -i "$POSTGRES_CONTAINER_NAME" psql -v ON_ERROR_STOP=1 -U "$DB_USERNAME" -d "$DB_NAME" "$@"
	elif [[ "$MODE" == "direct" ]]; then
		PGPASSWORD="${DB_PASSWORD:-}" psql -v ON_ERROR_STOP=1 -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USERNAME" -d "$DB_NAME" "$@"
	else
		echo "Mode must be 'docker' or 'direct'." >&2
		exit 2
	fi
}

case "$ACTION" in
	seed)
		: "${DEMO_OWNER_USER_ID:?Set DEMO_OWNER_USER_ID to an existing active user ID.}"
		: "${DEMO_GOOGLE_PLACE_ID_1:?Set DEMO_GOOGLE_PLACE_ID_1 to a real dog grooming Google Place ID.}"
		: "${DEMO_GOOGLE_PLACE_ID_2:=$DEMO_GOOGLE_PLACE_ID_1}"
		: "${IMAGE_STORAGE_DIRECTORY:=$ROOT_DIR/build/generated-images}"
		: "${IMAGE_STORAGE_BASE_URL:=/api/v1/beauty/style-simulations/images}"

		seed_review_images=false
		required_images=(demo-review-01.jpg demo-review-02.jpg demo-review-03.jpg demo-review-04.jpg)
		all_images_present=true
		for file_name in "${required_images[@]}"; do
			if [[ ! -f "$ROOT_DIR/demo/review-images/$file_name" ]]; then
				all_images_present=false
			fi
		done
		if [[ "$all_images_present" == true ]]; then
			mkdir -p "$IMAGE_STORAGE_DIRECTORY"
			for file_name in "${required_images[@]}"; do
				cp "$ROOT_DIR/demo/review-images/$file_name" "$IMAGE_STORAGE_DIRECTORY/$file_name"
			done
			seed_review_images=true
		else
			echo "Review images were not found; reviews will be seeded without images."
		fi

		run_psql \
			-v owner_user_id="$DEMO_OWNER_USER_ID" \
			-v place_id_1="$DEMO_GOOGLE_PLACE_ID_1" \
			-v place_id_2="$DEMO_GOOGLE_PLACE_ID_2" \
			-v seed_review_images="$seed_review_images" \
			-v image_base_url="${IMAGE_STORAGE_BASE_URL%/}" \
			-f - < "$ROOT_DIR/demo/seed-demo-data.sql"
		;;
	cleanup)
		run_psql -f - < "$ROOT_DIR/demo/cleanup-demo-data.sql"
		if [[ -n "${IMAGE_STORAGE_DIRECTORY:-}" ]]; then
			for file_name in demo-review-01.jpg demo-review-02.jpg demo-review-03.jpg demo-review-04.jpg; do
				rm -f "$IMAGE_STORAGE_DIRECTORY/$file_name"
			done
		fi
		;;
	status)
		run_psql -c "select 'demo_users' as item, count(*) from users where oauth_provider_id like 'demo-seed-user-%' union all select 'demo_reviews', count(*) from reviews where content like '[DEMO-SEED:%' union all select 'style_preferences', count(*) from grooming_style_preferences where user_id in (select id from users where oauth_provider_id like 'demo-seed-user-%') union all select 'owner_demo_pets', count(*) from pets where memo like '[DEMO-SEED] 시연용%';"
		;;
	*)
		echo "Usage: $0 {seed|cleanup|status} [docker|direct]" >&2
		exit 2
		;;
esac
