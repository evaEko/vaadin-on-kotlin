dependencies {
    compile(project(":vok-framework"))
    compile(project(":vok-security"))

    compile("com.github.mvysny.vokdataloader:vok-dataloader:${ext["vok_dataloader_version"]}")

    // Vaadin
    compile("com.github.mvysny.karibudsl:karibu-dsl-v8:${ext["karibudsl_version"]}")
    compile("com.vaadin:vaadin-server:${ext["vaadin8_version"]}")
    compile("javax.servlet:javax.servlet-api:3.1.0")

    // IDEA language injections
    compile("com.intellij:annotations:12.0")

    // testing
    testCompile("com.github.mvysny.dynatest:dynatest-engine:${ext["dynatest_version"]}")
    testCompile("com.github.mvysny.kaributesting:karibu-testing-v8:${ext["kaributesting_version"]}")
    testCompile("ch.qos.logback:logback-classic:${ext["logback_version"]}")
}

val configureBintray = ext["configureBintray"] as (artifactId: String, description: String) -> Unit
configureBintray("vok-util-vaadin8", "VOK: Basic utility classes for Vaadin 8")

