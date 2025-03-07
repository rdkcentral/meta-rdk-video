DESCRIPTION = "Factory Test App per manufacturer App Bundle selection [build/] from FTA CI repo"
HOMEPAGE = ""

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${MANIFEST_PATH_META_RDK}/licenses/Apache-2.0;md5=3b83ef96387f14655fc854ddc3c6bd57"

DEPENDS = "wpeframework"

do_install() {
   install -d ${D}/var/www
   install -d ${D}/var/www/factoryui
}

FILES:${PN} += "/var/www/factoryui/*"
