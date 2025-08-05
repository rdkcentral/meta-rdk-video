SUMMARY = "Cog is a small launcher designed for the WebKit WPE port. \
           But it also allows to use the WebKit GTK port instead. \
           Cog is small: provides no user interface, and is suitable \
           to be used as a Web application container. Cog may be \
           presented fullscreen depending on the WPE backend being used. \
           "
HOMEPAGE = "https://github.com/Igalia/cog"
BUGTRACKER = "https://github.com/Igalia/cog/issues"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING;md5=bf1229cd7425b302d60cdb641b0ce5fb"

FILESEXTRAPATHS:append := "${THISDIR}/files:"

SRC_URI = "https://wpewebkit.org/releases/cog-${PV}.tar.xz"

DEPENDS = " systemd libwpe glib-2.0 wpe-webkit"
