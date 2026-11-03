# E-Book PDF storage

E-Book PDFs are stored in a separate **private** Supabase Storage bucket. Configure
`SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, and `SUPABASE_EBOOKS_BUCKET` in the
server environment (the bucket defaults to `ebooks`). Keep the service-role key
on the server; do not expose it in browser code or commit it to Git.

Create the configured bucket in Supabase Storage as private and allow uploads of
at least 50 MB. An administrator can upload or replace a book's PDF from the
`PDF` action in `/admin.html`. The backend accepts PDF files up to 50 MB and
stores one PDF per book.

Members download from `/my-books.html` only after a valid DRM license has been
issued. The server checks the authenticated user, license validity, remaining
download quota, overdue loans, and unpaid fines before fetching the object.
PDF bytes pass through the backend; Supabase object URLs and the service-role
key are not returned to the browser. Downloading is unavailable until an
administrator uploads the book's PDF.
