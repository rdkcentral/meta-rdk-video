RDEPENDS:${PN} += "libstdc++6 libgcc"
RDEPENDS:${PN}-tools += "libstdc++6 libgcc"
RDEPENDS:${PN}-plugins += "libstdc++6 libgcc"

INSANE_SKIP:${PN} += "file-rdeps"
INSANE_SKIP:${PN}-tools += "file-rdeps"
INSANE_SKIP:${PN}-plugins += "file-rdeps"
