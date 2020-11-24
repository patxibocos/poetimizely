val ktlint by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:0.39.0")
}

tasks.register<JavaExec>("ktlint") {
    ktlintSetup(group = "verification", description = "Check Kotlin code style.")
    args("**/src/**/*.kt", "**/src/**/*.kts", "--relative")
}

tasks.register<JavaExec>("ktlintFormat") {
    ktlintSetup(group = "formatting", description = "Fix Kotlin code style deviations.")
    args("-F", "*.kt", "*.kts", "--relative")
}

fun JavaExec.ktlintSetup(group: String, description: String) {
    this.group = group
    this.description = description
    this.classpath = ktlint
    this.main = "com.pinterest.ktlint.Main"
}
