#!/usr/bin/env bash
# Load demo data into the debug build for screenshots.
# Requires the debug APK installed (dev.nucleus.scheduleit.debug) and adb on PATH.
# Usage: scripts/load-demo-data.sh [-s <device-serial>]

set -euo pipefail

PKG="dev.nucleus.scheduleit.debug"
SQL="$(cd "$(dirname "$0")/.." && pwd)/scripts/demo_data.sql"
ADB=(adb)
if [[ "${1:-}" == "-s" && -n "${2:-}" ]]; then
  ADB=(adb -s "$2")
fi

TMP_DB="$(mktemp -t scheduleit.XXXXXX.db)"
trap 'rm -f "$TMP_DB"' EXIT

echo "Stopping app…"
"${ADB[@]}" shell am force-stop "$PKG"

echo "Pulling database…"
"${ADB[@]}" shell run-as "$PKG" cat databases/scheduleit.db > "$TMP_DB"

echo "Applying demo data…"
sqlite3 "$TMP_DB" < "$SQL"

echo "Pushing back…"
"${ADB[@]}" push "$TMP_DB" /sdcard/scheduleit.db >/dev/null
"${ADB[@]}" shell "cat /sdcard/scheduleit.db | run-as $PKG sh -c 'cat > databases/scheduleit.db'"
"${ADB[@]}" shell "run-as $PKG rm -f databases/scheduleit.db-journal databases/scheduleit.db-wal databases/scheduleit.db-shm"
"${ADB[@]}" shell rm /sdcard/scheduleit.db

echo "Launching app…"
"${ADB[@]}" shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null

echo "Done."
