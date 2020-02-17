def call(Map config) {
  configFileProvider([configFile(fileId: 'global-maven-settings', variable: 'GLOBAL_MAVEN_SETTINGS')]) {
    def mavenCommand = "mvn --batch-mode -gs $GLOBAL_MAVEN_SETTINGS -e -Duser.timezone=Europe/Zurich ${config.cmd} "
    if (isUnix()) {
      sh mavenCommand
    } else {
      bat mavenCommand + " -Dmaven.repo.local=${WORKSPACE}/.m2-repo"
    }
  }
}
