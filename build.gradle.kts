import com.falsepattern.fpgradle.dsl.* //Helper utilities for cleaner buildscript code
plugins {
    id("fpgradle-minecraft") version("0.3.3")
}

group = "com.falsepattern"

minecraft_fp {
    mod {
        modid   = "worldedit"
        name    = "WorldEdit"
        rootPkg = "com.sk89q.worldedit.forge"
    }

    tokens {
        tokenClass = "Tags"
    }

    publish {
        changelog = "https://github.com/FalsePattern/WorldEdit/blob/master/CHANGELOG.txt"
        maven {
            repoName = "mavenpattern"
            repoUrl = "https://mvn.falsepattern.com/releases/"
        }
    }
}

repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "cursemaven"
                url = uri("https://mvn.falsepattern.com/cursemaven/")
            }
        }
        filter {
            includeGroup("curse.maven")
        }
    }
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
    exclusiveContent {
        forRepository {
            maven {
                name = "mavenpattern"
                url = uri("https://mvn.falsepattern.com/releases/")
            }
        }
        filter {
            includeGroup("com.falsepattern")
        }
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "mega"
                url = uri("https://mvn.falsepattern.com/gtmega_releases/")
            }
        }
        filter {
            includeGroup("codechicken")
            includeGroup("mega")
        }
    }
}

sourceSets {
    main {
        java {
            srcDir("core/src/main/java")
            srcDir("core/src/legacy/java")
        }
        resources {
            srcDir("core/src/main/resources")
        }
    }
    test {
        java {
            srcDir("core/src/test/java")
        }
    }
}

dependencies {
    // Core deps
    implementation("org.yaml:snakeyaml:1.9")
    implementation("org.mozilla:rhino:1.7.15")
    implementation("de.schlichtherle:truezip:6.8.3")
    implementation("com.sk89q:jchronic:0.2.4a")
    implementation("com.sk89q.lib:jlibnoise:1.0.0")
    implementation("com.thoughtworks.paranamer:paranamer:2.6")

    // Forge deps
    implementationSplit("com.falsepattern:falsepatternlib-mc1.7.10:1.2.5")

    devOnlyNonPublishable("codechicken:forgemultipart-mc1.7.10:1.6.2-mega:dev")
    devOnlyNonPublishable(deobfCurse("architecturecraft-242001:2352554"))
    devOnlyNonPublishable("mega:carpentersblocks-mc1.7.10:3.4.1-mega:dev")

    runtimeOnlyNonPublishable("codechicken:notenoughitems-mc1.7.10:2.3.1-mega:dev")

    //Testing
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
