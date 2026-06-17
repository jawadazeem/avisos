# AVISOS — Pending Work

## 1. Email Notifications (End-to-End)

SMTP config is wired (`spring.mail.*` in application.yml, Outlook SMTP).
EmailService exists and sends via JavaMailSender. Nothing calls it yet.

- [ ] Create `AlarmNotificationHandler` — `@Async @EventListener` on `AlarmCreatedEvent`
  - Look up staff by jurisdiction using `StaffService.searchByJurisdiction()` based on alarm zone
  - Build `EmailMessage` with alarm details (severity, reason, device, timestamp)
  - Call `EmailService.send()` for each matched staff member
- [ ] Clean up duplicate `EmailService` — the stub at `service/notification/EmailService.java` collides
      with the real one at `model/notification/EmailService.java`. Delete the stub or merge them.
- [ ] Create `.env.example` with `MAIL_USERNAME`, `MAIL_PASSWORD`, `AVISOS_EMAIL_DRY_RUN` placeholders
- [ ] Generate an Outlook App Password (account.microsoft.com/security) and set `MAIL_PASSWORD`
- [ ] Set `AVISOS_EMAIL_DRY_RUN=false` and test with a live alarm — verify Gmail receives it
- [ ] Add email delivery status to Sherwood analysis or alarm detail (optional enrichment)

## 2. CI/CD Pipeline

Current state: `docker-pipeline.yml` runs on `self-hosted` runner, does `docker compose up --build`.
Problem: GitHub Actions runner daemon on the server is not reliably picking up pushes.

- [ ] Diagnose self-hosted runner — is the service running? Check `actions-runner/_diag/` logs
- [ ] Fix or replace the runner registration (re-run `./config.sh` + `./svc.sh install`)
- [ ] Add health check step after deploy (curl `http://localhost:8083/api/health`)
- [ ] Add `docker compose logs` capture on failure for debugging
- [ ] Consider adding a build-test step before deploy (mvn test in a container)
- [ ] Add seed-staff.sh execution after deploy for fresh environments
- [ ] Add rollback strategy (tag previous image, roll back on health check failure)

## 3. ML Integration (Scikit-Learn / TensorFlow)

Candidates for where ML fits in the existing architecture:

- **Anomaly detection on telemetry** — train on normal sensor patterns (temp, humidity, battery curves),
  flag deviations before they become alarms. Python microservice consuming MQTT or a batch job on
  telemetry audit table.
- **Alarm severity prediction** — classify incoming alarms as Critical/Warning based on historical
  patterns + sensor context. Could replace or augment the keyword-based `ThreatDetector`.
- **Vision model fine-tuning** — retrain or fine-tune the object detection model on datacenter-specific
  images (server racks, cable runs, water pooling) for better accuracy than the generic CodeProject.AI model.
