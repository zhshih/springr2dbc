# Simple Web Application with Spring Boot 3

A simple web application built using **Spring Boot 3**, **Spring R2DBC**, with a focus on **resilience** and **observability**.  
This application allows you to perform CRUD operations on a collection of books in a **reactive, non-blocking** manner.

## Features
* Built with **Spring Boot 3** for modern, efficient development
* **Spring R2DBC** for reactive database persistence
* **Resilience** features to handle faults gracefully
* **Observability** including logging, metrics, and tracing
* RESTful APIs for managing books
* Fully **reactive** endpoints for improved scalability and performance

## APIs

| API | Method | Endpoint | Path/Query Parameters | Body | Description | Response |
|-----|--------|---------|----------------------|------|-------------|----------|
| getAllBooks | GET | /books | title (optional, query) | None | Returns all books. If `title` is provided, filters by title (implementation TBD). | 200 OK |
| getBookById | GET | /books/{id} | id (path) | None | Returns a book by its ID. | 200 OK or 404 Not Found |
| createBook | POST | /books | None | `{ "id": int, "title": string, "description": string }` | Creates a new book. | 201 Created |
| updateBook | PUT | /books/{id} | id (path) | `{ "title": string, "description": string }` | Updates an existing book by ID. | 200 OK or 404 Not Found |
| deleteBook | DELETE | /books/{id} | id (path) | None | Deletes a book by ID. | 204 No Content or 404 Not Found |

## Technology Stack
* **Spring Boot 3**
* **Spring R2DBC** for reactive database access
* **Resilience** (via Resilience4j or similar)
* **Observability** (metrics, logging, tracing)
* R2DBC-supported databases (H2, PostgreSQL, MySQL, etc.)

## Getting Started

1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```

## Getting Started

1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```

2. Build the project using Maven or Gradle:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

Access the APIs at http://localhost:8080/api/v1/books.

## Observability & Resilience
* Metrics, logs, and tracing are enabled for monitoring.
* Resilience patterns like retries, circuit breakers, and rate limiting are applied where appropriate.
