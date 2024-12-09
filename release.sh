REPO="gigachat-grpc-client"
OWNER="owpk"
TOKEN=$GITHUB_TOKEN
TAG="$1"
DESC="$2"

JSON_STRING=$( jq -n \
                  --arg tag "$TAG" \
                  --arg desc "$DESC" \
                  '{tag_name:$tag, target_commitish:"main", name:"gigachat-grpc-client-$tag", body:$desc, draft:false, prerelease:false, generate_release_notes:true}')

curl -L \
  -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "https://api.github.com/repos/$OWNER/$REPO/releases" \
  -d $JSON_STRING
