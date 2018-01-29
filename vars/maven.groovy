def call(Map config) {
	configFileProvider([configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
		sh "mvn --batch-mode -s $MAVEN_SETTINGS ${config.cmd}"
	}
}
