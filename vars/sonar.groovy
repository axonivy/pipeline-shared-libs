void call(config = [:]) {

  def projectName = config.projectName
  if (!projectName) {
    fail('projectName')
  }

  def mvnPhase = config.mvnPhase
  if (!mvnPhase) {
    mvnPhase = 'package'
  }

  def mvnArgs = config.mvnArgs
  if (!mvnArgs) {
    mvnArgs = ''
  }

  withSonarQubeEnv() {
    maven cmd: mvnPhase + ' org.sonarsource.scanner.maven:sonar-maven-plugin:sonar ' +
              '-Dsonar.projectKey=' + projectName +
              ' ' +
              '-Dsonar.projectName=' + projectName +
              ' ' +
              mvnArgs
  }
  

  timeout(time: 5, unit: 'MINUTES') {
    waitForQualityGate abortPipeline: true
  }
}

def fail(String paramName) {
  echo "ERROR: no ${paramName} provided."
  throw new Exception("no ${paramName} provided.")
}
