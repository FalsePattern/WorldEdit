import com.falsepattern.fpgradle.dsl.* //Helper utilities for cleaner buildscript code
plugins {
    id("fpgradle-minecraft") version("0.3.3")
}

group = "com.sk89q.worldedit"

minecraft_fp {
    mod {
        modid   = "worldedit"
        name    = "WorldEdit"
        rootPkg = "$group.forge"
    }

    tokens {
        tokenClass = "Tags"
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

dependencies {
    shadowImplementation(project(":core"))
    implementationSplit("com.falsepattern:falsepatternlib-mc1.7.10:1.2.5")

    devOnlyNonPublishable("codechicken:forgemultipart-mc1.7.10:1.6.2-mega:dev")
    devOnlyNonPublishable(deobfCurse("architecturecraft-242001:2352554"))
    devOnlyNonPublishable("mega:carpentersblocks-mc1.7.10:3.4.1-mega:dev")

    runtimeOnlyNonPublishable("codechicken:notenoughitems-mc1.7.10:2.3.1-mega:dev")
}

afterEvaluate {
    tasks.named<Jar>("sourcesJar").configure {
        from(project(":core").sourceSets["main"].allSource)
    }
}
