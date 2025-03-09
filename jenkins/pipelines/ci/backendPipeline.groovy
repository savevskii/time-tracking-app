def call(boolean isRelease, String releaseVersion, String developmentVersion) {

    environment {
        DOCKER_REGISTRY = 'docker-registry-internal.netcetera.com/fsavevsk'
        IMAGE_NAME = 'time-tracking-app'
        VERSION = ''
    }

    stage('Pre Flight') {
        steps {
            script {
                VERSION = sh(script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout", returnStdout: true).trim()
            }
        }
    }

    stage('Maven Build & Unit Tests') {
        steps {
            scripts {
                sh "mvn -ntp --batch-mode clean install"

                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
            }
        }
    }

    stage('Release Build') {
        when {
            allOf {
                branch "master"
                expression {
                    return isRelease
                }
            }
        }
        steps {
            script {
                sh """
                mvn --batch-mode -ntp \
                  -Dusername=\"${env.BITBUCKET_USER_USR}\" \
                  -Dpassword=\"${env.BITBUCKET_USER_PSW}\" \
                  -DreleaseVersion=\"${releaseVersion}\" \
                  -DdevelopmentVersion=\"${developmentVersion}\" \
                  release:prepare release:perform
               """
            }
        }
    }

    stage('Build and Publish Docker Image') {
        when {
            branch 'master'
        }
        scripts {
            steps {
                sh "docker build -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${VERSION} -f jenkins/pipelines/docker/backend.Dockerfile"
                sh "docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${VERSION}"
            }
        }
    }
}
return this