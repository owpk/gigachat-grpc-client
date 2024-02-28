#!/bin/bash 

git pull

TARGET=~/.local/bin/gigachat

VERSION=$( curl -I https://github.com/owpk/gigachat-grpc-client/releases/latest | awk -F '/' '/^location/ {print  substr($NF, 1, length($NF)-1)}' )

function version { echo "$@" | awk -F. '{ printf("%d%03d%03d%03d\n", $1,$2,$3,$4); }'; }

LAST_VER=$( gigachat -v 2> /dev/null )

if [[ -z "$LAST_VER" || $(version $VERSION -ge version $LAST_VER) ]]; then
    rm $TARGET 2> /dev/null
    echo "------"
    echo "Found newest version!: $VERSION"
    echo "Downloading..."
    echo "------"
    REDIRECT=$( curl -I "https://github.com/owpk/gigachat-grpc-client/releases/download/$VERSION/gigachat" \
        | awk '/^location/ {print $2}' | sed -e 's/\n//g' | sed -e 's/\r//g')
    curl $REDIRECT --output $TARGET
    chmod +x $TARGET
    exit 0
fi

echo "Installed newest version: $LAST_VER. Exiting."
