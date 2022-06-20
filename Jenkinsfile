pipeline {
    agent any
    stages {
        stage('build') {
            steps {
                sh './gradlew build'
            }
        }
        
        stage('test') {
            steps {
                sh './gradlew test'
            }
        }

        stage('generate WAR') {
            steps {
                sh './gradlew war'
            }
        }

        stage('publish WAR') {
            when {
                branch 'master'
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus', usernameVariable: 'nexusUser', passwordVariable: 'nexusPass')]) {
                    sh "./gradlew publish -PnexusUser=${nexusUser} -PnexusPass=${nexusPass}"
                }
            }
        }

        stage('Build and Publish Container') {
            steps {
                sh 'docker build --no-cache --tag=temperature_server .'
                withCredentials([usernamePassword(credentialsId: 'nexus', usernameVariable: 'nexusUser', passwordVariable: 'nexusPass')]) {
                    sh 'docker login nexus.local:8080 --username $nexusUser --password $nexusPass'
                }
                sh 'docker image tag temperature_server nexus.local:8080/rhink/temperature_server:latest'
                sh 'docker image push nexus.local:8080/rhink/temperature_server:latest'
            }
        }

        stage('Deploy') {
            steps {
                // stop and remove old server container. "||" prevents failure of stop command from failing build.
                sh 'docker stop temp_server_prod || true && docker rm temp_server_prod || true'
                sh 'docker run -e DB_HOST=database.local -v /etc/localtime:/etc/localtime:ro --name temp_server_prod --restart always --detach -p 192.168.1.32:8080:8080  -p 192.168.1.32:9990:9990 temperature_server'
            }
        }
    }
    post {
        always {
            publishCoverage adapters: [jacocoAdapter('build/reports/jacoco/test/jacocoTestReport.xml')]
        }
    }
}
