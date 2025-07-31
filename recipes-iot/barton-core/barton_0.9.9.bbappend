FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Add libxml2 to DEPENDS
DEPENDS:append = "libxml2"

# Remove otbr-agent from DEPENDS
DEPENDS:remove = "otbr-agent"

# Add the patch to SRC_URI
SRC_URI += "file://dependency-config-update.patch"
SRC_URI += "file://add-so-version.patch"

EXTRA_OECMAKE += "-DCMAKE_PREFIX_PATH=${STAGING_DIR_TARGET}/usr"
