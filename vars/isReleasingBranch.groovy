Boolean call(config = [:]) {
  def branchName = config.branchName
  branchName = branchName ?: env.BRANCH_NAME
  return branchName == 'master' || branchName.startsWith('release/')
}
