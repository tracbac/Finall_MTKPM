# In-app notifications

The notification center stores messages in the application database.

## Member

- `GET /api/notifications/mine` returns up to 100 newest notifications for the
  authenticated account.
- `PATCH /api/notifications/mine/{notificationId}/read` marks an owned
  notification as read.
- `PATCH /api/notifications/mine/read-all` marks all of the account's unread
  notifications as read.

Members can view and update their notifications on `/my-books.html`. The API
derives the account from the JWT; clients cannot choose another user's ID.

## Administrator

- `POST /api/notifications/admin/send` sends an in-app message to one member or
  all members. Use `{"userId": 123, "message": "..."}` for one member, or
  `{"userId": null, "message": "..."}` to broadcast.
- `GET /api/notifications/admin` returns the 100 newest notification records.

These endpoints require the `ADMIN` role. The Admin page provides the send form
and recent-history view.

Borrow requests, their approval/rejection/return, and E-Book request decisions
create in-app notifications automatically. This feature does not send email or
browser push notifications, does not store per-channel preferences, and does
not yet schedule due-date reminders.
