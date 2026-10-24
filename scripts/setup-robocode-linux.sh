#!/usr/bin/env bash
set -Eeuo pipefail

# Robocode bootstrap for Ubuntu/Debian-based Linux environments.
# Uses user-owned directories and never invokes apt, sudo, or system package managers.

REPO_URL="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS.git"
WORK_DIR="${WORK_DIR:-$HOME/robocode-workspace}"
PROJECT_DIR="$WORK_DIR/ROBOCODE-2026-IFSC-ADS"
ROBOT_ROOT="${ROBOT_ROOT:-$HOME/robocode/robots}"
ROBOT_PACKAGE_DIR="$ROBOT_ROOT/TriStateRobots"
INSTALLER_DIR="$WORK_DIR/installer"
INSTALLER_JAR="$INSTALLER_DIR/robocode-1.11.1-setup.jar"
INSTALLER_URL="https://downloads.sourceforge.net/project/robocode/robocode/1.11.1/robocode-1.11.1-setup.jar"

say() { printf '\n\033[1;36m[Robocode setup]\033[0m %s\n' "$*"; }
warn() { printf '\n\033[1;33m[Notice]\033[0m %s\n' "$*"; }
die() { printf '\n\033[1;31m[Error]\033[0m %s\n' "$*" >&2; exit 1; }

for tool in git curl java; do
  command -v "$tool" >/dev/null 2>&1 || die "Required command '$tool' is unavailable. Ask the lab administrator to provide Git, curl, and Java; this script does not use apt or sudo."
done

JAVA_VERSION="$(java -version 2>&1 | head -n 1)"
say "Using existing Java installation: $JAVA_VERSION"

mkdir -p "$WORK_DIR" "$INSTALLER_DIR" "$ROBOT_PACKAGE_DIR"

say "Cloning or updating the project repository..."
if [[ -d "$PROJECT_DIR/.git" ]]; then
  git -C "$PROJECT_DIR" fetch --all --prune
  git -C "$PROJECT_DIR" checkout main
  git -C "$PROJECT_DIR" pull --ff-only origin main
else
  git clone "$REPO_URL" "$PROJECT_DIR"
fi

say "Fetching version branches and installing each robot source..."
git -C "$PROJECT_DIR" fetch origin \
  '+refs/heads/madara-v1:refs/remotes/origin/madara-v1' \
  '+refs/heads/madara-v2:refs/remotes/origin/madara-v2' \
  '+refs/heads/madara-v3:refs/remotes/origin/madara-v3' \
  '+refs/heads/madara-v3.1:refs/remotes/origin/madara-v3.1' \
  '+refs/heads/stationary-tracker:refs/remotes/origin/stationary-tracker'

copy_from_branch() {
  local branch="$1" file="$2" destination="$3"
  if git -C "$PROJECT_DIR" cat-file -e "refs/remotes/origin/$branch:$file" 2>/dev/null; then
    git -C "$PROJECT_DIR" show "refs/remotes/origin/$branch:$file" > "$destination"
    say "Installed $file from $branch"
  else
    warn "Could not find $file in branch $branch; skipping it."
  fi
}

copy_from_branch "stationary-tracker" "StationaryTracker.java" "$ROBOT_PACKAGE_DIR/StationaryTracker.java"
copy_from_branch "madara-v1" "MadaraV1.java" "$ROBOT_PACKAGE_DIR/MadaraV1.java"
copy_from_branch "madara-v2" "MadaraV2.java" "$ROBOT_PACKAGE_DIR/MadaraV2.java"
copy_from_branch "madara-v3" "MadaraV3.java" "$ROBOT_PACKAGE_DIR/MadaraV3.java"
copy_from_branch "madara-v3" "MadaraV3.properties" "$ROBOT_PACKAGE_DIR/MadaraV3.properties"
copy_from_branch "madara-v3.1" "MadaraV3_1.java" "$ROBOT_PACKAGE_DIR/MadaraV3_1.java"
copy_from_branch "madara-v3.1" "MadaraV3_1.properties" "$ROBOT_PACKAGE_DIR/MadaraV3_1.properties"

# Keep V3 and V3.1 learning state independent. Seed V3.1 from V3 only when missing.
if git -C "$PROJECT_DIR" cat-file -e "refs/remotes/origin/madara-v3:MadaraV3.data/madara-v3-learning.dat" 2>/dev/null; then
  V3_LEARNING_FILE="$ROBOT_PACKAGE_DIR/MadaraV3.data/madara-v3-learning.dat"
  V31_LEARNING_FILE="$ROBOT_PACKAGE_DIR/MadaraV3_1.data/madara-v3-learning.dat"

  if [[ ! -s "$V3_LEARNING_FILE" ]]; then
    mkdir -p "$(dirname "$V3_LEARNING_FILE")"
    git -C "$PROJECT_DIR" show "refs/remotes/origin/madara-v3:MadaraV3.data/madara-v3-learning.dat" > "$V3_LEARNING_FILE"
    say "Seeded Madara V3 learning data"
  else
    say "Keeping existing Madara V3 learning data"
  fi

  if [[ ! -s "$V31_LEARNING_FILE" ]]; then
    mkdir -p "$(dirname "$V31_LEARNING_FILE")"
    git -C "$PROJECT_DIR" show "refs/remotes/origin/madara-v3:MadaraV3.data/madara-v3-learning.dat" > "$V31_LEARNING_FILE"
    say "Seeded Madara V3.1 learning data from the V3 baseline"
  else
    say "Keeping existing Madara V3.1 learning data"
  fi
fi

ROBocode_HOME="${ROBOCODE_HOME:-$HOME/robocode}"
if [[ ! -x "$ROBocode_HOME/robocode.sh" && ! -f "$ROBocode_HOME/robocode.jar" ]]; then
  say "Downloading the official classic Robocode installer..."
  if [[ ! -s "$INSTALLER_JAR" ]]; then
    curl -fL --retry 3 "$INSTALLER_URL" -o "$INSTALLER_JAR" || die "Download failed. Open https://sourceforge.net/projects/robocode/files/robocode/ and download the latest *-setup.jar into $INSTALLER_DIR."
  fi
  warn "The official installer opens a graphical setup wizard. Choose this recommended path: $ROBocode_HOME"
  java -jar "$INSTALLER_JAR"
  if [[ ! -x "$ROBocode_HOME/robocode.sh" && ! -f "$ROBocode_HOME/robocode.jar" ]]; then
    printf '\nEnter the directory where Robocode was installed (default: %s): ' "$ROBocode_HOME"
    read -r selected_dir || true
    selected_dir="${selected_dir:-$ROBocode_HOME}"
    ROBocode_HOME="$selected_dir"
  fi
fi

[[ -x "$ROBocode_HOME/robocode.sh" || -f "$ROBocode_HOME/robocode.jar" ]] || die "Robocode was not found at '$ROBocode_HOME'. Set ROBOCODE_HOME to the correct installation directory and rerun."

# Install the standard command name: robocode.
mkdir -p "$HOME/.local/bin"
cat > "$HOME/.local/bin/robocode" <<LAUNCHER
#!/usr/bin/env bash
set -e
ROBocode_HOME="$ROBocode_HOME"
if [[ -x "\$ROBocode_HOME/robocode.sh" ]]; then
  cd "\$ROBocode_HOME"
  exec ./robocode.sh "\$@"
elif [[ -f "\$ROBocode_HOME/robocode.jar" ]]; then
  exec java -jar "\$ROBocode_HOME/robocode.jar" "\$@"
else
  echo "Robocode not found at: \$ROBocode_HOME" >&2
  exit 1
fi
LAUNCHER
chmod +x "$HOME/.local/bin/robocode"

# Preserve the old project-specific command for existing instructions.
cat > "$HOME/.local/bin/robocode-project" <<'COMPAT_LAUNCHER'
#!/usr/bin/env bash
exec "$HOME/.local/bin/robocode" "$@"
COMPAT_LAUNCHER
chmod +x "$HOME/.local/bin/robocode-project"

# Add Robocode to the user's desktop application menu.
mkdir -p "$HOME/.local/share/applications"
cat > "$HOME/.local/share/applications/robocode.desktop" <<DESKTOP
[Desktop Entry]
Version=1.0
Type=Application
Name=Robocode
Comment=Robocode robot battle simulator
Exec=$HOME/.local/bin/robocode
Terminal=false
Categories=Game;Development;
DESKTOP
chmod 644 "$HOME/.local/share/applications/robocode.desktop"

mkdir -p "$HOME/.config/robocode-project"
cat > "$HOME/.config/robocode-project/paths.env" <<PATHS
ROBOCODE_HOME="$ROBocode_HOME"
ROBOT_ROOT="$ROBOT_ROOT"
PROJECT_DIR="$PROJECT_DIR"
PATHS

say "Setup finished."
printf '\nProject checkout: %s\nRobot sources:    %s\nRobocode install: %s\n' "$PROJECT_DIR" "$ROBOT_PACKAGE_DIR" "$ROBocode_HOME"
printf '\nLaunch Robocode with:\n  robocode\n  %s/.local/bin/robocode\n' "$HOME"
printf 'Application menu entry: Robocode\n'
printf '\nIn Robocode, make sure the robot root directory is set to:\n  %s\n' "$ROBOT_ROOT"
printf 'If robots are not detected, add that directory in Preferences/Robot Paths and restart Robocode.\n'
printf '\nCompatibility: V3.1 uses the V3 learning-data format but stores its progress separately under MadaraV3_1.data. Existing V3.1 learning data is never overwritten.\n'
printf '\nImportant: this script cannot preserve files if the Linux environment wipes your entire home directory. Keep this script or its GitHub URL available and rerun it after a reset.\n'
