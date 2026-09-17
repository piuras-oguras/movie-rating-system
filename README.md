# Movie Rating System

A web application for browsing, rating, and reviewing movies, built with **Spring Boot** and **Vaadin Flow**. Users can search the movie catalog, leave star ratings and comments, get a random movie suggestion based on filters, and manage the movie catalog itself.

## Features

- **Browse & search movies** — filter the catalog by title, director, genre, and a minimum/maximum rating range.
- **Movie details** — poster, description, director, release year, genre, embedded trailer, and the current average rating.
- **Rate & review** — logged-in users can rate a movie from 1 to 5 stars with an optional comment. Rating the same movie again updates the existing review instead of creating a duplicate, and the movie's average rating is recalculated automatically.
- **Random movie suggestion** — get a random pick filtered by genre/rating range; recently suggested movies are remembered so the same title isn't repeated right away.
- **Movie management (admin only)** — add, edit, and delete movies in the catalog through a dedicated management view.
- **Review management (admin only)** — look up all reviews left by a given username, with inline edit and delete actions.
- **Accounts & roles** — username/password registration and login, with passwords hashed using BCrypt. New registrations always get the `USER` role; a single `ADMIN` account is seeded on startup (see [Configuration](#configuration)).

### Roles

| Role | Can do |
|---|---|
| `USER` (self-registered) | Browse movies, view details, rate/review movies, get random suggestions |
| `ADMIN` (predefined, seeded on startup) | Everything a `USER` can do, plus manage the movie catalog (`/movie-management`) and manage/edit/delete any user's reviews (`/reviews`) |

Navigation items for admin-only screens are hidden for non-admin users, and the corresponding views also reject direct navigation (via `BeforeEnterObserver`) for anyone who isn't logged in as an admin, redirecting to the home page with a notification.

## Tech Stack

| Layer          | Technology                                   |
|----------------|-----------------------------------------------|
| Language       | Java 21                                        |
| Backend        | Spring Boot 3.5, Spring Data JPA               |
| UI             | Vaadin Flow 24.8                               |
| Database       | H2 (in-memory)                                 |
| Security       | Spring Security Crypto (BCrypt password hashing) |
| Build tool     | Maven (with the Maven Wrapper, `mvnw`)         |

## Project Structure

```
src/main/java/com/project/movieratingsystem/
├── MovieRatingSystemApplication.java   # Spring Boot entry point
├── config/
│   ├── DataLoader.java                 # seeds a couple of sample movies on startup
│   └── PasswordEncoderConfig.java      # BCrypt PasswordEncoder bean
├── model/                              # JPA entities: Movie, Rating, User, Genre, RatingStar
├── repository/                         # Spring Data repositories
├── services/                           # business logic (MovieService, RatingService, UserService)
└── views/                              # Vaadin UI views
    ├── MainLayout.java                 # shared app shell: navigation drawer + auth section
    ├── homepage/                       # movie catalog + search filters
    ├── details/                        # single movie page + rating dialog
    ├── reviews/                        # admin-only review management (search by user, edit/delete)
    ├── suggestedmovie/                 # random movie suggestion
    ├── moviemanagement/                # admin-only catalog CRUD
    ├── auth/                           # login & registration
    ├── components/                     # reusable UI components (MovieCard)
    └── util/                           # shared UI helpers (RatingFormatter, ConfirmationDialogs)
```

## Data Model

- **Movie** — title, director, release year, genre, description, image/trailer URLs, aggregated `rating` and `ratingsCount`.
- **User** — username, hashed password, `Role` (`USER` or `ADMIN`).
- **Rating** — one review of a movie by a username: a `RatingStar` score (1–5), an optional comment, and timestamps. A `(movie_id, user_name)` pair is unique, so a user can only have one active rating per movie.
- **Genre** — enum: Action, Comedy, Drama, Thriller, Sci-Fi, Crime, Horror.
- **RatingStar** — enum: `ONE_STAR` … `FIVE_STARS`, each carrying a display label (`★★★☆☆`) and a numeric value used for averaging.

## Getting Started

### Prerequisites

- JDK 21+
- No local Maven installation required — the project ships with the Maven Wrapper (`mvnw` / `mvnw.cmd`).

### Run in development mode

```bash
./mvnw spring-boot:run
```

On Windows:

```bat
mvnw.cmd spring-boot:run
```

The app starts on **http://localhost:8081** (configurable via the `PORT` environment variable) and opens the browser automatically. The H2 console is available at `/h2-console` for inspecting the in-memory database.

### Build a production package

```bash
./mvnw clean package -Pproduction
```

### Run tests

```bash
./mvnw test
```

## Configuration

Key settings live in `src/main/resources/application.properties`:

| Property | Purpose |
|---|---|
| `server.port` | HTTP port (defaults to `8081`, overridable via `PORT`) |
| `vaadin.allowed-packages` | Packages Vaadin scans for UI components/views |
| `spring.datasource.url` | H2 in-memory database URL |
| `spring.jpa.hibernate.ddl-auto` | Schema is auto-created/updated on startup (`update`) |
| `spring.h2.console.enabled` | Enables the H2 web console at `/h2-console` |

The database is in-memory: **all data resets every time the application restarts**, and `DataLoader` seeds two sample movies (*The Matrix*, *Incepcja*) if the catalog is empty, plus a predefined admin account:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | `ADMIN` |

Change `DataLoader.ADMIN_USERNAME` / `ADMIN_PASSWORD` before deploying anywhere real — these are development-only seed credentials.

## Application Routes

| Route | View | Description |
|---|---|---|
| `/` | `HomePage` | Movie catalog with search filters |
| `/details/{id}` | `DetailsView` | Movie details, reviews, and rating dialog |
| `/reviews` | `ReviewsView` | **Admin only.** Look up and edit/delete reviews by username |
| `/randomMovie` | `SuggestedMovieView` | Random movie suggestion |
| `/movie-management` | `MovieManagementView` | **Admin only.** Add/edit/delete movies |
| `/login` | `LoginView` | Sign in |
| `/register` | `RegisterView` | Create an account |

## Known Limitations

- Authentication and authorization are session-based only (the logged-in username and role are stored in the `VaadinSession`), enforced per-view with `BeforeEnterObserver` — this is not a full Spring Security setup with method-level security or CSRF protection.
- There is no "promote to admin" UI; the only way to create an admin account is via the seeded `DataLoader` account (or manually in the database).
- The H2 database is in-memory and non-persistent; restarting the app wipes all data — including any admin promotions made directly in the database — back to the seeded sample.
