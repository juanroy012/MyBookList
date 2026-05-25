# MyBookList

[![CI](https://github.com/juanroy012/MyBookList/actions/workflows/ci.yml/badge.svg)](https://github.com/juanroy012/MyBookList/actions/workflows/ci.yml)
[![Deploy](https://github.com/juanroy012/MyBookList/actions/workflows/deploy-fly.yml/badge.svg)](https://github.com/juanroy012/MyBookList/actions/workflows/deploy-fly.yml)

A self-hosted personal reading tracker for managing your book collection, reading progress, and reviews. With per-user data isolation and JWT authentication.

🔗 **Live demo: [mybooklist.juan-roy.com](https://mybooklist.juan-roy.com/)**

---

## Dashboard

Get a quick overview of your reading activity at a glance; books in progress, recently completed, and your overall stats all in one place.

## Book List

Add, edit, and delete books from your list. Filter by reading status, search by title or author, and paginate through your collection.

## Reading Progress

Track your progress through each book with page counts and status updates; from "Want to Read" to "Completed".

## Reviews & Ratings

Leave personal reviews and star ratings for books you've finished to keep your thoughts organized.

## Theming

Switch between dark and light mode. Your preference is saved across sessions.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Security, Spring Data JPA |
| Auth | JWT (`jjwt`), bcrypt password hashing |
| Frontend | React 18, Vite 8, Tailwind CSS v3, Recharts 2 |
| Database | PostgreSQL |
| CI/CD | GitHub Actions — JUnit tests + frontend build check on every push |
| Deployment | Fly.io, Docker multi-stage build |
