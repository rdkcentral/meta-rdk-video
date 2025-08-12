FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Add libxml2 to DEPENDS
DEPENDS:append = "libxml2 jsoncpp"

# Remove otbr-agent from DEPENDS
DEPENDS:remove = "otbr-agent"

# Add the patch to SRC_URI
SRC_URI += "file://add-so-version.patch"

EXTRA_OECMAKE += "\
    -DBCORE_ZIGBEE=OFF \
    -DBCORE_THREAD=OFF \
"
