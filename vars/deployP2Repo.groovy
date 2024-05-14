

/**
 * deploy a p2 repo (unzipped) to a server with ssh
 */
Map call(config = [:]) {
  def srcDir = config.srcDir
  if (!srcDir) {
    srcDir = config.sourceFolder
    if (!srcDir) {
      fail('sourceFolder')
    }
  }
  def user = config.user
  if (!user) {
    user = 'ubuntu'
  }
  def host = config.host
  if (!host) {
    host = 'p2.ivyteam.io'
  }
  def targetFolder = config.targetFolder
  Boolean updateCompositeRepo = config.updateCompositeRepo
  if (!updateCompositeRepo) {
    updateCompositeRepo = false
  }
  def p2RootPath = config.p2RootPath
  if (!p2RootPath) {
    p2RootPath = 'p2'
  }
  // simplify later, we always separate repoPath and qualifier
  def name = config.name
  if (!name) {
    if (updateCompositeRepo) {
      throw new Exception("name must be set if updateCompositeRepo='true'.")
    }
  }
  def version = config.version
  if (!version) {
    if (updateCompositeRepo) {
      throw new Exception("version must be set if updateCompositeRepo='true'.")
    }
  }
  def qualifier = config.qualifier
  if (!qualifier) {
    qualifier = new Date().format('yyyyMMdd.HHmmss')
  }
  if (!targetFolder) {
    if (updateCompositeRepo) {
      targetFolder = "${p2RootPath}/${name}/${version}/${qualifier}"
    } else {
      fail('targetFolder')
    }
  }

  if (updateCompositeRepo) {
    generateCompositeRepo("${name}-${version}", qualifier)
  }

  def sshHost = user + '@' + host
  docker.image('axonivy/build-container:ssh-client-1').inside {
    sshagent(['zugprojenkins-ssh']) {
      echo "Upload p2 repo from ${sourceFolder} to ${sshHost}:${targetFolder}"
      sh "ssh ${sshHost} mkdir -p ${targetFolder}"
      sh "rsync -r ${sourceFolder} ${sshHost}:${targetFolder}"
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

  def url = "${host}/${repoName}/${version}/${qualifier}"
  def compositeUrl = updateCompositeRepo ? "${host}/${repoName}/${version}/" : ''
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
