# Letter Generation Modernization

## Current modernization status
- Spring Boot backend scaffolded in `backend/`
- Angular frontend scaffolded in `frontend/`
- Core business models and services migrated
- Initial UI flows migrated for login, view, search, generate, preview, and download

## Target runtime
- Java 21
- Spring Boot 3.3.x
- Angular 18.x

## Project structure
- `backend/` - Spring Boot REST API and file generation logic
- `frontend/` - Angular SPA preserving the legacy UI layout and styling

## Backend run
1. Ensure Java 21 and Maven are installed.
2. From `backend/`, run:
   - `mvn spring-boot:run`

Backend default URL:
- `http://localhost:8080`

## Frontend run
1. Ensure Node.js 20+ and npm are installed.
2. From `frontend/`, install dependencies:
   - `npm install`
3. Start the Angular app:
   - `npm start`

Frontend default URL:
- `http://localhost:4200`

## Important notes
- TypeScript import errors currently shown in the editor are expected until `npm install` is executed in `frontend/`.
- Generated files are stored under:
  - `backend/data/generated-letters`
- Metadata is stored as:
  - `letters-index.csv`
- Demo login credentials:
  - username: `admin`
  - password: `admin`

## Containerized local run
From the workspace root:
1. Build and start both containers:
   - `docker compose up --build`
2. Access the application:
   - Frontend: `http://localhost:8080`
   - Backend API: `http://localhost:9090/api`
3. Stop containers:
   - `docker compose down`

Container notes:
- Frontend uses NGINX and proxies `/api` to the backend container.
- Backend persists generated letters in a Docker volume:
  - `backend-data`
- Backend runtime configuration is externalized with environment variables.

## IBM Cloud demo direction
Recommended demo deployment approach:
1. Build backend container image from `backend/Dockerfile`.
2. Build frontend container image from `frontend/Dockerfile`.
3. Deploy both containers to IBM Cloud Code Engine or Kubernetes/OpenShift.
4. Attach persistent storage for backend generated files.
5. Expose the frontend publicly and allow it to proxy `/api` traffic to the backend service.

Suggested cloud runtime configuration:
- `SERVER_PORT=9090`
- `APP_STORAGE_BASE_DIRECTORY=/app/data/generated-letters`
- `APP_STORAGE_METADATA_FILE_NAME=letters-index.csv`

## Remaining work
- Execute container smoke test with `docker compose up --build`
- Complete UI behavior parity checks
- Add IBM Cloud-specific manifests if targeting a specific IBM Cloud service
- Add end-to-end smoke validation