# Jenkins Setup Guide for Java 21

## Prerequisites

### 1. Install Java 21 on Jenkins Server

**For Linux/Mac:**
```bash
# Using SDKMAN (recommended)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.2-open

# Or using package manager
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-21-jdk

# Mac (Homebrew)
brew install openjdk@21
```

**For Windows:**
- Download Java 21 from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/#java21)
- Install and note the installation path

### 2. Configure Java 21 in Jenkins

1. Go to **Jenkins Dashboard** → **Manage Jenkins** → **Global Tool Configuration**

2. Under **JDK** section:
   - Click **Add JDK**
   - Name: `JDK-21`
   - Uncheck "Install automatically"
   - JAVA_HOME: Enter the path to Java 21 installation
     - Linux/Mac: `/usr/lib/jvm/java-21-openjdk-amd64` or `/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`
     - Windows: `C:\Program Files\Java\jdk-21`

3. Under **Maven** section:
   - Click **Add Maven**
   - Name: `Maven-3.9`
   - Check "Install automatically"
   - Version: Select Maven 3.9.x or later

4. Click **Save**

### 3. Configure Jenkins Job

#### Option A: Using Jenkinsfile (Recommended)

1. Create a new **Pipeline** job
2. In **Pipeline** section:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: Your repository URL
   - Script Path: `Jenkinsfile`
3. Save and build

#### Option B: Freestyle Project

1. Create a new **Freestyle project**
2. Under **Build Environment**:
   - Check "Provide Node & npm bin/ folder to PATH" (if needed)
3. Under **Build**:
   - Add build step: **Invoke top-level Maven targets**
   - Maven Version: `Maven-3.9`
   - Goals: `clean install`
   - Advanced → JVM Options: `-Djava.version=21`
4. Add another build step for tests:
   - Goals: `test jacoco:report`

### 4. Troubleshooting

#### Error: "No plugin found for prefix 'jacoco'"

**Solution 1: Clear Maven cache on Jenkins**
```bash
# On Jenkins server
rm -rf ~/.m2/repository/org/jacoco
```

**Solution 2: Force Maven to download dependencies**
Add this to your Jenkins build step:
```bash
mvn dependency:purge-local-repository -DactOnly=true -DreResolve=false
mvn clean install
```

**Solution 3: Use Maven wrapper**
```bash
# In your project root
mvn wrapper:wrapper -Dmaven=3.9.14
# Then in Jenkins, use ./mvnw instead of mvn
```

#### Error: "Java version mismatch"

Ensure Jenkins is using Java 21:
```groovy
// In Jenkinsfile
environment {
    JAVA_HOME = tool name: 'JDK-21', type: 'jdk'
    PATH = "${JAVA_HOME}/bin:${env.PATH}"
}
```

#### Error: "Byte Buddy does not support Java 25"

This means Jenkins is picking up the wrong Java version. Verify:
```bash
# In Jenkins shell
which java
java -version
echo $JAVA_HOME
```

### 5. Jenkins Pipeline Environment Variables

Add these to your Jenkinsfile if needed:
```groovy
environment {
    JAVA_HOME = tool name: 'JDK-21', type: 'jdk'
    MAVEN_HOME = tool name: 'Maven-3.9', type: 'maven'
    PATH = "${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${env.PATH}"
    MAVEN_OPTS = '-Xmx1024m -XX:MaxPermSize=512m'
}
```

### 6. Verify Setup

Run this in Jenkins shell to verify:
```bash
echo "Java Version:"
java -version
echo "Maven Version:"
mvn --version
echo "JAVA_HOME:"
echo $JAVA_HOME
```

Expected output:
```
Java Version:
openjdk version "21.0.x"
Maven Version:
Apache Maven 3.9.x
Java version: 21.0.x
JAVA_HOME:
/path/to/java-21
```

## Quick Fix for Current Error

If you're getting the JaCoCo error immediately, try this in Jenkins:

1. **Execute Shell** build step:
```bash
cd backend
export JAVA_HOME=/path/to/java-21
export PATH=$JAVA_HOME/bin:$PATH
mvn clean install -U
```

2. Or use the full path to Maven:
```bash
cd backend
/path/to/maven/bin/mvn clean install -U
```

The `-U` flag forces Maven to update dependencies, which should resolve the JaCoCo plugin issue.

## Additional Resources

- [Jenkins Java Configuration](https://www.jenkins.io/doc/book/installing/linux/#java-requirements)
- [Maven in Jenkins](https://www.jenkins.io/doc/book/pipeline/getting-started/#maven)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)