RDEPENDS:${PN} += "libstdc++ libgcc"
RDEPENDS:${PN}-tools += "libstdc++ libgcc"
RDEPENDS:${PN}-plugins += "libstdc++ libgcc"

INSANE_SKIP:${PN} += "file-rdeps"
INSANE_SKIP:${PN}-tools += "file-rdeps"
INSANE_SKIP:${PN}-plugins += "file-rdeps"
