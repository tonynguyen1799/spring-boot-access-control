# Access Control System

A comprehensive, production-ready Spring Boot application for user, role, and privilege management. This project features JWT-based authentication, a full suite of RESTful APIs for administration, and a containerized setup using Docker for easy development and deployment.

## Features
- **Secure Authentication**: JWT-based authentication and authorization with refresh tokens.
- **Role & Privilege Management**: Granular control over user permissions with a flexible role-based access control (RBAC) system.
- **User Administration**: Full CRUD (Create, Read, Update, Delete) functionality for users, including enabling/disabling accounts and assigning roles.
- **Database Migrations**: Uses Liquibase for managing database schema changes.
- **Auditing**: Automatically tracks when and by whom entities are created or modified.
- **Containerized**: Fully containerized with Docker and Docker Compose for a consistent and portable development environment.

---

## Getting Started

### Prerequisites
- Java 21 or higher
- Git
- Docker and Docker Compose

### Setup and Run (Docker - Recommended)
This is the easiest and most reliable way to run the application and its database.

1.  **Clone the repository:**
    ```bash
    git clone <your-repo-url>
    cd access-control
    ```

2.  **Prepare Configuration:**
    Create a local `application.properties` file from the template. This file is ignored by Git and will store your local database credentials and JWT secret.
    ```bash
    cp application.properties.template application.properties
    ```

3.  **Build and run the services:**
    This command will build the Spring Boot application image and start both the application and the MySQL database containers.
    ```bash
    docker-compose -f docker-compose.dev.yml up --build
    ```

4.  **Access the application:**
    The API will be available at `http://localhost:8080`.

5.  **Connect to the Database (Optional):**
    You can connect to the MySQL database from your local machine using a tool like MySQL Workbench with the following settings:
    - **Hostname**: `127.0.0.1`
    - **Port**: `3306`
    - **Username**: `root`
    - **Password**: `root`

---

## API Overview

The API is secured using JWTs, which must be included in the `Authorization: Bearer <token>` header for protected endpoints.

-   **Authentication (`/api/auth`)**
    -   `POST /signin`: Authenticate a user and receive an access token and refresh token.
    -   `POST /refresh`: Get a new access token using a valid refresh token.
    -   `GET /me`: Get the details of the currently authenticated user.
    -   `POST /change-password`: Change the password for the current user.

-   **User Management (`/api/admin/users`)**
    -   `GET /`: List and filter users with pagination.
    -   `POST /`: Create a new user.
    -   `GET /{textId}`: Get details for a specific user.
    -   `PUT /{textId}/roles`: Update the roles assigned to a user.
    -   `PUT /{textId}/status`: Enable or disable a user's account.

-   **Access Control Management (`/api/admin/access-control`)**
    -   `GET /roles`: List all available roles.
    -   `POST /roles`: Create a new role.
    -   `GET /roles/{textId}`: Get details for a specific role.
    -   `PUT /roles/{textId}`: Update a role's name and assigned privileges.
    -   `DELETE /roles/{textId}`: Delete a role.
    -   `GET /privileges`: List all available system privileges.

---

## Contribution Guidelines
1.  Fork the repository.
2.  Create a new feature branch (`git checkout -b feature/your-feature-name`).
3.  Commit your changes (`git commit -m 'Add some feature'`).
4.  Push to the branch (`git push origin feature/your-feature-name`).
5.  Open a Pull Request.

## License
This project is licensed under the MIT License.

