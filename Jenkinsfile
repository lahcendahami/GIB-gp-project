
def resolveGibArgs() {
    if (env.CHANGE_ID) {
        echo "PR #${env.CHANGE_ID}: ${env.CHANGE_BRANCH} → ${env.CHANGE_TARGET}"
        return "-Dgib.referenceBranch=refs/remotes/origin/${env.CHANGE_TARGET} " +
               "-Dgib.fetchReferenceBranch=true"
    } else {
        def branch         = env.BRANCH_NAME
        def lastSuccessful = env.GIT_PREVIOUS_SUCCESSFUL_COMMIT

        switch (branch) {
            case 'main':
            case 'master':
                if (lastSuccessful) {
                    echo "[main] Comparing against last successful → ${lastSuccessful}"
                    return "-Dgib.referenceBranch=${lastSuccessful}"
                }
                echo "[main] No history → full build"
                return "-Dgib.disable=true"

            case 'develop':
                if (lastSuccessful) {
                    echo "[develop] Comparing against last successful → ${lastSuccessful}"
                    return "-Dgib.referenceBranch=${lastSuccessful}"
                }
                echo "[develop] First build → comparing against main"
                return "-Dgib.referenceBranch=refs/remotes/origin/main " +
                       "-Dgib.fetchReferenceBranch=true"

            case ~/^hotfix\/.+/:
                if (lastSuccessful) {
                    echo "[hotfix] Comparing against last successful → ${lastSuccessful}"
                    return "-Dgib.referenceBranch=${lastSuccessful}"
                }
                echo "[hotfix] First build → comparing against main"
                return "-Dgib.referenceBranch=refs/remotes/origin/main " +
                       "-Dgib.fetchReferenceBranch=true"

            case ~/^(feature|release|bugfix)\/.+/:
                if (lastSuccessful) {
                    echo "[${branch}] Comparing against last successful → ${lastSuccessful}"
                    return "-Dgib.referenceBranch=${lastSuccessful}"
                }
                echo "[${branch}] First build → comparing against develop"
                return "-Dgib.referenceBranch=refs/remotes/origin/develop " +
                       "-Dgib.fetchReferenceBranch=true"

            default:
                if (lastSuccessful) {
                    echo "[${branch}] Comparing against last successful → ${lastSuccessful}"
                    return "-Dgib.referenceBranch=${lastSuccessful}"
                }
                echo "[${branch}] comaparing against develop"
                return "-Dgib.referenceBranch=refs/remotes/origin/develop " +
                       "-Dgib.fetchReferenceBranch=true"
        }
    }
}

def resolveDockerTag() {
    def baseVersion = readMavenPom().getVersion().replaceAll('-SNAPSHOT$', '')
    def gitHash = bat(script: '@git rev-parse --short=4 HEAD', returnStdout: true).trim()

    switch (env.BRANCH_NAME) {
        case 'main':
        case 'master':
            return "${baseVersion}-${gitHash}"
        case ~/^release\/.*/:
            return "${baseVersion}-${gitHash}-RELEASE"
        case ~/^hotfix\/.*/:
            return "${baseVersion}-${gitHash}-HOTFIX"
        default:
            def safeBranch = env.BRANCH_NAME.replaceAll('/', '-')
            return "${baseVersion}-${gitHash}-${safeBranch}-SNAPSHOT"
    }
}

// ── Pipeline ──────────────────────────────────────────────────────

pipeline {
    agent any
    options {
        disableConcurrentBuilds()
    }
    environment {
        SONAR_TOKEN = credentials('sonar-token')
    }
    tools {
        maven 'maven'
        jdk 'jdk-17'
    }
    stages {
        stage('Resolve GIB & Version') {
            steps {
                script {
                    env.GIB_ARGS = resolveGibArgs()
                    env.VERSION  = resolveDockerTag()
                    echo "GIB_ARGS = ${env.GIB_ARGS}"
                    echo "VERSION  = ${env.VERSION}"
                }
            }
        }

        stage('Build') {
            steps {
                bat "mvn clean install -DskipTests ${env.GIB_ARGS} -s ./.mvn/settings.xml"
            }
        }

        stage('Tests & Coverage') {
            steps {
                bat "mvn test ${env.GIB_ARGS} -s ./.mvn/settings.xml"
            }
        }

        stage('Publish Artifacts - Nexus') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                bat "mvn deploy -Dmaven.test.skip=true ${env.GIB_ARGS} -s ./.mvn/settings.xml"
            }
        }

        stage('Build & Push Docker Images') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                    branch 'master'
                    branch pattern: "release/*", comparator: "GLOB"
                    branch pattern: "hotfix/*", comparator: "GLOB"
                }
            }
            steps {
                bat "mvn jib:build -Dmaven.test.skip=true -Djib.to.tags=${env.VERSION} ${env.GIB_ARGS} -s ./.mvn/settings.xml"
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
