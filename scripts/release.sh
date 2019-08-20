#!/usr/bin/env bash
: ${1?"Version missing - usage: $0 x.y.z"}

#update build.gradle
sed -i '.bak' "s/version = '.*-SNAPSHOT/version = '$1-SNAPSHOT/g" build.gradle

#update README.md
sed -i '.bak' "s/'com.mparticle:android-media:.*'/'com.mparticle:android-media:$1'/g" README.md

#commit the version bump, tag, and push to private and public
git add build.gradle
git add README.md
git commit -m "Update version to $1"
git tag "v$1"
git push origin "v$1"
git remote add public git@github.com:mParticle/mparticle-android-media.git
git push public "v$1"
git push origin HEAD:development
git push origin HEAD:master
git push public HEAD:master