ROOTFS_POSTPROCESS_COMMAND += "nmqe_check_rootfs; "

nmqe_check_rootfs() {
    if [ ! -x "${IMAGE_ROOTFS}${bindir}/nmqe-player" ]; then
        bbfatal "MoQ POC image is missing executable ${bindir}/nmqe-player"
    fi
    bbnote "MoQ POC rootfs contains ${bindir}/nmqe-player"
}
