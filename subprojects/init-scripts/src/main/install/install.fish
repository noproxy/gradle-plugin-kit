#!/usr/bin/env fish

function fail
  set_color red
  echo $argv
  set_color normal
  exit 1
end

function step
  set_color -o green
  echo -n $argv'...'
  set_color normal
  echo
end

set REMOTE_SCRIPT_HOME 'https://github.com/noproxy/gradle-plugin-kit/raw/master/subprojects/init-scripts/src/main/groovy'
set INSTALL_SCRIPT_NAME 'install.sh'
set GRADLE_SCRIPT_NAMES checkstyle docs spotbugs
set GRADLE_SCRIPT_DEST_DIR $HOME/.gradle/scripts
set ALIAS_SCRIPT $HOME/.ci_alias

step 'Download Gradle Scripts'
mkdir -p $GRADLE_SCRIPT_DEST_DIR
for name in $GRADLE_SCRIPT_NAMES
  curl -so $GRADLE_SCRIPT_DEST_DIR/$name.gradle -L $REMOTE_SCRIPT_HOME/$name.gradle; or fail "download fail: $REMOTE_SCRIPT_HOME/$name.gradle"
end

step 'Create Command Shortcut'

#if not [ -f $ALIAS_SCRIPT ]; then
#    touch $ALIAS_SCRIPT; or fail "fail creating file $ALIAS_SCRIPT"
#fi
if not [ -f $ALIAS_SCRIPT ]
    touch $ALIAS_SCRIPT; or fail "fail creating file $ALIAS_SCRIPT"
end
chmod u+x $ALIAS_SCRIPT

echo '#!/usr/bin/env sh
alias checkstyle="./gradlew  --init-script ~/.gradle/scripts/checkstyle.gradle"
alias spotbug="./gradlew  --init-script ~/.gradle/scripts/spotbugs.gradle"
alias doc="./gradlew  --init-script ~/.gradle/scripts/docs.gradle"

alias checkstyles="checkstyle checkstyleMain checkstyleTest --continue --quiet"
alias spotbugs="spotbug spotbugsMain spotbugsTest --continue --quiet"
alias docs="doc :asciidoctor --continue --quiet"

alias update_ci_utils="curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install | sh"

' > $ALIAS_SCRIPT

if not grep "source .ci_alias" ~/.bashrc
  printf "\nsource .ci_alias\n" >> ~/.bashrc; or fail "fail updating .bashrc"
end

if not grep "source .ci_alias" ~/.bash_profile
  printf "\nsource .ci_alias\n" >> ~/.bash_profile; or fail "fail updating .bash_profile"
end

source ~/.bashrc

function cmd
  set_color -o green
  printf "%-30s" $argv[1]
  set_color normal
  echo $argv[2]
end

echo
set_color -o
echo 'Congratulations! Everything is done.'
echo
set_color normal
echo -n 'You can now use these command '
set_color -o
echo 'in your project directory:'
set_color normal
echo

cmd 'checkstyles' 'generate checkstyle reports'
cmd 'spotbugs' 'generate spotbug reports'
cmd 'docs' 'generate documents'
echo
cmd 'checkstyle initCheckstyle' 'create sample checkstyle configuration in your project'
cmd 'docs :initDocs' 'create a set template documents in your project'
echo

set_color red
echo -n 'You can use '
set_color -o green
echo -n 'update_ci_utils'
set_color normal
set_color red
echo ' to update all scripts at any time!'

