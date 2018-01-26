def call(Map config) {
	configFileProvider([configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
		sh "mvn -s $MAVEN_SETTINGS ${config.cmd}"
	}
}
