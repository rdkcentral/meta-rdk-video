LARBOARD_SRC_URI = "${CMF_GITHUB_ROOT}/larboard"

LARBOARD_SRCREV_25 = "ac684073548b497ee6788c8fc22015498c6907ca"

# Required for unmerged PR commits
LARBOARD_SRC_URI:append = ";nobranch=1"
