# 16_media-service.md

Version: 1.0
Status: FINAL

---

# Purpose

Manage media uploads and metadata.

---

# Responsibilities

- Upload URLs
- Metadata
- File Validation
- Cleanup

---

# Database

Table

- media

---

# APIs

POST /media/upload-url

POST /media/confirm

GET /media/{id}

DELETE /media/{id}

---

# Kafka Events

Publish

- media.uploaded
- media.deleted

---

# Business Rules

- File size limits
- MIME validation
- Virus scanning (Future)

---

# Security

- Signed upload URLs
- Owner validation

---

# Testing

- Upload
- Delete
- Validation

---

Status: FINAL