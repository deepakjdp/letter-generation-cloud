# Jenkins Pipeline Complete Guide
## Letter Generation Backend - Java 21 Build Pipeline

---

## 📋 Table of Contents
1. [Pipeline Overview](#pipeline-overview)
2. [Prerequisites](#prerequisites)
3. [Pipeline Configuration](#pipeline-configuration)
4. [Stage-by-Stage Breakdown](#stage-by-stage-breakdown)
5. [Understanding Each Stage](#understanding-each-stage)
6. [Reports and Artifacts](#reports-and-artifacts)
7. [Troubleshooting](#troubleshooting)
8. [Optional Features](#optional-features)

---

## 🎯 Pipeline Overview

### What This Pipeline Does
This Jenkins pipeline automates the complete build, test, security scan, and deployment process for the Letter Generation Backend application built with Spring Boot and Java 21.

### Pipeline Flow
```
┌─────────────────────────────────────────────────────────────┐
│                    JENKINS PIPELINE                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. Checkout Code from Git                                  │
│  2. Build Backend (Java 21 + Maven)                         │
│  3. Run Tests (JUnit + JaCoCo Coverage)                     │
│  4. SonarQube Analysis (Optional)                           │
│  5. OWASP Dependency Check (Security)                       │
│  6. SpotBugs + PMD Security Scan                            │
│  7. Quality Gate Check (Optional)                           │
│  8. Package JAR File                                        │
│  9. Deploy for DAST Testing                                 │
│ 10. OWASP ZAP Security Scan                                 │
│ 11. Security Summary Report                                 │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Total Stages: 11
- **Required Stages**: 9 (always run)
- **Optional Stages**: 2 (SonarQube related)

---

## 🔧 Prerequisites

### 1. Jenkins Server Requirements
- Jenkins 2.300+ installed
- Minimum 4GB RAM
- 20GB disk space

### 2. Required Jenkins Plugins
```
✓ Pipeline Plugin
✓ Git Plugin
✓ Maven Integration Plugin
✓ JaCoCo Plugin
✓ JUnit Plugin
✓ HTML Publisher Plugin
✓ Credentials Plugin
```

### 3. Required Tools on Jenkins Server

#### Java 21
```bash
# Install Java 21
brew install openjdk@21  # macOS
# or
sudo apt-get install openjdk-21-jdk  # Linux

# Path: /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
```

#### Maven 3.8+
```bash
# Install Maven
brew install maven  # macOS
# or
sudo apt-get install maven  # Linux
```

### 4. Jenkins Configuration

#### Configure JDK 21
1. Go to: **Manage Jenkins** → **Global Tool Configuration**
2. Under **JDK** section:
   - Click **Add JDK**
   - Name: `JDK 21`
   - Uncheck "Install automatically"
   - JAVA_HOME: `/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`

#### Configure Maven
1. Under **Maven** section:
   - Click **Add Maven**
   - Name: `Maven 3.8`
   - Check "Install automatically"
   - Version: Maven 3.9.x

---

## ⚙️ Pipeline Configuration

### Environment Variables
```groovy
environment {
    SONAR_HOST_URL = 'http://localhost:9000'      // SonarQube server URL
    SONAR_TOKEN = credentials('sonar-token')       // SonarQube auth token
    JAVA_HOME = tool 'JDK 21'                      // Java 21 path
    PATH = "${JAVA_HOME}/bin:${env.PATH}"          // Add Java to PATH
    DEPENDENCY_CHECK_HOME = '/opt/dependency-check' // OWASP tool path
    ZAP_HOME = '/opt/zaproxy'                      // ZAP scanner path
    ZAP_PORT = '8090'                              // ZAP port
    APP_URL = 'http://localhost:8080'              // App URL for testing
}
```

### Tools Configuration
```groovy
tools {
    maven 'Maven 3.8'  // Must match name in Jenkins config
    jdk 'JDK 21'       // Must match name in Jenkins config
}
```

---

## 📊 Stage-by-Stage Breakdown

### Stage 1: Checkout
**Purpose**: Get the latest code from Git repository

**What Happens**:
```groovy
checkout scm
```
- Clones the Git repository
- Checks out the specified branch
- Downloads all project files

**Duration**: ~5-10 seconds

**Output**:
```
Cloning repository...
Checking out branch: main
Commit: abc123def456
```

---

### Stage 2: Build Backend
**Purpose**: Compile Java code using Maven

**What Happens**:
```bash
cd backend
mvn clean compile -DskipTests
```

**Steps**:
1. Changes to `backend` directory
2. Cleans previous build artifacts
3. Downloads Maven dependencies
4. Compiles Java source code
5. Verifies Java 21 is being used

**Duration**: ~30-60 seconds (first run), ~10-20 seconds (subsequent)

**Output**:
```
Java Version: openjdk version "21.0.11"
Maven Version: Apache Maven 3.9.14
Building backend...
[INFO] Compiling 20 source files to target/classes
[INFO] BUILD SUCCESS
```

**Artifacts Created**:
- `backend/target/classes/` - Compiled .class files

---

### Stage 3: Test Backend
**Purpose**: Run all unit tests with code coverage

**What Happens**:
```bash
cd backend
mvn test
```

**Steps**:
1. JaCoCo agent starts (code coverage tool)
2. Runs all JUnit tests (45 tests)
3. Generates coverage report
4. Creates test result XML files

**Duration**: ~15-30 seconds

**Output**:
```
Running JUnit tests with JaCoCo coverage...
Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Reports Generated**:
- `backend/target/surefire-reports/*.xml` - Test results
- `backend/target/jacoco.exec` - Coverage data
- `backend/target/site/jacoco/index.html` - Coverage report

**Jenkins Displays**:
- ✅ Test Results: 45 passed
- 📊 Code Coverage: 85%+ line coverage, 75%+ branch coverage

---

### Stage 4: Backend SonarQube Analysis (Optional)
**Purpose**: Static code analysis for quality and security

**What Happens**:
```bash
cd backend
mvn sonar:sonar \
    -Dsonar.projectKey=letter-gen-backend \
    -Dsonar.host.url=${SONAR_HOST_URL} \
    -Dsonar.login=${SONAR_TOKEN}
```

**When It Runs**:
- Only if SonarQube is configured in Jenkins
- Skipped with warning if not available

**What It Checks**:
- Code smells and bugs
- Security vulnerabilities
- Code duplication
- Code complexity
- Technical debt

**Duration**: ~1-2 minutes

**Output**:
```
[INFO] Analysis report uploaded in 1234ms
[INFO] ANALYSIS SUCCESSFUL
```

**Skip Message** (if not configured):
```
⚠️  SonarQube not configured. Skipping analysis.
To enable: Configure SonarQube server in Jenkins
```

---

### Stage 5: SAST - Dependency Check
**Purpose**: Check for known vulnerabilities in dependencies

**What Happens**:
```bash
cd backend
mvn org.owasp:dependency-check-maven:check \
    -DfailBuildOnCVSS=7
```

**Steps**:
1. Downloads OWASP Dependency Check database
2. Scans all Maven dependencies
3. Checks against CVE database
4. Generates HTML report

**Duration**: ~2-5 minutes (first run), ~30-60 seconds (subsequent)

**What It Checks**:
- Known vulnerabilities (CVEs)
- Outdated dependencies
- Security advisories
- License issues

**Output**:
```
Checking dependencies for vulnerabilities...
Scanning 150 dependencies...
Found 0 high-severity vulnerabilities
[INFO] BUILD SUCCESS
```

**Report**: `backend/target/dependency-check-report.html`

**Example Findings**:
```
CVE-2023-12345: Spring Framework 5.x - SQL Injection
Severity: HIGH
Recommendation: Upgrade to 6.x
```

---

### Stage 6: SAST - Security Scanning
**Purpose**: Additional static security analysis

**What Happens**:
```bash
cd backend
# SpotBugs - Find bugs and security issues
mvn compile spotbugs:check -Dspotbugs.failOnError=false

# PMD - Code quality and security rules
mvn pmd:check -Dpmd.failOnViolation=false
```

**SpotBugs Checks**:
- Null pointer dereferences
- SQL injection vulnerabilities
- Cross-site scripting (XSS)
- Insecure random number generation
- Hard-coded passwords

**PMD Checks**:
- Unused variables
- Empty catch blocks
- Inefficient code
- Security best practices

**Duration**: ~30-60 seconds

**Output**:
```
Running SpotBugs security analysis...
[INFO] No bugs found
Running PMD security analysis...
[INFO] PMD processing completed
```

**Reports**:
- `backend/target/spotbugs/spotbugsXml.html`
- `backend/target/pmd.html`

---

### Stage 7: Quality Gate (Optional)
**Purpose**: Verify code meets quality standards

**What Happens**:
```groovy
timeout(time: 5, unit: 'MINUTES') {
    waitForQualityGate abortPipeline: false
}
```

**When It Runs**:
- Only if SonarQube analysis completed
- Waits for SonarQube to process results

**Quality Checks**:
- Code coverage > 80%
- No critical bugs
- No blocker issues
- Technical debt < threshold

**Duration**: ~30-60 seconds

**Possible Results**:
- ✅ **PASSED**: All quality gates met
- ⚠️  **WARNING**: Some issues found but not critical
- ❌ **FAILED**: Critical issues found (doesn't stop build)

**Skip Message** (if not configured):
```
⚠️  SonarQube Quality Gate check skipped (SonarQube not configured)
```

---

### Stage 8: Package Backend
**Purpose**: Create deployable JAR file

**What Happens**:
```bash
cd backend
mvn package -DskipTests
```

**Steps**:
1. Packages compiled classes
2. Includes dependencies in JAR
3. Creates Spring Boot executable JAR
4. Archives artifact in Jenkins

**Duration**: ~10-20 seconds

**Output**:
```
Creating JAR file...
[INFO] Building jar: target/letter-gen-backend.jar
JAR created: target/letter-gen-backend.jar
```

**Artifact**:
- `backend/target/letter-gen-backend.jar` (50-60 MB)
- Includes all dependencies
- Executable with `java -jar`

**Jenkins Archives**:
- JAR file available for download
- Fingerprinted for tracking

---

### Stage 9: DAST - Deploy for Testing
**Purpose**: Start application for dynamic security testing

**What Happens**:
```bash
cd backend
# Kill any existing instance
pkill -f "letter-gen-backend" || true

# Start application in background
nohup java -jar target/letter-gen-backend.jar > app.log 2>&1 &
APP_PID=$!

# Wait for application to be ready
for i in {1..30}; do
    if curl -s http://localhost:8080/api/letters/templates > /dev/null 2>&1; then
        echo "Application is ready!"
        break
    fi
    sleep 2
done
```

**Steps**:
1. Stops any running instance
2. Starts Spring Boot application
3. Waits for app to be ready (max 60 seconds)
4. Verifies endpoints are accessible

**Duration**: ~10-20 seconds

**Output**:
```
Starting Spring Boot application for DAST testing...
Application PID: 12345
Waiting for application to start...
Application is ready!
Application started on http://localhost:8080
```

**Endpoints Tested**:
- `http://localhost:8080/api/letters/templates`
- `http://localhost:8080/actuator/health`

---

### Stage 10: DAST - ZAP Security Scan
**Purpose**: Dynamic security testing of running application

**What Happens**:
```bash
# If ZAP is installed
zap-cli start
zap-cli open-url http://localhost:8080
zap-cli spider http://localhost:8080
zap-cli active-scan http://localhost:8080
zap-cli report -o zap-report.html

# If ZAP not installed (fallback)
curl -I http://localhost:8080  # Check security headers
```

**What It Tests**:
- SQL injection
- Cross-site scripting (XSS)
- Security headers
- Authentication issues
- Session management
- Input validation

**Duration**: ~2-5 minutes (with ZAP), ~10 seconds (fallback)

**Output**:
```
Running OWASP ZAP DAST scan...
Spidering application...
Found 15 URLs
Running active scan...
Scan completed: 0 high, 2 medium, 5 low alerts
```

**Report**: `zap-report.html`

**Example Findings**:
```
MEDIUM: Missing Anti-CSRF Tokens
URL: http://localhost:8080/api/letters/generate
Recommendation: Implement CSRF protection

LOW: X-Content-Type-Options Header Missing
Recommendation: Add header: X-Content-Type-Options: nosniff
```

**Cleanup**:
- Stops the application
- Kills background process
- Removes PID file

---

### Stage 11: Security Summary
**Purpose**: Display comprehensive security testing results

**What Happens**:
```bash
echo "Security Testing Summary"
echo "SAST: SonarQube, Dependency Check, SpotBugs, PMD"
echo "DAST: OWASP ZAP"
echo "View all reports in Jenkins build artifacts"
```

**Summary Includes**:
- ✅ Static Analysis (SAST) results
- ✅ Dynamic Analysis (DAST) results
- ✅ Dependency vulnerabilities checked
- ✅ Code quality gate status

**Duration**: ~1 second

**Output**:
```
╔════════════════════════════════════════════════════════╗
║         SECURITY TESTING SUMMARY                       ║
╚════════════════════════════════════════════════════════╝

✅ SAST (Static Application Security Testing):
   - SonarQube Code Quality & Security Analysis
   - OWASP Dependency Check (Vulnerable Dependencies)
   - SpotBugs Security Analysis
   - PMD Code Quality & Security Rules

✅ DAST (Dynamic Application Security Testing):
   - OWASP ZAP Dynamic Security Scan
   - Runtime Vulnerability Detection
   - Security Headers Validation

📊 Security Reports Available:
   - SonarQube: http://localhost:9000
   - OWASP Dependency Check Report
   - SpotBugs Security Report
   - OWASP ZAP DAST Report

View all reports in Jenkins build artifacts!
```

---

## 📁 Reports and Artifacts

### Test Reports
| Report | Location | Description |
|--------|----------|-------------|
| JUnit Test Results | Jenkins UI | 45 tests, pass/fail status |
| JaCoCo Coverage | `backend/target/site/jacoco/index.html` | Line and branch coverage |

### Security Reports
| Report | Location | Description |
|--------|----------|-------------|
| SonarQube Dashboard | `http://localhost:9000` | Code quality & security |
| Dependency Check | `backend/target/dependency-check-report.html` | CVE vulnerabilities |
| SpotBugs | `backend/target/spotbugs/spotbugsXml.html` | Bug patterns |
| PMD | `backend/target/pmd.html` | Code quality issues |
| OWASP ZAP | `zap-report.html` | Dynamic security scan |

### Build Artifacts
| Artifact | Location | Size | Description |
|----------|----------|------|-------------|
| JAR File | `backend/target/letter-gen-backend.jar` | ~50MB | Executable Spring Boot app |
| Original JAR | `backend/target/letter-gen-backend.jar.original` | ~5MB | Without dependencies |

---

## 🔍 Troubleshooting

### Common Issues and Solutions

#### 1. JaCoCo Plugin Error
**Error**: `No plugin found for prefix 'jacoco'`

**Cause**: Calling jacoco plugin directly instead of through lifecycle

**Solution**: ✅ Already fixed in Jenkinsfile
```groovy
// ❌ Wrong
mvn test jacoco:report

// ✅ Correct
mvn test  // JaCoCo runs automatically
```

#### 2. Java Version Mismatch
**Error**: `Byte Buddy does not support Java 25`

**Cause**: Jenkins using wrong Java version

**Solution**:
```groovy
environment {
    JAVA_HOME = tool 'JDK 21'
    PATH = "${JAVA_HOME}/bin:${env.PATH}"
}
```

#### 3. Quality Gate Fails
**Error**: `waitForQualityGate` times out

**Cause**: SonarQube not configured

**Solution**: ✅ Already fixed - stage is optional
```groovy
when {
    expression { 
        return fileExists('/opt/sonarqube') || env.SONAR_HOST_URL != null
    }
}
```

#### 4. Tests Fail
**Error**: Tests failing in Jenkins but pass locally

**Possible Causes**:
- Different Java version
- Missing environment variables
- Database not available

**Solution**:
```bash
# Check Java version
java -version

# Run tests locally with same command
cd backend
mvn clean test
```

#### 5. Out of Memory
**Error**: `java.lang.OutOfMemoryError: Java heap space`

**Solution**: Increase Maven memory in `backend/.mvn/jvm.config`
```
-Xmx2048m
```

---

## 🎛️ Optional Features

### Enable SonarQube

**1. Install SonarQube**:
```bash
# Using Docker
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest

# Or download from https://www.sonarqube.org/downloads/
```

**2. Configure in Jenkins**:
1. Go to: **Manage Jenkins** → **Configure System**
2. Find **SonarQube servers** section
3. Click **Add SonarQube**
4. Name: `SonarQube`
5. Server URL: `http://localhost:9000`
6. Server authentication token: Add credential `sonar-token`

**3. Generate Token in SonarQube**:
1. Login to SonarQube: `http://localhost:9000`
2. Go to: **My Account** → **Security** → **Generate Tokens**
3. Name: `jenkins`
4. Copy token and add to Jenkins credentials

### Enable OWASP ZAP

**1. Install ZAP**:
```bash
# macOS
brew install --cask owasp-zap

# Linux
wget https://github.com/zaproxy/zaproxy/releases/download/v2.14.0/ZAP_2.14.0_Linux.tar.gz
tar -xvf ZAP_2.14.0_Linux.tar.gz -C /opt/
```

**2. Install ZAP CLI**:
```bash
pip install zapcli
```

**3. Update Jenkinsfile** (already configured):
```groovy
environment {
    ZAP_HOME = '/opt/zaproxy'
}
```

### Customize Pipeline

**Skip Security Stages**:
```groovy
stage('SAST - Dependency Check') {
    when {
        expression { return false }  // Skip this stage
    }
    // ...
}
```

**Add Email Notifications**:
```groovy
post {
    failure {
        emailext (
            subject: "Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
            body: "Check console output at ${env.BUILD_URL}",
            to: "team@example.com"
        )
    }
}
```

**Add Slack Notifications**:
```groovy
post {
    success {
        slackSend (
            color: 'good',
            message: "Build Successful: ${env.JOB_NAME} - ${env.BUILD_NUMBER}"
        )
    }
}
```

---

## 📈 Pipeline Metrics

### Typical Build Times
| Stage | Duration | Notes |
|-------|----------|-------|
| Checkout | 5-10s | Depends on repo size |
| Build | 30-60s | First run slower |
| Test | 15-30s | 45 tests |
| SonarQube | 1-2m | Optional |
| Dependency Check | 2-5m | First run slower |
| Security Scan | 30-60s | |
| Quality Gate | 30-60s | Optional |
| Package | 10-20s | |
| Deploy | 10-20s | |
| DAST | 2-5m | With ZAP |
| Summary | 1s | |
| **Total** | **7-12 minutes** | Without optional stages: 5-8 min |

### Resource Usage
- **CPU**: 2-4 cores recommended
- **Memory**: 4GB minimum, 8GB recommended
- **Disk**: 20GB for dependencies and reports
- **Network**: Required for Maven Central, SonarQube

---

## 🎯 Success Criteria

### Build Passes When:
- ✅ All 45 tests pass
- ✅ Code coverage ≥ 85% (line), ≥ 75% (branch)
- ✅ No critical security vulnerabilities
- ✅ JAR file created successfully
- ✅ No high-severity bugs found

### Build May Warn When:
- ⚠️  Medium-severity vulnerabilities found
- ⚠️  Code coverage below target but above minimum
- ⚠️  Code smells detected
- ⚠️  Technical debt increased

### Build Fails When:
- ❌ Tests fail
- ❌ Compilation errors
- ❌ Critical security vulnerabilities (CVSS ≥ 7)
- ❌ Coverage below minimum threshold

---

## 📚 Additional Resources

### Documentation
- [Jenkins Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Maven Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/)
- [OWASP ZAP](https://www.zaproxy.org/docs/)

### Support Files
- `Jenkinsfile` - Pipeline definition
- `JENKINS_SETUP.md` - Jenkins configuration guide
- `jenkins-build.sh` - Standalone build script
- `backend/pom.xml` - Maven configuration

---

## 🎓 Learning Path

### For Beginners
1. Start with basic build: Stages 1-3, 8
2. Add testing: Stage 3 with reports
3. Add packaging: Stage 8
4. Gradually add security stages

### For Advanced Users
1. Customize security thresholds
2. Add custom security rules
3. Integrate with other tools
4. Set up multi-branch pipeline
5. Add deployment stages

---

**Last Updated**: 2026-05-03  
**Pipeline Version**: 2.0  
**Java Version**: 21  
**Spring Boot Version**: 3.3.5