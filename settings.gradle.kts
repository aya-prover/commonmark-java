// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE file.

rootProject.name = "commonmark-parent"

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage") repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
  }
}

include(
  "commonmark",
  "commonmark-ext-autolink",
  "commonmark-ext-gfm-strikethrough",
  "commonmark-ext-gfm-tables",
  "commonmark-ext-heading-anchor",
  "commonmark-ext-image-attributes",
  "commonmark-ext-ins",
  "commonmark-ext-task-list-items",
  "commonmark-ext-yaml-front-matter",
  "commonmark-integration-test",
  "commonmark-test-util",
)
