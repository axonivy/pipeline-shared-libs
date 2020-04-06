def call(Map config = [:]) {
  def versionsLog = "versions.log"
  def mavenVersionCheckPlugin = " org.codehaus.mojo:versions-maven-plugin:RELEASE:display-plugin-updates "
  def mavenVersionCheckDependency = " org.codehaus.mojo:versions-maven-plugin:RELEASE:display-dependency-updates "

  def cmd = config.get("cmd", "")
  def recordIssue = config.get("recordIssue", true)

  maven cmd: cmd + mavenVersionCheckPlugin + mavenVersionCheckDependency + " >> " + versionsLog

  if (recordIssue) {
    recordIssues tools: [groovyScript(parserId: 'maven-version-update-parser', pattern: versionsLog)], unstableTotalAll: 1
  }

  archiveArtifacts versionsLog
}
