do_install:append() {
    # RDKEMW-18111: Add mtls.target dependency for mTLS boot ordering
    if [ -f ${D}${systemd_unitdir}/system/ctrlm-main.service ]; then
        # Add After=mtls.target to [Unit] section
        sed -i '/^\[Unit\]/a After=mtls.target' \
            ${D}${systemd_unitdir}/system/ctrlm-main.service

        # Check if [Install] section exists and modify WantedBy
        if grep -q "^\[Install\]" ${D}${systemd_unitdir}/system/ctrlm-main.service; then
            # Replace existing WantedBy with mtls.target
            sed -i 's/WantedBy=.*/WantedBy=mtls.target/g' \
                ${D}${systemd_unitdir}/system/ctrlm-main.service
        else
            # Add new [Install] section
            echo "" >> ${D}${systemd_unitdir}/system/ctrlm-main.service
            echo "[Install]" >> ${D}${systemd_unitdir}/system/ctrlm-main.service
            echo "WantedBy=mtls.target" >> ${D}${systemd_unitdir}/system/ctrlm-main.service
        fi
    fi
}
