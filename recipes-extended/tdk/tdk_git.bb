SUMMARY = "Test Development Kit for RDK stack"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=94800ec97dc80e9d6e0c228c2100adc2"

PV = "${RDK_RELEASE}"
SRC_URI = "${CMF_GIT_ROOT}/rdkv/tools/tdkv;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH};name=tdk"
SRCREV_tdk = "b05d3991d362bf03c4d267c0ba7eac4b806f74f3"
SRCREV_FORMAT = "tdk"

S = "${WORKDIR}/git"

require recipes-extended/tdk/tdk.inc

#Adding new package "tdk-dl" which will be downloaded as RDM package
TDK_DL_PACK:= "${@bb.utils.contains('DISTRO_FEATURES', 'tdk_rdm', '${PN}-dl', '', d)}"
PACKAGE_BEFORE_PN += "${TDK_DL_PACK}"

DEPENDS = "${@bb.utils.contains('DISTRO_FEATURES', 'gstreamer1', 'gstreamer1.0', 'gstreamer', d)}"
ENABLE_GST1 = "--enable-gstreamer1=${@bb.utils.contains('DISTRO_FEATURES', 'gstreamer1', 'yes', 'no', d)}"
EXTRA_OECONF += "${ENABLE_GST1}"

EXTRA_OECONF += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_firebolt_compliance_tdk',' --enable-fireboltcompliance', '', d)}"
EXTRA_OECONF += "--enable-xupnp"
EXTRA_OECONF += " --enable-westeroshal"

DEPENDS += "iarmbus devicesettings rdk-logger jsoncpp jsonrpc westeros aamp"
DEPENDS:append:client = " iarmmgrs servicemanager netsrvmgr moca-hal"
DEPENDS:append:hybrid = " iarmmgrs servicemanager xupnp virtual/dtcpmgr"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES','tdk_benchmark','libxml2', '',d)}"

RDEPENDS:${PN} += " jsoncpp jsonrpc bash "
RDEPENDS:${PN}_dunfell  += " jsoncpp jsonrpc bash devicesettings"

# This variable used as the condition for compiling TDK components
# for hybrid and client devices
EXTRA_OECONF:append:hybrid = " --enable-hybrid"
EXTRA_OECONF:append:client = " --enable-client"
EXTRA_OECONF:append:qemux86mc = " --enable-qemux86mc"
EXTRA_OECONF:append:qemux86hyb = " --enable-qemux86hyb"
ASNEEDED= ""

#Default components for Hybrid and Client devices
EXTRA_OECONF:append:hybrid = " --enable-dtcp"
EXTRA_OECONF:append:hybrid = " --enable-snmp"
EXTRA_OECONF:append:client = " --enable-tr69"
EXTRA_OECONF:append:client = " --enable-netsrvmgr"

inherit autotools systemd pkgconfig

# Until the TDK scripts change, we have to manually install in ${TDK_TARGETDIR}
do_install:append () {
    install -d ${D}${TDK_TARGETDIR}
    install -d ${D}${TDK_TARGETDIR}/scripts
    install -d ${D}${TDK_TARGETDIR}/opensourcecomptest
    if [ "${TDK_IPK}" = "TRUE" ]; then
        install -d ${D}${TDK_TARGETDIR}/lib
        install -D -p -m 755 ${D}${bindir}/rdk_tdk_agent_process ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/tst_iarmbus ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/DUMMYMgr ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/gen_single_event ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/gen_multiple_events ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/tdk_mediapipelinetests ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/tdk_apparmoraccess ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/tdk_mediapipelinetests_trickplay ${D}${TDK_TARGETDIR}/
        if ${@bb.utils.contains('DISTRO_FEATURES', 'enable_firebolt_compliance_tdk', 'true', 'false', d)}; then
            install -D -p -m 755 ${D}${bindir}/Essos_TDKTestApp ${D}${TDK_TARGETDIR}/
            install -D -p -m 755 ${D}${bindir}/Westeros_TDKTestApp ${D}${TDK_TARGETDIR}/
        fi
        install -D -p -m 755 ${D}${libdir}/*.so* ${D}${TDK_TARGETDIR}/lib/
        rm -rf ${D}${libdir}/

    elif [ "${TDK_RDM}" = "TRUE" ]; then
        install -d ${D}${TDK_TARGETDIR}/lib
	install -d ${D}${systemd_unitdir}/system
        install -d ${D}${base_libdir}/rdk/
        install -D -p -m 755 ${D}${bindir}/rdk_tdk_agent_process ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/tst_iarmbus ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/DUMMYMgr ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/gen_single_event ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/gen_multiple_events ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/tdk_mediapipelinetests ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/tdk_mediapipelinetests_trickplay ${D}${TDK_TARGETDIR}/
        install -D -p -m 755 ${D}${bindir}/tdk_apparmoraccess ${D}${TDK_TARGETDIR}/
        if ${@bb.utils.contains('DISTRO_FEATURES', 'enable_firebolt_compliance_tdk', 'true', 'false', d)}; then
            install -D -p -m 755 ${D}${bindir}/Essos_TDKTestApp ${D}${TDK_TARGETDIR}/
            install -D -p -m 755 ${D}${bindir}/Westeros_TDKTestApp ${D}${TDK_TARGETDIR}/
        fi
        install -m 0644 ${S}/tdk.service ${D}${systemd_unitdir}/system/tdk.service
        install -m 755 ${S}/tdkstartup.sh ${D}${base_libdir}/rdk/
        install -m 755 ${S}/tdkstop.sh ${D}${base_libdir}/rdk/
        rm -rf ${D}${bindir}/rdk_tdk_agent_process
        rm -rf ${D}${bindir}/tst_iarmbus
        rm -rf ${D}${bindir}/DUMMYMgr
        rm -rf ${D}${bindir}/gen_single_event
        rm -rf ${D}${bindir}/gen_multiple_events
        rm -rf ${D}${bindir}/tdk_mediapipelinetests
        rm -rf ${D}${bindir}/tdk_mediapipelinetests_trickplay
        rm -rf ${D}${bindir}/tdk_apparmoraccess
        if ${@bb.utils.contains('DISTRO_FEATURES', 'enable_firebolt_compliance_tdk', 'true', 'false', d)}; then
            rm -rf ${D}${bindir}/Essos_TDKTestApp
            rm -rf ${D}${bindir}/Westeros_TDKTestApp
        fi
    elif [ "${TDK_HWPerf_OPENSOURCE_TOOLS}" = "TRUE" ]; then
        install -d ${D}${base_libdir}/rdk/
        install -d ${D}${systemd_unitdir}/system
        ln -sf ${bindir}/rdk_tdk_agent_process ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/tst_iarmbus ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/DUMMYMgr ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/gen_single_event ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/gen_multiple_events ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/tdk_mediapipelinetests ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/tdk_mediapipelinetests_trickplay ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/tdk_apparmoraccess ${D}${TDK_TARGETDIR}/
        if ${@bb.utils.contains('DISTRO_FEATURES', 'enable_firebolt_compliance_tdk', 'true', 'false', d)}; then
            ln -sf ${bindir}/Essos_TDKTestApp ${D}${TDK_TARGETDIR}/
            ln -sf ${bindir}/Westeros_TDKTestApp ${D}${TDK_TARGETDIR}/
        fi
        install -m 0644 ${S}/tdk.service ${D}${systemd_unitdir}/system/tdk.service
        install -m 755 ${S}/tdkstartup.sh ${D}${base_libdir}/rdk/
        install -m 755 ${S}/tdkstop.sh ${D}${base_libdir}/rdk/
        install -m 755 ${S}/utilities/HWPerf_metric_parser.sh ${D}${TDK_TARGETDIR}/
        install -m 755 ${S}/utilities/HWPerf_metric_details.xml ${D}${TDK_TARGETDIR}/
        install -m 755 ${S}/utilities/TDK_HWPerfTools_Executor.sh ${D}${TDK_TARGETDIR}/
    else
        install -d ${D}${base_libdir}/rdk/
        install -d ${D}${systemd_unitdir}/system
        ln -sf ${bindir}/rdk_tdk_agent_process ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/tst_iarmbus ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/DUMMYMgr ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/gen_single_event ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/gen_multiple_events ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/tdk_mediapipelinetests ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/tdk_mediapipelinetests_trickplay ${D}${TDK_TARGETDIR}/
        ln -sf ${bindir}/tdk_apparmoraccess ${D}${TDK_TARGETDIR}/
        if ${@bb.utils.contains('DISTRO_FEATURES', 'enable_firebolt_compliance_tdk', 'true', 'false', d)}; then
            ln -sf ${bindir}/Essos_TDKTestApp ${D}${TDK_TARGETDIR}/
            ln -sf ${bindir}/Westeros_TDKTestApp ${D}${TDK_TARGETDIR}/
        fi
        install -m 0644 ${S}/tdk.service ${D}${systemd_unitdir}/system/tdk.service
        install -m 755 ${S}/tdkstartup.sh ${D}${base_libdir}/rdk/
        install -m 755 ${S}/tdkstop.sh ${D}${base_libdir}/rdk/
    fi

    install -d ${D}${includedir}/rdk/tdk
    install -m 0644 ${S}/agent/include/*.h ${D}${includedir}/rdk/tdk
    install -m 0644 ${S}/agent/scripts/TDK_version.txt ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/agent/scripts/runSysStat.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/agent/scripts/RemoveLogs.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/agent/scripts/file_copy.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/agent/scripts/PushLogs.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/agent/scripts/uploadLogs.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/agent/scripts/output_json_parser.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/agent/scripts/FreeUpTDKPorts.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/SystemUtil_stub/scripts/Spark_testrunner.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/SystemUtil_stub/scripts/Optimusprime_testrunner.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/SystemUtil_stub/scripts/Rdk_browser2.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/SystemUtil_stub/scripts/Ledmgr_testrunner.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/SystemUtil_stub/scripts/tdk_apparmor_tests.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/SystemUtil_stub/scripts/apparmor_profile.config ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/IARMBUS_stub/scripts/RunAppInBackground.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/SystemUtil_stub/scripts/system_sanity_check.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/SystemUtil_stub/scripts/sanity_check.config ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/SystemUtil_stub/scripts/system_sanity_check_before_reboot.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/SystemUtil_stub/scripts/system_sanity_check_after_reboot.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/SystemUtil_stub/scripts/system_sanity_check_eth_wifi_toggle_functionality.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/SystemUtil_stub/scripts/system_sanity_lightsleep_service_status.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/SystemUtil_stub/scripts/system_sanity_lightsleep_service_status_after_reboot.sh ${D}${TDK_TARGETDIR}/
    install -m 755 ${S}/Apparmor_Stub/scripts/tdk_sample.profile ${D}${TDK_TARGETDIR}/
    if ${@bb.utils.contains('DISTRO_FEATURES', 'enable_firebolt_compliance_tdk', 'true', 'false', d)}; then
        install -m 755 ${S}/FireboltCompliance_Validation/scripts/RunGraphicsTDKTest.sh ${D}${TDK_TARGETDIR}/
        install -m 755 ${S}/FireboltCompliance_Validation/scripts/ExecuteSuite.sh ${D}${TDK_TARGETDIR}/opensourcecomptest/
        install -m 755 ${S}/FireboltCompliance_Validation/scripts/SecurityTestTDK.sh ${D}${TDK_TARGETDIR}/
    fi
}

do_install:append:client() {
    install -m 755 ${S}/NetSrvMgr_stub/scripts/* ${D}${TDK_TARGETDIR}/scripts/
}

SYSTEMD_SERVICE:${PN} = "${@bb.utils.contains('DISTRO_FEATURES','ENABLE_IPK','','tdk.service',d)}"

#In both RDM and traditional scenarios, below startup script and service files will be part of tdk package only
FILES:${PN} = " \
    ${base_libdir}/rdk/* \
"
FILES:${PN} += "${@bb.utils.contains('DISTRO_FEATURES','ENABLE_IPK','',' ${systemd_unitdir}/system/tdk.service ',d)}"

#All artifacts will be part of tdk package when tdk_rdm distro is not present (in traditional tdk builds)
FILES:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'tdk_rdm', ' ', ' ${TDK_TARGETDIR}/* ${TDK_TARGETDIR}/scripts/* ${TDK_TARGETDIR}/opensourcecomptest/* ${bindir}/*  ${libdir}/*.so* ', d)"

#All artifacts will be packed in tdk-dl package when tdk_rdm distro is enabled
FILES:${PN}-dl = "${@bb.utils.contains('DISTRO_FEATURES', 'tdk_rdm', ' ${TDK_TARGETDIR}/* ${TDK_TARGETDIR}/scripts/* ${TDK_TARGETDIR}/opensourcecomptest/* ${bindir}/*  ${libdir}/*.so* /etc/* ', '', d)"


FILES:${PN}-dbg = " \
    ${TDK_TARGETDIR}/.debug \
    ${TDK_TARGETDIR}/scripts/.debug \
    ${prefix}/src/debug \
    ${bindir}/.debug \
    ${libdir}/.debug \
"
FILES:${PN}-dbg += "${@bb.utils.contains('DISTRO_FEATURES','ENABLE_IPK','${TDK_TARGETDIR}/lib/.debug','',d)}"
