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
  
  withCredentials([string(credentialsId: 'sonar.ivyteam.io', variable: 'token')]) {
    maven cmd: mvnPhase + ' org.sonarsource.scanner.maven:sonar-maven-plugin:5.7.0.6970:sonar ' +
               '-Dsonar.host.url=https://sonar.ivyteam.io ' +
               '-Dsonar.projectKey=' + projectName + ' ' +
               '-Dsonar.projectName=' + projectName + ' ' +
               "-Dsonar.token=${token} " + mvnArgs
  }
}

def fail(String paramName) {
  echo "ERROR: no ${paramName} provided."
  throw new Exception("no ${paramName} provided.")
}
