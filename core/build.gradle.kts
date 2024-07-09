plugins {
    `java-library`
}

sourceSets {
    main {
        java {
            srcDir("src/legacy/java")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven {
                name = "sk89q"
                url = uri("https://maven.enginehub.org/repo/")
            }
        }
        filter {
            includeGroup("com.sk89q")
            includeGroup("com.sk89q.lib")
        }
    }
}

dependencies {
    //Provided by minecraft
    implementation("com.google.guava:guava:17.0")
    implementation("com.google.code.gson:gson:2.2.4")
    compileOnly("com.google.code.findbugs:jsr305:1.3.9")

    //Custom deps
    api("org.yaml:snakeyaml:1.9")
    api("org.mozilla:rhino:1.7.15")
    api("de.schlichtherle:truezip:6.8.3")
    api("com.sk89q:jchronic:0.2.4a")
    api("com.sk89q.lib:jlibnoise:1.0.0")
    api("com.thoughtworks.paranamer:paranamer:2.6")


    //Lombok and testing
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    testImplementation("org.mockito:mockito-core:1.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks.named<Test>("test") {
    useJUnitPlatform()

    testLogging {
        events("passed")
    }
}
