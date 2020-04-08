def call(Map config = [:]) {
  def versionsLog = "versions.log"
  def mavenVersionCheckPlugin = " "
  def mavenVersionCheckDependency = " "

  def cmd = config.get("cmd", "")
  def recordIssue = config.get("recordIssue", true)
  def checkPlugins = config.get("checkPlugins", true)
  def checkDependencies = config.get("checkDependencies", false)
  def onlyProjectBuildPluginWithVersion = config.get("onlyProjectBuildPluginWithVersion", "")
  def additionalVersionArgs = config.get("additionalVersionArgs", "")

  def mavenVersionArguments = addVersionRules(onlyProjectBuildPluginWithVersion) + additionalVersionArgs

  if (checkPlugins) {
    mavenVersionCheckPlugin = " org.codehaus.mojo:versions-maven-plugin:RELEASE:display-plugin-updates " + mavenVersionArguments
  }

  if (checkDependencies) {
    mavenVersionCheckDependency = " org.codehaus.mojo:versions-maven-plugin:RELEASE:display-dependency-updates " + mavenVersionArguments
  }

  maven cmd: cmd + mavenVersionCheckPlugin + mavenVersionCheckDependency + " >> " + versionsLog

  if (recordIssue) {
    recordIssues tools: [groovyScript(parserId: 'maven-version-update-parser', pattern: versionsLog)], unstableTotalAll: 1
  }

  archiveArtifacts versionsLog
}

def addVersionRules(String onlyProjectBuildPluginWithVersion)
{
  def versionRules = " "
  if (onlyProjectBuildPluginWithVersion) {
    def rulesFileName = "version-rules.xml"
    def versionRulesXML = libraryResource rulesFileName
    versionRulesXML = versionRulesXML.replace("{version}", onlyProjectBuildPluginWithVersion)
    writeFile(file: rulesFileName, text: versionRulesXML, encoding: "UTF-8")
    versionRules = " -Dmaven.version.rules=file://" + "${env.WORKSPACE}/" + rulesFileName + " "
  }
  return versionRules
}
