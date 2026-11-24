# This file can be included by any module that wish to enable AddressSanitizer
# By default all bbappend files are disabled with BBMASK (DISTRO_FEATURE enable_asan)
# inside layer.conf

# Some recipes check if native binaries are runable
# that may lead to memory leak detection from address sanitizer.
# We don't want to detect any leaks during building.
export ASAN_OPTIONS="start_deactivated=1,detect_leaks=0"

DEPENDS:append:class-target = " gcc-sanitizers "
TUNE_CCARGS:append = " -fno-omit-frame-pointer -fno-optimize-sibling-calls -fsanitize=address "
TUNE_LDARGS:append = " -fsanitize=address -lpthread "
