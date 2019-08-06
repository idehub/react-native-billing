set -e
if [ "$LANE" = "android" ]; then
    node --version
    npm install -g yarn
fi