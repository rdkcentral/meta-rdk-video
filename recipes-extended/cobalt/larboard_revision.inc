LARBOARD_SRC_URI = "${CMF_GITHUB_ROOT}/larboard"

LARBOARD_SRCREV_25 = "8d006792d5f86e5d635b8eb2fa9ef16c19673de2"

# Required for unmerged PR commits
LARBOARD_SRC_URI:append = ";nobranch=1"
