require ./thunder-tools.inc

SRC_URI += " \ 
    file://0001-Change-MODULE-PATH.patch \
    file://0005-Autostart-startmode-deactivated.patch \
"

# Overrule R5.3.0 because we need upsteam fixes to properly
# generate entservices-apis
#
# master of 2 sep 2026
SRCREV = "4d150e28894cca1853c41eee8ca69d81e327820f"
