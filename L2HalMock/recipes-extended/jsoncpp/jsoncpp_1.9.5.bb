SUMMARY = "A C++ library for interacting with JSON"
HOMEPAGE = "https://github.com/open-source-parsers/jsoncpp"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=fa2a23dd594a58674291f60f08a26161"

SRC_URI = "git://github.com/open-source-parsers/jsoncpp.git;branch=master;protocol=https"
SRCREV = "9059f5cad030ba11d37818847443a53918c327b1"

S = "${WORKDIR}/git"

inherit cmake

EXTRA_OECMAKE = "-DBUILD_SHARED_LIBS=ON -DJSONCPP_WITH_TESTS=OFF -DJSONCPP_WITH_POST_BUILD_UNITTEST=OFF"
