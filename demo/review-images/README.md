# Demo review images

Optional review images are intentionally not committed. Place team-owned or properly licensed JPEG files here with these names:

- `demo-review-01.jpg`: Pomeranian teddy cut
- `demo-review-02.jpg`: Pomeranian seal cut
- `demo-review-03.jpg`: Poodle lion cut
- `demo-review-04.jpg`: Bichon seal cut

`demo/manage-demo-data.sh seed` copies all four files to `IMAGE_STORAGE_DIRECTORY` and creates AI-reference-enabled review image rows. If any file is absent, the seed still succeeds but creates text-only reviews.
