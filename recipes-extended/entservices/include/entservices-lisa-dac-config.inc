# depending on detected platform during build
# set correct dacBundlePlatformNameOverride and
# dacBundleFirmwareCompatibilityKey in LISA.json config
# Also add asmsUrl

def get_lisa_dac_config(d):
    machine_config_map_dunfell = {
        'raspberrypi-': ('rpi3', '1.0.0-f4b0603d7d93dfa10ed932ddb0f324d334bc40f9-dbg'),
        'raspberrypi4': ('rpi4', '1.0.0-e71889dc02521bfdc2f9f38f750b34224184c375-dbg'),
        'brcm972180hbc': ('7218c', '1.0.0-41abf2fba994b2d04baedeca6d5adf097ece9f79-dbg'),
        'brcm972126ott': ('72126ott', '1.0.0-e7d1057ede4140493e3604ae4d5b2b73d4620a5f-dbg'),
        'brcm972127ott': ('72127ott', ''),
        'ah212': ('ah212', '1.0.0-aaa9510325f054535768497092c973872d44fef3-dbg'),
        'at301': ('ah212', '1.0.0-aaa9510325f054535768497092c973872d44fef3-dbg'),
        'hp44h': ('ah212', '1.0.0-aaa9510325f054535768497092c973872d44fef3-dbg'),
        'ap222': ('ap222', '1.0.0-8d58497b284a33438d8c3a6e727caba1ac6ff5c8-dbg'),
        'mediabox': ('rtd1319', '1.0.0-c1bdea2bac14d0f69ba5681bc0bf1ff2fea720ee-dbg'),
        'blade': ('rtd1319', '1.0.0-c1bdea2bac14d0f69ba5681bc0bf1ff2fea720ee-dbg'),
        'm393': ('m393', '1.0.0-133c97e6d586132d7986d4f1d3cbb9e167de04c1-dbg')
    }

    machine_config_map_kirkstone = {
        'raspberrypi-': ('rpi3', ''),
        'raspberrypi4': ('rpi4', '1.0.0-b34e9a38a2675d4cd02cf89f7fc72874a4c99eb0-dbg'),
        'brcm972180hbc': ('7218c', ''),
        'brcm972126ott': ('72126ott', '1.0.0-6a60471939a0ea09e5568c2f6fbd672c9406eb0a-dbg'),
        'brcm972127ott': ('72127ott', ''),
        'brcm974116sff': ('74116sff', '1.0.0-1ea4059882e94d44064e8a1e86a8532fa1f5e251-dbg'),
        'ah212': ('ah212', '1.0.0-15cd0290c4412eb17d1888767806b69d25d66eb8-dbg'),
        'at301': ('ah212', '1.0.0-15cd0290c4412eb17d1888767806b69d25d66eb8-dbg'),
        'hp44h': ('ah212', '1.0.0-15cd0290c4412eb17d1888767806b69d25d66eb8-dbg'),
        'ap222': ('ap222', ''),
        'rtd1325': ('rtd1325', '1.0.0-521679d6a36270f5aa3bb70f1f2a62be1575e6b9-dbg'),
        'mediabox': ('rtd1319', '1.0.0-3c34f1bd496bff1894423372d35316c4c9ea7224-dbg'),
        'blade': ('rtd1319', '1.0.0-3c34f1bd496bff1894423372d35316c4c9ea7224-dbg'),
        'm393': ('m393', '')
    }

    m = d.getVar('MACHINE', True)
    ver = d.getVar("DISTRO_CODENAME", True)
    if "dunfell" in ver:
        for key in machine_config_map_dunfell:
            if key in m:
                return machine_config_map_dunfell[key]
        return '',''
    else:
        for key in machine_config_map_kirkstone:
            if key in m:
                return machine_config_map_kirkstone[key]
        return '',''

CONFIG_URL ?= "https://280222515084-rdkm-apps-resources.s3.eu-central-1.amazonaws.com/configuration/cpe.json"
LISA_DAC_CONFIG_PLATFORM ?= "${@get_lisa_dac_config(d)[0]}"
LISA_DAC_CONFIG_FIRMWARE ?= "${@get_lisa_dac_config(d)[1]}"

LISA_DEFAULT_IMAGE_STORAGE_PATH ?= "/opt/dac_apps"
LISA_DEFAULT_APPDATA_STORAGE_PATH ?= "/opt/dac_apps/data"
LISA_IMAGE_PATH ?= "${@d.getVar('PRODUCT_CONF_LISA_IMAGE_STORAGE_PATH') or d.getVar('LISA_DEFAULT_IMAGE_STORAGE_PATH') }"
LISA_APPDATA_PATH ?= "${@d.getVar('PRODUCT_CONF_LISA_APPDATA_STORAGE_PATH') or d.getVar('LISA_DEFAULT_APPDATA_STORAGE_PATH') }"

python do_inject_dac_config() {
    import json

    for root, dirs, files in os.walk(d.getVar('D', True)):
        if 'LISA.json' in files:
            file_path = os.path.join(root, 'LISA.json')
            print(f"Found LISA config at: {file_path}")

            with open(file_path, 'r') as f:
                json_data = json.load(f)

            variables_mapping = {
                'LISA_DAC_CONFIG_PLATFORM': 'dacBundlePlatformNameOverride',
                'LISA_DAC_CONFIG_FIRMWARE': 'dacBundleFirmwareCompatibilityKey',
                'CONFIG_URL': 'configUrl'
            }

            for yocto_var, json_key in variables_mapping.items():
                value = d.getVar(yocto_var, True).strip()
                if value:
                    json_data['configuration'][json_key] = value

            with open(file_path, 'w') as f:
                json.dump(json_data, f, indent=2)
}

addtask inject_dac_config after do_install before do_package
