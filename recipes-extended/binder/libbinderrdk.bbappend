
DEPENDS += "bison-native flex-native"

inherit pkgconfig

SRCREV = "4278b0c80d098b1853976f3ced5275d77e53c0aa"

PROVIDES += "liblogrdk"

EXTRA_OECMAKE:append = " \
	-DBUILD_ENV_YOCTO=ON \
	-DBUILD_ENV_HOST=OFF \
	-DTARGET_LIB64_VERSION=ON \
"
EXTRA_OECMAKE:append:class-native = " \
    -DBUILD_ENV_HOST=ON \
"

do_compile:prepend() {
	toolsdir="${S}/android/build-tools/linux-x86/bin"
	if [ -d "${toolsdir}" ]; then
		ln -sf $(which flex) "${toolsdir}/flex"
		ln -sf $(which bison) "${toolsdir}/bison"
	fi
}

RDEPENDS:libbinderrdk += "bash"

SYSTEMD_PACKAGES := "${PN}"

do_compile:prepend() {
	toolsdir="${S}/android/build-tools/linux-x86/bin"
	if [ -d "${toolsdir}" ]; then
		ln -sf $(which flex) "${toolsdir}/flex"
		ln -sf $(which bison) "${toolsdir}/bison"
	fi
}

do_install:append:class-native() {
    install -d ${D}/${bindir}
    install -m 0755 ${B}/aidl ${D}/${bindir}
}

BBCLASSEXTEND = "native"



