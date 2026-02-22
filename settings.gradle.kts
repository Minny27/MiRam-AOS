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
    // gradle/libs.versions.toml 은 Gradle 8.x가 자동으로 libs 카탈로그로 등록하므로
    // 여기서 별도 선언 불필요
}

rootProject.name = "MiRam"
include(":app")
