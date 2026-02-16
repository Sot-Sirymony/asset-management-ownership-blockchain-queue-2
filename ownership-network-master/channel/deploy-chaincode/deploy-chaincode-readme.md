# Find repo root by walking up until we find "channel" folder (or .git)
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
while [ ! -d "${ROOT_DIR}/channel" ] && [ ! -d "${ROOT_DIR}/.git" ] && [ "${ROOT_DIR}" != "/" ]; do
ROOT_DIR="$(dirname "${ROOT_DIR}")"
done

if [ "${ROOT_DIR}" = "/" ]; then
echo "ERROR: Could not detect repo root (no ./channel or .git found)"
exit 1
fi



