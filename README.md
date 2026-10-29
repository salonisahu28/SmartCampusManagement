<<<<<<< HEAD
# Smart Campus — Complaint Management System (Backend)

Matches the tech stack and API contracts from the Presentation-2 deck (Slides 5–7):
Java 25 (LTS) + Spring Boot 3.5, Spring Security + JWT, MySQL, Email/SMTP notifications.

## Run it
1. Install Java 25 and Maven. Start MySQL and create the database: `CREATE DATABASE smart_campus;`. The application initializes the tables and seeds the prototype's departments on startup.
2. Set `JWT_SECRET` to a Base64-encoded key containing at least 32 random bytes (for example, generate one with `openssl rand -base64 32`). Set `DB_PASSWORD` to the MySQL account password. Optionally set `DB_URL` and `DB_USERNAME` (defaults to the local database and `root`).
3. To create the first administrator, set `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` before first launch. Optionally set `BOOTSTRAP_ADMIN_NAME`. This creates an admin only when that email does not already exist. The admin can provision staff with `POST /api/admin/users`; faculty accounts must have a department.
4. From this project directory run `mvn spring-boot:run`, then open `http://localhost:8080/`. The prototype UI is served by Spring and uses the database-backed API; do not open the HTML directly from disk.
5. Email notifications are disabled by default. To enable them set `SMTP_ENABLED=true` and configure `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, and `SPRING_MAIL_PASSWORD`.

## Endpoints (slide 7's API contracts)
- `POST /api/auth/register` — create a Student account; staff registration is rejected
- `POST /api/auth/login` — returns a JWT
- `GET  /api/departments` — list departments used by the UI
- `POST /api/admin/users` — Admin provisions an Admin or Faculty account
- `POST /api/complaints` — Student files a complaint
- `GET  /api/complaints/mine` — list the signed-in student's complaints and status history
- `GET  /api/complaints/{id}` — status tracking
- `GET  /api/complaints` — Admin/Faculty dashboard listing
- `PUT  /api/complaints/{id}/status` — Admin updates status → triggers email notification
- `GET  /api/analytics` — department-wise pending workload

Student registration fields: `name`, `email`, `password`, optional `departmentId`. Login fields: `email`, `password`. Staff accounts cannot self-register; Admin creates them through the protected endpoint with `name`, `email`, `password`, `role` (`ADMIN` or `FACULTY`), and a required `departmentId` for Faculty.

## Module → code map (for the Slide 9 code walkthrough)
- **Student / Admin / Faculty modules** → `controller/ComplaintController.java`, `entity/User.java`, `entity/Role.java`
- **Security module** → `security/JwtUtil.java`, `security/JwtAuthFilter.java`, `config/SecurityConfig.java`
- **Notifications module** → `service/NotificationService.java`
- **Analytics module** → `service/ComplaintService.java#pendingByDepartment`, `controller/AnalyticsController.java`
- **Database / ER schema** → `resources/schema.sql`
=======
# SmartCampusManagement
>>>>>>> origin/main
