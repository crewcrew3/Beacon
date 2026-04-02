enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Beacon"
include(":app")
include(":core:domain")
include(":core:ui")
include(":core:utils")
include(":data:impl")
include(":navigation:api")
include(":navigation:impl")
include(":feature:map:api")
include(":feature:map:impl")
include(":feature:login:api")
include(":feature:login:impl")
include(":feature:signup:api")
include(":feature:signup:impl")
include(":feature:profile:api")
include(":feature:profile:impl")
