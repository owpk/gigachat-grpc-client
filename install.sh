#!/bin/bash 

TARGET=$HOME/.local/bin/gigachat

mkdir -p $HOME/.local/bin

function version { echo "$@" | awk -F. '{ printf("%d%03d%03d%03d\n", $1,$2,$3,$4); }'; }

if [ "$1" == "-b" ]; then
   git pull
   echo "Building..."
   ./gradlew nativeBuild --info
   cp ./build/native/nativeCompile/gigachat $TARGET
else 
   VERSION=$( curl -I https://github.com/owpk/gigachat-grpc-client/releases/latest | awk -F '/' '/^location/ {print  substr($NF, 1, length($NF)-1)}' )
   LAST_VER=$( gigachat -v )

   echo "Latest version: $VERSION"
   echo "Current version: $LAST_VER"

   if [[ -z "$LAST_VER" || $(version $VERSION) -gt $(version $LAST_VER) ]]; then
       rm $TARGET 2> /dev/null
       echo "------"
       echo "Found newest version!: $VERSION"
       echo "------"
       echo "Downloading..."
       REDIRECT=$( curl -I "https://github.com/owpk/gigachat-grpc-client/releases/download/$VERSION/gigachat" \
           | awk '/^location/ {print $2}' | sed -e 's/\n//g' | sed -e 's/\r//g')
       curl $REDIRECT --output $TARGET
       chmod +x $TARGET
       exit 0
   fi
fi

