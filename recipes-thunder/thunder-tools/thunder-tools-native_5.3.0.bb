require ./thunder-tools.inc

SRC_URI += " \
    file://0001-Change-MODULE-PATH.patch \
    file://0003-Callsign-not-generated-Json-Generator.patch \
    file://0004-Add-support-for-project-dir.patch \
    file://0005-Autostart-startmode-deactivated.patch \
"

# Overrule R5.3.0 because we need upsteam fixes to properly
# generate entservices-apis 
SRCREV = "26471c50282bb07a6305a7db27ac69bd339a2337"
