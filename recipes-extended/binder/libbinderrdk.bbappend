FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

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

SRC_URI += "file://setup-binderrdk.sh \
	file://servicemanagerrdk.service \
	file://start-binderrdk.service \
"

FILES:${PN} += " setup-binderrdk.sh \
	start-binderrdk.service \
	setup-binderrdk.sh \
"
do_compile:prepend() {
	toolsdir="${S}/android/build-tools/linux-x86/bin"
	if [ -d "${toolsdir}" ]; then
		ln -sf $(which flex) "${toolsdir}/flex"
		ln -sf $(which bison) "${toolsdir}/bison"
	fi
}

FILES:${PN} += "servicemanagerrdk.service"

RDEPENDS:libbinderrdk += "bash"

SYSTEMD_PACKAGES := "${PN}"
SYSTEMD_SERVICE:${PN} = "servicemanagerrdk.service \
	start-binderrdk.service \
"
do_compile:prepend() {
	toolsdir="${S}/android/build-tools/linux-x86/bin"
	if [ -d "${toolsdir}" ]; then
		ln -sf $(which flex) "${toolsdir}/flex"
		ln -sf $(which bison) "${toolsdir}/bison"
	fi
}

do_install:append() {
	install -d ${D}/${systemd_unitdir}/system
	install -d ${D}/${bindir}
	install -m 0644 ${WORKDIR}/start-binderrdk.service ${D}/${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/servicemanagerrdk.service ${D}/${systemd_unitdir}/system
	install -m 0755 ${WORKDIR}/setup-binderrdk.sh ${D}/${bindir}
}

do_install:append:class-native() {
    install -d ${D}/${bindir}
    install -m 0755 ${B}/aidl ${D}/${bindir}
}

BBCLASSEXTEND = "native"



