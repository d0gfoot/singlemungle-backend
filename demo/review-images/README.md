# Demo review images

Place team-owned or properly licensed `seal-cut.jpg` and `teddy-cut.jpg` files here, adjust IDs in `scripts/demo-reviews.json`, and run:

```bash
python3 scripts/seed_demo_reviews.py
```

The command is idempotent for each manifest entry and marks its image as allowed for AI reference use.
