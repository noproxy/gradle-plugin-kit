#!/usr/bin/env zsh

red='\033[0;31m'
bold_black='\033[1;30m'
bold_green='\033[1;32m'

normal='\033[0m'

function fail() {
  printf "$red%s$normal\n" "$*"
  exit 1
}

function step() {
  printf "$bold_green%s...$normal\n" "$*"
}

REMOTE_SCRIPT_HOME='https://github.com/noproxy/gradle-plugin-kit/raw/master/subprojects/init-scripts/src/main/groovy'
INSTALL_SCRIPT_NAME='install.sh'
GRADLE_SCRIPT_NAMES=(checkstyle docs spotbugs)
GRADLE_SCRIPT_DEST_DIR=$HOME/.gradle/scripts
ALIAS_SCRIPT=$HOME/.ci_alias

step 'Download Gradle Scripts'
mkdir -p "$GRADLE_SCRIPT_DEST_DIR"

for name in "${GRADLE_SCRIPT_NAMES[@]}"; do
  curl -so "$GRADLE_SCRIPT_DEST_DIR/$name.gradle" -L "$REMOTE_SCRIPT_HOME/$name."gradle || fail "download fail: $REMOTE_SCRIPT_HOME/$name.gradle"
done

step 'Create Command Shortcut'

if [ ! -f "$ALIAS_SCRIPT" ]; then
  touch "$ALIAS_SCRIPT" || fail "fail creating file $ALIAS_SCRIPT"
fi
chmod u+x "$ALIAS_SCRIPT"

echo '#!/usr/bin/env sh
alias checkstyle="./gradlew  --init-script ~/.gradle/scripts/checkstyle.gradle"
alias spotbug="./gradlew  --init-script ~/.gradle/scripts/spotbugs.gradle"
alias doc="./gradlew  --init-script ~/.gradle/scripts/docs.gradle"

alias checkstyles="checkstyle checkstyleMain checkstyleTest --continue --quiet"
alias spotbugs="spotbug spotbugsMain spotbugsTest --continue --quiet"
alias docs="doc :asciidoctor --continue --quiet"

alias update_ci_utils="curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install | sh"

' >"$ALIAS_SCRIPT"

if (($(grep 'source .ci_alias' ~/.bashrc) != 1)); then
  printf "\nsource .ci_alias\n" >>~/.bashrc || fail "fail updating .bashrc"
fi

# shellcheck source=/Users/yiyazhou/.bashrc
source ~/.bashrc

function cmd() {
  printf "$bold_green%-30s$normal%s" "$1" "$2"
}

printf "\n$bold_black%s\n$normal" 'Congratulations! Everything is done.'
# shellcheck disable=SC2059
printf "You can now use these command ${bold_black}in your project directory$normal:\n\n"

cmd 'checkstyles' 'generate checkstyle reports'
cmd 'spotbugs' 'generate spotbug reports'
cmd 'docs' 'generate documents'
echo
cmd 'checkstyle initCheckstyle' 'create sample checkstyle configuration in your project'
cmd 'docs :initDocs' 'create a set template documents in your project'
echo

echo "${red}You can use ${bold_green}update_ci_utils${red} to update all scripts at any time!"
