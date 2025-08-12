FILESEXTRAPATHS:prepend := "${THISDIR}/../../../meta-rdk-iot/recipes-matter/barton-matter-example/files:"

SRC_URI += " \
    file://barton.zap \
    file://zzz_generated.tar.gz \
"

MATTER_ZAP_FILE = "${WORKDIR}/barton.zap"

# Adding the zzz_generated tarball to the SRC_URI will unpack it into WORKDIR
MATTER_ZZZ_GENERATED = "${WORKDIR}/zzz_generated"

MATTER_CONF_DIR = "/var/lib/my-product"

do_install:append() {
    cp -r "${S}/third_party/jsoncpp/repo/include/"* "${D}${includedir}/matter/"
}
