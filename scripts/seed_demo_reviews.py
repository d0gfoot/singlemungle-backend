#!/usr/bin/env python3
"""Copy demo review images and seed consented reviews through Docker PostgreSQL."""

import argparse
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys


ROOT = Path(__file__).resolve().parents[1]


def load_env(path: Path) -> dict[str, str]:
    values = dict(os.environ)
    if not path.exists():
        return values
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values.setdefault(key.strip(), value.strip().strip('"').strip("'"))
    return values


def sql_literal(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def main() -> int:
    parser = argparse.ArgumentParser(description="Seed demo grooming reviews")
    parser.add_argument("--manifest", default="scripts/demo-reviews.json")
    parser.add_argument("--assets-dir", default="demo/review-images")
    args = parser.parse_args()

    env = load_env(ROOT / ".env")
    entries = json.loads((ROOT / args.manifest).read_text(encoding="utf-8"))
    assets_dir = ROOT / args.assets_dir
    storage_dir = ROOT / env.get("IMAGE_STORAGE_DIRECTORY", "build/generated-images")
    base_url = env.get("IMAGE_STORAGE_BASE_URL", "/api/v1/beauty/style-simulations/images").rstrip("/")
    storage_dir.mkdir(parents=True, exist_ok=True)

    statements = ["begin;"]
    for index, entry in enumerate(entries, start=1):
        source = assets_dir / entry["file"]
        if not source.is_file():
            print(f"Missing demo image: {source}", file=sys.stderr)
            return 1
        target_name = f"demo-review-{index}{source.suffix.lower()}"
        shutil.copy2(source, storage_dir / target_name)
        marker = f"[DEMO:{index}]"
        content = f"{marker} {entry['content']}"
        image_url = f"{base_url}/{target_name}"
        statements.append(f"""
with target_review as (
    insert into reviews (user_id, place_id, rating, content, grooming_style_name, created_at, updated_at)
    select {int(entry['userId'])}, {int(entry['shopId'])}, {int(entry['rating'])},
           {sql_literal(content)}, {sql_literal(entry['styleName'])}, current_timestamp, current_timestamp
    where not exists (select 1 from reviews where content like {sql_literal(marker + '%')})
    returning id
), selected_review as (
    select id from target_review
    union all
    select id from reviews where content like {sql_literal(marker + '%')} limit 1
)
insert into review_images (review_id, image_url, ai_reference_allowed, created_at)
select id, {sql_literal(image_url)}, true, current_timestamp from selected_review
where not exists (select 1 from review_images where image_url = {sql_literal(image_url)});
""")
    statements.append("commit;")

    command = [
        "docker", "compose", "exec", "-T", "postgres", "psql",
        "-v", "ON_ERROR_STOP=1",
        "-U", env.get("DB_USERNAME", "singlemungle"),
        "-d", env.get("DB_NAME", "singlemungle"),
    ]
    subprocess.run(command, cwd=ROOT, input="\n".join(statements), text=True, check=True)
    print(f"Seeded {len(entries)} demo reviews; images: {storage_dir}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
