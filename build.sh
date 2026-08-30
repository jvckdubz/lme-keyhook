#!/bin/sh
set -eu

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
[ -n "$SDK" ] || { echo "не задан ANDROID_HOME"; exit 1; }

BT=$(ls -1 "$SDK/build-tools" | sort -V | tail -1)
PL=$(ls -1 "$SDK/platforms" | sort -V | tail -1)
echo "build-tools: $BT | platform: $PL"

AAPT2="$SDK/build-tools/$BT/aapt2"
D8="$SDK/build-tools/$BT/d8"
ZIPALIGN="$SDK/build-tools/$BT/zipalign"
APKSIGNER="$SDK/build-tools/$BT/apksigner"
JAR="$SDK/platforms/$PL/android.jar"

for t in "$AAPT2" "$D8" "$ZIPALIGN" "$APKSIGNER" "$JAR"; do
  [ -e "$t" ] || { echo "нет инструмента: $t"; exit 1; }
done

rm -rf build out
mkdir -p build/classes out

echo "--- ресурсы"
"$AAPT2" compile --dir res -o build/res.zip
"$AAPT2" link -o build/app.unsigned.apk -I "$JAR" --manifest AndroidManifest.xml build/res.zip --min-sdk-version 21 --target-sdk-version 30

echo "--- код"
javac -nowarn -source 8 -target 8 -bootclasspath "$JAR" -d build/classes $(find java -name '*.java')
"$D8" --release --lib "$JAR" --output build $(find build/classes -name '*.class')

echo "--- упаковка"
(cd build && zip -q app.unsigned.apk classes.dex)
"$ZIPALIGN" -f 4 build/app.unsigned.apk build/app.aligned.apk

if [ -f key.jks ]; then
  cp key.jks build/key.jks
  echo "ключ: из репозитория"
else
  keytool -genkeypair -keystore build/key.jks -storepass android -keypass android -alias keyhook -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=lme-keyhook" >/dev/null 2>&1
  echo "ключ: создан одноразовый"
fi

"$APKSIGNER" sign --ks build/key.jks --ks-pass pass:android --key-pass pass:android --out out/keyhook.apk build/app.aligned.apk

echo "--- проверка"
"$APKSIGNER" verify --print-certs out/keyhook.apk | head -2
ls -l out/keyhook.apk
unzip -l out/keyhook.apk | grep -E "classes.dex|AndroidManifest.xml|res/xml" || { echo "в APK нет обязательных частей"; exit 1; }
