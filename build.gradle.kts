// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE file.
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
  java
  `java-library`
  `maven-publish`
  signing
}

allprojects {
  group = "org.aya-prover"
  version = "0.19.0"
}

@Suppress("unsupported")
subprojects {
  apply {
    plugin("java")
    plugin("maven-publish")
    plugin("java-library")
    plugin("signing")
  }

  java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain {
      languageVersion.set(JavaLanguageVersion.of(11))
    }
  }

  sourceSets.test.configure {
    java { this.srcDirs.clear() }
  }

  tasks.withType<JavaCompile>().configureEach {
    modularity.inferModulePath.set(true)

    options.apply {
      encoding = "UTF-8"
      isDeprecation = true
      release.set(11)
      compilerArgs.addAll(listOf("-Xlint:unchecked", "--enable-preview"))
    }
  }

  tasks.withType<Javadoc>().configureEach {
    val options = options as StandardJavadocDocletOptions
    options.modulePath = tasks.compileJava.get().classpath.toList()
    options.addBooleanOption("-enable-preview", true)
    options.addStringOption("source", "11")
    options.addStringOption("Xdoclint:none", "-quiet")
    options.encoding("UTF-8")
    options.tags(
      "apiNote:a:API Note:",
      "implSpec:a:Implementation Requirements:",
      "implNote:a:Implementation Note:",
    )
  }

  artifacts {
    add("archives", tasks["sourcesJar"])
    add("archives", tasks["javadocJar"])
  }
  tasks.withType<Test>().configureEach {
    jvmArgs = listOf("--enable-preview")
    useJUnitPlatform()
    enableAssertions = true
    reports.junitXml.mergeReruns.set(true)
  }

  tasks.withType<JavaExec>().configureEach {
    jvmArgs = listOf("--enable-preview")
    enableAssertions = true
  }

  if (hasProperty("ossrhUsername")) publishing.repositories {
    maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2") {
      name = "MavenCentral"
      credentials {
        username = property("ossrhUsername").toString()
        password = property("ossrhPassword").toString()
      }
    }
  }

  val proj = this@subprojects
  publishing.publications {
    create<MavenPublication>("maven") {
      val githubUrl = "https://github.com/aya-prover/commonmark-java"
      groupId = proj.group.toString()
      version = proj.version.toString()
      artifactId = proj.name
      from(components["java"])
      pom {
        description.set("Commonmark-java with JPMS support")
        name.set(proj.name)
        url.set("https://www.aya-prover.org")
        licenses {
          license {
            name.set("BSD 2-Clause License")
            url.set("$githubUrl/blob/master/LICENSE")
          }
        }
        developers {
          developer {
            id.set("rstocker")
            name.set("Robin Stocker")
            email.set("rstocker@atlassian.com")
          }
        }
        scm {
          connection.set("scm:git:$githubUrl")
          url.set(githubUrl)
        }
      }
    }
  }

  if (hasProperty("signing.keyId")) signing {
    sign(publishing.publications["maven"])
  }
}

val mergeJacocoReports = tasks.register<JacocoReport>("mergeJacocoReports") {
  group = "verification"
  subprojects.forEach { subproject ->
    subproject.plugins.withType<JacocoPlugin>().configureEach {
      subproject.tasks.matching { it.extensions.findByType<JacocoTaskExtension>() != null }.configureEach {
        sourceSets(subproject.sourceSets.main.get())
        executionData(this)
      }

      subproject.tasks.matching { it.extensions.findByType<JacocoTaskExtension>() != null }.forEach {
        dependsOn(it)
      }
    }
  }

  reports { configureReports(true) }
  doLast {
    if (Os.isFamily(Os.FAMILY_WINDOWS) && System.getenv("CI") != "true") exec {
      commandLine("explorer.exe", ".\\build\\reports\\jacoco\\mergeJacocoReports\\html\\index.html")
    }
  }
}

tasks.register("githubActions") {
  group = "verification"
  dependsOn(mergeJacocoReports, tasks.findByPath(":lsp:jlink"))
}

tasks.withType<Sync>().configureEach {
  dependsOn(tasks.findByPath(":buildSrc:copyModuleInfo"))
}

fun JacocoReportsContainer.configureReports(merger: Boolean) {
  xml.required.set(true)
  csv.required.set(false)
  html.required.set(merger)
}
