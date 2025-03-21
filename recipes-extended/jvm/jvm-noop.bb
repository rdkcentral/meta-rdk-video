SUMMARY = "This recipe compiles and installs the sample no-op libraries for the dtcp interface."
SECTION = "console/utils"

LICENSE = "Apache-2.0"

PROVIDES = "virtual/jvm"
RPROVIDES:${PN} =  "virtual/jvm"
do_compile[noexec] = "1"

do_install () {
echo "This is Dummy JVM , in place of actual jvm that needs to be replaced by jvm vendors" > ${D}/JVM_README
}

FILES:${PN} = "/JVM_README"
