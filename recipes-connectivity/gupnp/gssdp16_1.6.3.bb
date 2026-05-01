SUMMARY = "Resource discovery and announcement over SSDP"
DESCRIPTION = "GSSDP implements resource discovery and announcement over SSDP \
               (Simpe Service Discovery Protocol)."
HOMEPAGE = "https://gitlab.gnome.org/GNOME/gssdp/"
BUGTRACKER = "https://gitlab.gnome.org/GNOME/gssdp/-/issues"
LICENSE = "LGPL-2.1-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=4fbd65380cdd255951079008b364516c"

S = "${WORKDIR}/gssdp-1.6.3"

SRC_URI = "https://download.gnome.org/sources/gssdp/1.6/gssdp-1.6.3.tar.xz"
SRC_URI[sha256sum] = "2fedb5afdb22cf14d5498a39a773ca89788a250fcf70118783df821e1f3f3446"

GTKDOC_MESON_OPTION = 'gtk_doc'

DEPENDS = " \
    glib-2.0 \
    libsoup-3.0 \
"

inherit pkgconfig gobject-introspection vala gi-docgen features_check meson

# manpages require pandoc-native
EXTRA_OEMESON += "-Dmanpages=false"

SNIFFER = "${@bb.utils.contains("BBFILE_COLLECTIONS", "gnome-layer", "sniffer", "", d)}"

PACKAGECONFIG ??= "${@bb.utils.contains('DISTRO_FEATURES', 'opengl', "${SNIFFER}", "", d)}"
PACKAGECONFIG[sniffer] = "-Dsniffer=true,-Dsniffer=false,gtk4,"

REQUIRED_DISTRO_FEATURES = "${@bb.utils.contains('PACKAGECONFIG', 'sniffer', 'opengl', '', d)}"

PROVIDES += "libgssdp-1.6-0"

PACKAGES += "${PN}-tools"

FILES:${PN}-tools = "${bindir}/gssdp-device-sniffer ${datadir}/gssdp-1.6/*.glade"

FILES:${PN} += "${libdir}/libgssdp-1.6.so.*"
FILES:${PN} += "${libdir}/girepository-1.0/GSSDP-1.6.typelib"

FILES:${PN}-dev += "${includedir}/gssdp-1.6 \
                    ${libdir}/libgssdp-1.6${SOLIBSDEV} \
                    ${libdir}/pkgconfig/gssdp-1.6.pc \
                    ${datadir}/gir-1.0/GSSDP-1.6.gir \
                    ${datadir}/vala/vapi/gssdp-1.6.*"

PKG:${PN} = "libgssdp-1.6-0"
