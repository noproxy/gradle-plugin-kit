#!/usr/bin/env bash

red='\033[0;31m'
bold_black='\033[1;30m'
bold_green='\033[1;32m'

normal='\033[0m'

fail() {
  printf "$red%s$normal\n" "$*"
  exit 1
}

step() {
  printf "$bold_green%s...$normal\n" "$*"
}

REMOTE_SCRIPT_HOME='https://github.com/noproxy/gradle-plugin-kit/raw/master/subprojects/init-scripts/src/main/groovy'
GRADLE_SCRIPT_NAMES=(checkstyle docs spotbugs)
GRADLE_SCRIPT_DEST_DIR=$HOME/.gradle/scripts
ALIAS_SCRIPT=$HOME/.ci_alias

step 'Download Gradle Scripts'
mkdir -p "$GRADLE_SCRIPT_DEST_DIR"

for name in "${GRADLE_SCRIPT_NAMES[@]}"; do
  echo "  installing $name"
  curl -so "$GRADLE_SCRIPT_DEST_DIR/$name.gradle" -L "$REMOTE_SCRIPT_HOME/$name."gradle || fail "download fail: $REMOTE_SCRIPT_HOME/$name.gradle"
done

step 'Create Command Shortcut'

sleep 2

echo "  updating $ALIAS_SCRIPT"
echo '#!/usr/bin/env sh
alias checkstyle="./gradlew  --init-script ~/.gradle/scripts/checkstyle.gradle"
alias spotbug="./gradlew  --init-script ~/.gradle/scripts/spotbugs.gradle"
alias doc="./gradlew  --init-script ~/.gradle/scripts/docs.gradle"

alias checkstyles="checkstyle checkstyleMain checkstyleTest --continue --quiet"
alias spotbugs="spotbug spotbugsMain spotbugsTest --continue --quiet"
alias docs="doc :asciidoctor --continue --quiet"

alias update_ci_utils="curl -fsSL https://git.io/fj54t | sh"

' >"$ALIAS_SCRIPT"

# shellcheck disable=SC2016
INIT_CMD='test -e "$HOME/.ci_alias" && source "$HOME/.ci_alias"'

INIT_PROFILES=("$HOME"/.profile "$HOME"/.bash_profile "$HOME"/.zshrc)
for profile in "${INIT_PROFILES[@]}"; do
  if ! grep "$INIT_CMD" "$profile" >/dev/null 2>&1; then
    echo "  updating $profile"
    printf "\n%s\n" "$INIT_CMD" >>"$profile" || fail "fail updating $profile"
  else
    echo "  skip updating $profile"
  fi
done

# shellcheck source=/Users/yiyazhou/.ci_alias
source "$HOME"/.ci_alias

cmd() {
  printf "$bold_green%-30s$normal%s\n" "$1" "$2"
}

printf "$bold_green%s\n$normal" 'Congratulations! Everything is done.'
sleep 1

printf -- '---------------------------------------------------------------\n\n'

# shellcheck disable=SC2059
printf "You can now use these command ${red}in your project directory$normal:\n\n"

cmd 'checkstyles' 'generate checkstyle reports'
cmd 'spotbugs' 'generate spotbug reports'
cmd 'docs' 'generate documents'
echo
cmd 'checkstyle initCheckstyle' 'create sample checkstyle configuration in your project'
cmd 'doc :initDocs' 'create a set template documents in your project'
echo

# shellcheck disable=SC2059
printf "You can use ${bold_green}update_ci_utils${normal} to update all scripts at any time!\n"
