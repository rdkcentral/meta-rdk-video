#do_install_append_dunfell() {
#    install -d ${D}${includedir}/sanitizer/
#    install -m 0644 ${D}/${libdir}/gcc/${TARGET_SYS}/${BINV}/include/sanitizer/*.h ${D}${includedir}/sanitizer/
#}
#
#FILES_${PN}_dunfell += " ${includedir}/sanitizer/*.h"
