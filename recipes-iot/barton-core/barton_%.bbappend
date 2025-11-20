FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Skip unless the distro advertises "ENABLE_MATTER_BARTON"
inherit features_check
REQUIRED_DISTRO_FEATURES = "ENABLE_MATTER_BARTON"

# Add libxml2 to DEPENDS
DEPENDS:append = "libxml2 jsoncpp linenoise"

# Remove otbr-agent from DEPENDS
DEPENDS:remove = "otbr-agent"

# Add the patch to SRC_URI
SRC_URI += "file://add-so-version.patch"
SRC_URI += "file://0001-included-path-for-providers.patch"
SRC_URI += "file://0001-puts-app-in-rootfs.patch"
SRC_URI += "file://0001-change-default-dir.patch"
SRC_URI += "file://0001-changing-storage-to-opt.patch"

#Enable the reference application
EXTRA_OECMAKE:append = "${@' -DBCORE_BUILD_REFERENCE=ON' if 'BARTON_REFERENCE_APP' in d.getVar('DISTRO_FEATURES').split() else ' -DBCORE_BUILD_REFERENCE=OFF'}"
EXTRA_OECMAKE:append = " -DBCORE_MATTER_DELEGATE_IMPLEMENTATIONS=${S}/core/src/subsystems/matter/delegates/dev/SelfSignedCertifierOperationalCredentialsIssuer.cpp"

#Install the ref application in rootfs
IMAGE_INSTALL:append = " barton-bin"
FILES:${PN} += "${bindir}/barton-core-reference"

EXTRA_OECMAKE += "\
    -DBCORE_ZIGBEE=OFF \
    -DBCORE_THREAD=OFF \
"
