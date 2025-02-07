void call(config = [:]) {
  def projectName = config.projectName
  if (!projectName) {
    fail('projectName')
  }

  def projectVersion = config.projectVersion
  if (!projectVersion) {
    fail('projectVersion')
  }

  def bomFile = config.bomFile
  if (!bomFile) {
    fail('bomFile')
  }

  withCredentials([string(credentialsId: 'dependency-track', variable: 'API_KEY')]) {
    sh 'curl -v --fail -X POST https://api.dependency-track.ivyteam.io/api/v1/bom \
        -H "Content-Type: multipart/form-data" \
        -H "X-API-Key: ' + API_KEY + '" \
        -F "autoCreate=true" \
        -F "projectName=' + projectName + '" \
        -F "projectVersion=' + projectVersion + '" \
        -F "bom=@' + bomFile + '"'
  }  
}

def fail(String paramName) {
  echo "ERROR: no ${paramName} provided."
  throw new Exception("no ${paramName} provided.")
}
