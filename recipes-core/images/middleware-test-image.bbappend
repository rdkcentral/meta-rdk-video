IMAGE_INSTALL:append = " nmqe-toolchain-probe"
ROOTFS_POSTPROCESS_COMMAND += "nmqe_check_toolchain_probe_rootfs; "

nmqe_check_toolchain_probe_rootfs() {
    if [ ! -x "${IMAGE_ROOTFS}${bindir}/nmqe-toolchain-probe" ]; then
        bbfatal "MoQ toolchain POC image is missing executable ${bindir}/nmqe-toolchain-probe"
    fi
    bbnote "MoQ toolchain POC rootfs contains ${bindir}/nmqe-toolchain-probe"
}
