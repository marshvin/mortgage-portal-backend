
## Local Development

### Environment Setup

1. Create a `.env` file in the project root with the following content:

```
# Database Configuration
DB_USER=postgres
DB_PASSWORD=1234

# Development Database
DB_URL=jdbc:postgresql://localhost:5432/mortgage_db

# Production Database
DB_HOST=localhost
DB_PORT=5432

# Kafka Configuration
KAFKA_BROKER=localhost:9092

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
```

2. **Never commit your .env file to git.** It is already included in `.gitignore`.

### Running the Application

#### Option 1: Local Development
```bash
cd backend
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

#### Option 2: Docker Compose (Recommended)
```bash
# Start all services (PostgreSQL, Kafka, Zookeeper, App)
docker-compose -f infra/docker-compose.yml up -d

# View logs
docker-compose -f infra/docker-compose.yml logs -f app

# Stop all services
docker-compose -f infra/docker-compose.yml down
```

### Profiles

- **dev**: Local development with detailed logging and SQL queries
- **prod**: Production configuration with optimized settings

### Services

- **Application**: http://localhost:8080
- **PostgreSQL**: localhost:5432
- **Kafka**: localhost:9092
- **Zookeeper**: localhost:2181

