require ./thunder-clientlibraries.inc

# These patches are upstreamed on branch 'R4-4patches-to-R5.3'
SRC_URI += "\
    file://0001-CipherNetflix-methods-return-type-changes.patch \
    file://0002-R4.2-compilation-assert-fix.patch \
    file://0003-RDK-49093-RDK-49094-RDK-49095-Sync-up-WPEFramework.patch \
    file://0004-RDK-49093-RDK-49094-RDK-49095-Sync-up-WPEFramework.patch \
    file://0005-Add-vault-platform-case.patch \
    file://0006-PATCH-add-0001-Implement-IPersistent-interface-for-R.patch \
    file://0007-PATCH-Add-0001-SecAPI-Re-acquire-sec-handle-after-fl.patch \
    file://0008-PATCH-Add-0002-RDKEMW-19048-Release-and-reacquire-Va.patch \
    file://0009-PATCH-Add-0003-RDKEMW-20680-vault-processor-release-.patch \
    file://0010-PowerController-PowerManager-plugin-client-implement.patch \
    "
