# do_rootfs task runs openembedded-core/scripts/postinst-intercepts/update_gio_module_cache
# as one of postinstall tasks. It updates gio module cache under qemu-arm
# that doesn't work with leak detector (as both uses ptrace).
# Disable leak detector as we don't want it here anyway
export ASAN_OPTIONS="start_deactivated=1,detect_leaks=0"
