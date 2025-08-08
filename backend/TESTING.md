# Testing Documentation

This document provides comprehensive information about the testing strategy and implementation for the Mortgage Portal Backend.

## 🎯 Testing Strategy

Our testing approach follows the **Testing Pyramid** with the following layers:

1. **Unit Tests** (70%) - Fast, isolated tests for individual components
2. **Integration Tests** (20%) - Tests for component interactions
3. **E2E Tests** (10%) - Full application workflow tests

## 📊 Coverage Requirements

- **Minimum Coverage**: 80% line coverage for business logic
- **Coverage Tool**: JaCoCo
- **Coverage Reports**: Generated in `target/site/jacoco/`
- **Build Failure**: If coverage drops below threshold

## 🧪 Unit Tests

### Test Structure
```
src/test/java/com/mortgage/mortgageportal/
├── service/
│   ├── ApplicationServiceTest.java
│   └── DocumentServiceTest.java
├── mapper/
│   ├── ApplicationMapperTest.java
│   └── DocumentMapperTest.java
└── integration/
    ├── ApplicationIntegrationTest.java
    └── KafkaEventIntegrationTest.java
```

### Running Unit Tests
```bash
# Run all unit tests
mvn test

# Run specific test class
mvn test -Dtest=ApplicationServiceTest

# Run with coverage
mvn test jacoco:report
```

### Key Unit Test Features
- **Mockito** for mocking dependencies
- **AssertJ** for fluent assertions
- **JUnit 5** for test framework
- **@ExtendWith(MockitoExtension.class)** for Mockito integration

## 🔗 Integration Tests

### Testcontainers Setup
Integration tests use **Testcontainers** to spin up real infrastructure:

- **PostgreSQL**: Real database with Flyway migrations
- **Kafka**: Embedded Kafka broker for event testing
- **ActiveMQ**: Embedded broker for fallback testing

### Running Integration Tests
```bash
# Run integration tests only
mvn test -Dtest="*IntegrationTest"

# Run with specific profile
mvn test -Dspring.profiles.active=test
```

### Test Configuration
- **Profile**: `test`
- **Database**: Testcontainers PostgreSQL
- **Kafka**: Embedded Kafka
- **Port**: Random port allocation

## 🌐 E2E Tests

### Postman Collection
Complete E2E test suite using **Postman**:

- **Collection**: `postman/Mortgage-Portal-API.postman_collection.json`
- **Environment**: `postman/Mortgage-Portal-Environment.postman_environment.json`
- **Coverage**: All API endpoints and workflows

### Running E2E Tests

#### Manual Testing
1. Import collection into Postman
2. Set environment variables
3. Run collection manually

#### Automated Testing with Newman
```bash
# Install Newman
npm install -g newman

# Run collection
newman run postman/Mortgage-Portal-API.postman_collection.json \
  -e postman/Mortgage-Portal-Environment.postman_environment.json \
  --reporters cli,junit \
  --reporter-junit-export newman-results.xml
```

### E2E Test Scenarios
1. **Authentication**
   - JWT token generation for different roles
   - Token validation and expiration

2. **Application Management**
   - Create application
   - Retrieve application by ID
   - List applications with filters
   - Update application
   - Delete application

3. **Document Management**
   - Upload document metadata
   - Retrieve documents by application
   - Access control validation

4. **Decision Management**
   - Approve/reject applications
   - Decision history tracking

5. **Error Handling**
   - Unauthorized access
   - Invalid data validation
   - Resource not found scenarios

## 🔒 Security Testing

### OWASP Dependency Check
Automated security scanning for vulnerable dependencies:

```bash
# Run security scan
mvn org.owasp:dependency-check-maven:check

# View report
open target/dependency-check-report.html
```

### Security Test Coverage
- **Authentication**: JWT token validation
- **Authorization**: Role-based access control
- **Input Validation**: Request payload validation
- **SQL Injection**: Parameterized queries
- **XSS Prevention**: Output encoding

## 📈 Coverage Reports

### JaCoCo Reports
- **Location**: `target/site/jacoco/index.html`
- **Metrics**: Line, branch, and complexity coverage
- **Threshold**: 80% minimum line coverage

### Coverage Badge
Automatically generated coverage badge for README:

```markdown
![Coverage](https://img.shields.io/badge/coverage-85%25-green)
```

## 🚀 CI/CD Pipeline

### GitHub Actions Workflow
Automated testing pipeline in `.github/workflows/ci.yml`:

1. **Test Job**
   - Unit and integration tests
   - Coverage reporting
   - JaCoCo threshold validation

2. **E2E Job**
   - Newman E2E tests
   - Application startup and health checks
   - Test result reporting

3. **Build Job**
   - Application packaging
   - Artifact generation

4. **Security Job**
   - OWASP dependency check
   - Vulnerability scanning

5. **Deploy Job**
   - Staging deployment (develop branch)
   - Production deployment (main branch)

### Pipeline Triggers
- **Push**: `main`, `develop` branches
- **Pull Request**: Any branch to `main` or `develop`

## 🛠️ Test Utilities

### Test Data Builders
```java
// Example test data builder
User testUser = User.builder()
    .id(UUID.randomUUID())
    .fullName("John Doe")
    .email("john@example.com")
    .nationalId("123456789")
    .role(UserRole.APPLICANT)
    .build();
```

### Mock JWT Tokens
```java
// Generate test JWT tokens
String token = JwtTokenGenerator.generateToken("applicant@example.com", "APPLICANT");
```

### Database Test Utilities
```java
// Clean database between tests
@Transactional
@Rollback
void cleanDatabase() {
    // Database cleanup logic
}
```

## 📋 Test Checklist

### Before Running Tests
- [ ] Database is running and accessible
- [ ] Environment variables are set
- [ ] Dependencies are installed
- [ ] Test profile is active

### Test Execution
- [ ] Unit tests pass (fast execution)
- [ ] Integration tests pass (with containers)
- [ ] E2E tests pass (with Newman)
- [ ] Coverage meets threshold (80%)
- [ ] Security scan passes

### Post-Test Validation
- [ ] Coverage report generated
- [ ] Test artifacts uploaded
- [ ] Build artifacts created
- [ ] Security report reviewed

## 🔧 Troubleshooting

### Common Issues

#### Testcontainers Not Starting
```bash
# Check Docker is running
docker ps

# Increase memory for Docker
# Docker Desktop > Settings > Resources > Memory: 4GB+
```

#### Kafka Connection Issues
```bash
# Check Kafka is accessible
telnet localhost 9092

# Verify embedded Kafka configuration
spring.kafka.bootstrap-servers=localhost:9092
```

#### Database Connection Issues
```bash
# Check PostgreSQL is running
pg_isready -h localhost -p 5432

# Verify test database exists
psql -h localhost -U test_user -d test_db
```

### Debug Mode
```bash
# Run tests with debug logging
mvn test -Dspring.profiles.active=test -Dlogging.level.com.mortgage=DEBUG
```

## 📚 Additional Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Postman Newman Documentation](https://learning.postman.com/docs/running-collections/using-newman-cli/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
