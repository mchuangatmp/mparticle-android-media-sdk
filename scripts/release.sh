#!/usr/bin/env bash
: ${1?"Version missing - usage: $0 x.y.z"}

#update build.gradle
sed -i '.bak' "s/version = '.*-SNAPSHOT/version = '$1-SNAPSHOT/g" build.gradle

#update README.md
sed -i '.bak' "s/'com.mparticle:android-media:.*'/'com.mparticle:android-media:$1'/g" README.md

#commit the version bump, tag, and push to private and public
git add build.gradle
git add README.md
