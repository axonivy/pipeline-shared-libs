def call(Map config = [:]) {
  def versionsLog = "versions.log"
  def mavenVersionCheckPlugin = " "
  def mavenVersionCheckDependency = " "

  def cmd = config.get("cmd", "")
  def recordIssue = config.get("recordIssue", true)
  def checkPlugins = config.get("checkPlugins", true)
  def checkDependencies = config.get("checkDependencies", false)

  if (checkPlugins) {
    mavenVersionCheckPlugin = " org.codehaus.mojo:versions-maven-plugin:RELEASE:display-plugin-updates "
  }

  if (checkDependencies) {
    mavenVersionCheckDependency = " org.codehaus.mojo:versions-maven-plugin:RELEASE:display-dependency-updates "
  }

  maven cmd: cmd + mavenVersionCheckPlugin + mavenVersionCheckDependency + " >> " + versionsLog

  if (recordIssue) {
    recordIssues tools: [groovyScript(parserId: 'maven-version-update-parser', pattern: versionsLog)], unstableTotalAll: 1
  }

  archiveArtifacts versionsLog
}
