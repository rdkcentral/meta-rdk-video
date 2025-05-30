SUMMARY = "Custom package group for adding Opensource Benchmark components"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit packagegroup


PACKAGES = "\
    packagegroup-benchmark-tdk \
    "

#Opensource benchmark tools
RDEPENDS:packagegroup-benchmark-tdk = "\
  iozone3 \
  stress-ng \
  nbench-byte \
  sysbench \
  tinymembench \
  "
