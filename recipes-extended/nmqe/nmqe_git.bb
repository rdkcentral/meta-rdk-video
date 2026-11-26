SUMMARY = "Native Media over QUIC player CLI (POC)"
DESCRIPTION = "Native MoQ transport, standalone CMAF demux and GStreamer playback"

# Internal POC metadata, not redistribution clearance. Replace CLOSED with the
# reviewed engine license before release; the standalone AAMP source is Apache-2.0.
NMQE_LICENSE ?= "CLOSED & Apache-2.0"
NMQE_LIC_FILES_CHKSUM ?= ""
LICENSE = "${NMQE_LICENSE}"
LIC_FILES_CHKSUM = "${NMQE_LIC_FILES_CHKSUM} \
    file://${WORKDIR}/aamp/LICENSE;md5=97dd37dbf35103376811825b038fc32b \
"

PV = "0.1.0"
PR = "r0"

NMQE_GIT_URI ?= "git://git@github.com/rdk-e/native-media-over-quic-engine.git"
NMQE_BRANCH ?= "main"
NMQE_SRCREV ?= "64f6553a9b4d039bffd459606dc2f4ccfc9c35ba"
NMQE_AAMP_BRANCH ?= "develop"
NMQE_AAMP_SRCREV ?= "c94d0ce6ac8c85dd9776e8e57cd4a97584187193"
# Optional locked Cargo vendor bundle containing nmqe/vendor and nmqe/.cargo/config.toml.
NMQE_CARGO_VENDOR_URI ?= ""
NMQE_CARGO_VENDOR_SHA256 ?= ""

SRC_URI = " \
    ${NMQE_GIT_URI};protocol=ssh;branch=${NMQE_BRANCH};name=nmqe;destsuffix=nmqe \
    git://github.com/rdkcentral/aamp.git;protocol=https;branch=${NMQE_AAMP_BRANCH};name=aamp;destsuffix=aamp \
"
SRC_URI += "${@('%s;name=nmqe-vendor' % d.getVar('NMQE_CARGO_VENDOR_URI')) if d.getVar('NMQE_CARGO_VENDOR_URI') else ''}"
SRC_URI[nmqe-vendor.sha256sum] = "${NMQE_CARGO_VENDOR_SHA256}"
SRCREV_nmqe = "${NMQE_SRCREV}"
SRCREV_aamp = "${NMQE_AAMP_SRCREV}"
SRCREV_FORMAT = "nmqe_aamp"

S = "${WORKDIR}/nmqe"
B = "${WORKDIR}/build"

inherit cargo cmake pkgconfig

DEPENDS += "cjson gstreamer1.0 gstreamer1.0-plugins-base"
CARGO_SRC_DIR = "src/transport/moq_shim"
NMQE_RUST_TARGET ?= "${HOST_SYS}"
export RUSTFLAGS
export CARGO_NET_OFFLINE = "true"
CARGO_DISABLE_BITBAKE_VENDORING = "1"

EXTRA_OECMAKE += " \
    -DNMQE_BUILD_TESTS=OFF \
    -DNMQE_BUILD_SIM=OFF \
    -DNMQE_AAMP_DIR=${WORKDIR}/aamp \
    -DNMQE_RUST_TARGET=${NMQE_RUST_TARGET} \
    -DCARGO=${STAGING_BINDIR_NATIVE}/cargo \
"

RDEPENDS:${PN} += " \
    gstreamer1.0-plugins-base-app \
    gstreamer1.0-plugins-base-audioconvert \
    gstreamer1.0-plugins-base-audioresample \
    gstreamer1.0-plugins-base-videoconvert \
    gstreamer1.0-plugins-good-audioparsers \
    gstreamer1.0-plugins-good-autodetect \
    gstreamer1.0-plugins-bad-videoparsersbad \
    westeros-sink \
"

python () {
    if d.getVar("NMQE_CARGO_VENDOR_URI") and not d.getVar("NMQE_CARGO_VENDOR_SHA256"):
        bb.fatal("nmqe POC: NMQE_CARGO_VENDOR_URI requires NMQE_CARGO_VENDOR_SHA256")
}

do_configure:prepend() {
    cargo_common_do_configure
}

do_install() {
    install -d "${D}${bindir}" "${D}${sysconfdir}/nmqe/platform"
    install -m 0755 "${B}/nmqe-player" "${D}${bindir}/nmqe-player"
    install -m 0644 "${S}/config/platform/rdk-generic.json" \
        "${D}${sysconfdir}/nmqe/platform/rdk-generic.json"
}

FILES:${PN} += "${sysconfdir}/nmqe"
CONFFILES:${PN} += "${sysconfdir}/nmqe/platform/rdk-generic.json"
