require cog.inc
require cog-meson.inc

SRC_URI:append = " file://0001-lower-webkit-version-requirement-for-meson.patch"
SRC_URI:append = " file://0002-enable-firebolt.patch"
SRC_URI[sha256sum] = "37c5f14123b8dcf077839f6c60f0d721d2a91bb37829e796f420126e6b0d38b5"

#PACKAGECONFIG ?= "${@bb.utils.contains('DISTRO_FEATURES', 'enable_libsoup3', '', '', d)}"
PACKAGECONFIG ?= "${@bb.utils.contains('DISTRO_FEATURES', 'enable_libsoup3', '', 'soup2', d)}"
