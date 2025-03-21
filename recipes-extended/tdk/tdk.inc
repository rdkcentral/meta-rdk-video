#Environment variable to enable TDK ipk build
TDK_IPK="${@bb.utils.contains('DISTRO_FEATURES','ENABLE_IPK','TRUE','FALSE',d)}"
TDK_RDM="${@bb.utils.contains('DISTRO_FEATURES','tdk_rdm','TRUE','FALSE',d)}"
TDK_HWPerf_OPENSOURCE_TOOLS="${@bb.utils.contains('DISTRO_FEATURES','tdk_benchmark','TRUE','FALSE',d)}"


#TDK installation directories
TDK_TARGETDIR="${@bb.utils.contains('DISTRO_FEATURES','ENABLE_IPK','/opt/TDK','${localstatedir}/TDK',d)}"
TDK_INSTALLDIR="${@bb.utils.contains('DISTRO_FEATURES','ENABLE_IPK','${localstatedir}/InstallTDK','',d)}"

#Flag to skip the QA check for TDK installation path
#INSANE_SKIP:${PN} = "installed-vs-shipped "
