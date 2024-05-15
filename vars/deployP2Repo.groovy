/**
 * deploy a p2 repo (unzipped) to a server with ssh
 */
Map call(config = [:]) {
  def srcDir = config.srcDir
  if (!srcDir) {
    fail('srcDir')
  }
  String user = config.user
  user = user ?: 'ubuntu'
  String host = config.host
  host = host ?: 'p2.ivyteam.io'
  String p2RootPath = config.p2RootPath
  p2RootPath = p2RootPath ?: 'p2'

  String name = config.name
  if (!name) {
    fail('name')
  }
  String version = config.version
  if (!version) {
    fail('version')
  }
  String qualifier = config.qualifier
  qualifier = qualifier ?: new Date().format('yyyyMMdd.HHmmss')
  Boolean updateCompositeRepo = config.updateCompositeRepo
  updateCompositeRepo = updateCompositeRepo ?: true

  if (updateCompositeRepo) {
    generateCompositeRepo("${name}-${version}", qualifier)
  }

  def targetFolder = "${p2RootPath}/${name}/${version}/${qualifier}/"
  def sshHost = user + '@' + host
  docker.image('axonivy/build-container:ssh-client-1').inside {
    sshagent(['zugprojenkins-ssh']) {
      echo "Upload p2 repo from ${srcDir} to ${sshHost}:${targetFolder}"
      sh "ssh ${sshHost} mkdir -p ${targetFolder}"
      sh "rsync -r ${srcDir} ${sshHost}:${targetFolder}"
      sh "ssh ${sshHost} touch ${targetFolder}p2.ready"
      if (updateCompositeRepo) {
        def targetCompositePath = "${p2RootPath}/${name}/${version}/"
        echo "Upload p2 composite repository to ${sshHost}:${targetCompositePath}"
        sh "scp target/composite-repo/compositeArtifacts.xml ${sshHost}:${targetCompositePath}"
        sh "scp target/composite-repo/compositeContent.xml ${sshHost}:${targetCompositePath}"
        sh "scp target/composite-repo/p2.index ${sshHost}:${targetCompositePath}"
      }
    }
  }

  def url = "${host}/${name}/${version}/${qualifier}"
  def compositeUrl = updateCompositeRepo ? "${host}/${name}/${version}/" : ''
  return [url: url, compositeUrl: compositeUrl, host: host, qualifier: qualifier]
}

def fail(String paramName) {
    echo "ERROR: no ${paramName} provided."
    throw new Exception("no ${paramName} provided.")
}

def generateCompositeRepo(String repoPath, String qualifier) {
  def timestamp = String.valueOf(new Date().getTime())
  def compositeArtifactsXmlContent = """<?xml version='1.0' encoding='UTF-8'?>
<?compositeArtifactRepository version='1.0.0'?>
<repository name='${repoPath} composite repository' type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1.0.0'>
  <properties size='3'>
    <property name='p2.timestamp' value='${timestamp}'/>
    <property name='p2.compressed' value='false'/>
    <property name='p2.atomic.composite.loading' value='true'/>
  </properties>
  <children size='1'>
    <child location='${qualifier}'/>
  </children>
</repository>
"""

  def compositeContentXmlContent = """<?xml version='1.0' encoding='UTF-8'?>
<?compositeMetadataRepository version='1.0.0'?>
<repository name='${repoPath} composite repository' type='org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository' version='1.0.0'>
  <properties size='3'>
    <property name='p2.timestamp' value='${timestamp}'/>
    <property name='p2.compressed' value='false'/>
    <property name='p2.atomic.composite.loading' value='true'/>
  </properties>
  <children size='1'>
    <child location='${qualifier}'/>
  </children>
</repository>
"""

  def p2IndexContent = """version=1
metadata.repository.factory.order=compositeContent.xml,\\!
artifact.repository.factory.order=compositeArtifacts.xml,\\!
"""

  dir('target/composite-repo') {
    writeFile encoding: 'UTF-8', file: 'compositeArtifacts.xml', text: compositeArtifactsXmlContent
    writeFile encoding: 'UTF-8', file: 'compositeContent.xml', text: compositeContentXmlContent
    writeFile encoding: 'UTF-8', file: 'p2.index', text: p2IndexContent
  }
}
