def call(Map config) {
  configFileProvider([configFile(fileId: 'global-maven-settings', variable: 'GLOBAL_MAVEN_SETTINGS')]) {
    def mavenCommand = "mvn --batch-mode -gs $GLOBAL_MAVEN_SETTINGS -e -Duser.timezone=Europe/Zurich "
    if (isUnix()) {
      sh mavenCommand + " -Dmaven.plugin.validation=BRIEF " + config.cmd
    } else {
      bat mavenCommand + "-Dmaven.repo.local=${WORKSPACE}/.m2-repo -Dmaven.plugin.validation=BRIEF " + config.cmd
    }
  }
}
