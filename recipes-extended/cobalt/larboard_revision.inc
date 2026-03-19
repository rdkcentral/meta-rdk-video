# Temporary workaround: allow fetching by SRCREV even when commit is not in configured branch.
LARBOARD_SRC_URI = "${CMF_GITHUB_ROOT}/larboard;nobranch=1"

# 24.lts.stable. March 13, 2025
LARBOARD_SRCREV_24 ?= "10f8d522b8a26727bc03dfe1e3f619bad91df5c3"

# develop. Feb 19, 2026
LARBOARD_SRCREV_DEV ?= "21b77d03e1ac9c2107db6b5713833cf102c15283"
