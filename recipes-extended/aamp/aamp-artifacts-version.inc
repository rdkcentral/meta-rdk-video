
OS_TYPE = "rdke"

PLATFORM_PATH_skyxione="stream"
PLATFORM_PATH_sky-llama-panel="glass"
PLATFORM_PATH_sky-cello="cello"
PLATFORM_PATH_xione-sercomm="dhruv"

# Disable for xione US variants and other platforms for now
PLATFORM_PATH_skyxione-xoe="unknown"
PLATFORM_PATH_skyxione-flex2="unknown"
PLATFORM_PATH ?= "stream"

# Try to find the yocto version
def get_yocto_code(d):
    overrides = d.getVar('OVERRIDES').split(':')
    if 'dunfell' in overrides:
        return "DUNFELL"
    elif 'kirkstone' in overrides:
        return "KIRKSTONE"
    else:
        return "UNKNOWN"

# Try to deduce AAMP artifacts version from available info
def deduce_aamp_artifacts_version_code(d):
    """
    For non-release builds -
      X.Y (X = branch name, Y = datetime )
      25Q3.Y for 25Q3_sprint ( Y = datetime )
      stable.Y for stable2 ( Y = datetime )
      8.2_vipa.Y for 8.2_vipa (Y = datetime )
    For release builds -
      X.Y ( X = RELEASE_VERSION_STRING, Y = SPIN )
      038.012.00.8.2.0.13.1 for 8.2 Release ( X = 038.012.00.8.2.0.13, Y = 1 )
    """
    def try_sky_release():
        X = d.getVar("RELEASE_VERSION_STRING", True)
        if not X:
            return None
        Y = d.getVar('RELEASE_SPIN') or '0'
        return '{}.{}'.format(X, Y)

    def try_sprint_or_stable():
        branch = d.getVar('PROJECT_BRANCH')
        if branch.endswith('sprint'):
           if '_' in branch:
               X = branch.split('_')[0]
           else:
               X = branch  # fallback to full branch name if no underscore
           return '{}.{}'.format(X, '${DATETIME}')
        elif 'stable2' == branch or 'default' == branch:
           return '{}.{}'.format('develop', '${DATETIME}')
        return '{}.{}'.format(branch, '${DATETIME}') # most likely a patch branch
    return try_sky_release() or try_sprint_or_stable()

# Try to deduce the VIPA widget version prefix
def deduce_vipa_widget_version_prefix(d):
    """
    For non-release builds -
      X_OS_Y_Z (X = branch name, OS = V or E, Y = platform, Z = JS version )
      25Q3_V_Y_Z for 25Q3_sprint RDKV ( Y = platform, Z = JS version )
      stable_V_Y_Z for stable2 RDKV ( Y = platform, Z = JS version )
      8.2_vipa_V_Y_Z for 8.2_vipa RDKV ( Y = platform, Z = JS version )
    For release builds -
      X_OS_Y_Z (X = RELEASE.SPIN, OS = V or E, Y = platform, Z = JS version )
      038.012.00.8.2.0.13.1_V_Y_Z for 8.2 RDKV ( Y = platform, Z = JS version )
    JS version will be added when widget is created
    """
    def generate_for_release():
        X = d.getVar("RELEASE_VERSION_STRING", True)
        if not X:
            return None
        Y = d.getVar('RELEASE_SPIN') or '0'
        PLATFORM = d.getVar('PLATFORM_PATH')
        return '{}.{}_V_{}'.format(X, Y, PLATFORM)

    def generate_for_sprint_or_stable():
        branch = d.getVar('PROJECT_BRANCH')
        X = branch  # Default to full branch name
        PLATFORM = d.getVar('PLATFORM_PATH')

        if branch.endswith('sprint'):
            if '_' in branch:
                X = branch.split('_')[0]
        elif 'stable2' == branch or 'default' == branch:
            X = "stable"

        return '{}_V_{}'.format(X, PLATFORM)

    return generate_for_release() or generate_for_sprint_or_stable()

# DATETIME is defined elsewhere
WIDGET_VERSION_PREFIX = "${@deduce_vipa_widget_version_prefix(d)}"
AAMP_ARTIFACTS_VERSION ?= "${OS_TYPE}-${PLATFORM_PATH}-${@deduce_aamp_artifacts_version_code(d)}"
AAMP_ARTIFACTS_VERSION[vardepsexclude] += "DATETIME"
