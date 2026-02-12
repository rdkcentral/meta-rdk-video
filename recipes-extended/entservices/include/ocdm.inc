# OpenCDM related switches

PACKAGECONFIG[opencdmi]       = "-DPLUGIN_OPENCDMI=ON \
                                 -DPLUGIN_OPENCDMI_AUTOSTART=true \
                                 -DPLUGIN_OPENCDMI_MODE=Local \
                                ,,wpeframework-clientlibraries,wpeframework-clientlibraries"
PACKAGECONFIG[opencdmi_ck]    = "-DPLUGIN_OPENCDMI_CLEARKEY=ON,,,wpeframework-ocdm-clearkey"
PACKAGECONFIG[opencdmi_pr4]   = "-DPLUGIN_OPENCDMI_PLAYREADY=ON \
                                 -DPLUGIN_OCDM_PLAYREADY_READ_DIR=/opt/drm/playready4 \
                                 -DPLUGIN_OCDM_PLAYREADY_STORE_LOCATION=/opt/drm/playready4/drmstore.dat,, \
                                 wpeframework-ocdm-playready-rdk,wpeframework-ocdm-playready-rdk"

PACKAGECONFIG[opencdmi_prnx]  = "-DPLUGIN_OPENCDMI_PLAYREADY_NEXUS=ON,,,wpeframework-ocdm-playready-nexus"
PACKAGECONFIG[opencdmi_prnx_svp]  = "-DPLUGIN_OPENCDMI_PLAYREADY_NEXUS_SVP=ON,,,wpeframework-ocdm-playready-nexus-svp"
PACKAGECONFIG[opencdmi_vgrdm] = "-DPLUGIN_OPENCDMI_PLAYREADY_VGDRM=ON,,"
PACKAGECONFIG[opencdmi_wv]    = "-DPLUGIN_OPENCDMI_WIDEVINE=ON,,,wpeframework-ocdm-widevine"
PACKAGECONFIG[opencdmi_wv_svp]    = "-DPLUGIN_OPENCDMI_WIDEVINE=ON,,,wpeframework-ocdm-widevine-nexus-svp"
PACKAGECONFIG[opencdmi_fps]    = "-DPLUGIN_OPENCDMI_FAIRPLAY=ON,,,wpeframework-ocdm-fairplay"
