# meta-rdk-video/recipes-extended/entservices/hdmicec_git.bbappend
SRCREV_hdmicec = "f0c27ca6627d00fb3f87d96dfe1624ce00bb0f29"
 
# Build-time dependency on the generated HAL AIDL headers
DEPENDS += " rdk-halif-aidl"