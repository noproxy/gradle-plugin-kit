#!/usr/bin/env fish

function fail
  echo $argv[1]
  exit -1
end

function step
  echo $argv[1]
end


set GRADLE_SCRIPT_HOME 'https://github.com/noproxy/gradle-plugin-kit/raw/master/subprojects/init-scripts/src/main/groovy'
set SCRIPTS checkstyle docs spotbugs
set dest_dir $HOME/.gradle/scripts

step 'Download Gradle Scripts'
mkdir -p $dest_dir
for name in $SCRIPTS
  curl -so $dest_dir/$name.gradle -L $GRADLE_SCRIPT_HOME/$name.gradle; or fail "download fail: $GRADLE_SCRIPT_HOME/$name.gradle"
end

step 'Create Command Shortcut'
printf "\nalias checkstyle=\"./gradlew  --init-script ~/.gradle/checkstyle.gradle checkstyleMain --quiet\"\n" >> ~/.bashrc
source ~/.bashrc

step 'Create Update Script'

