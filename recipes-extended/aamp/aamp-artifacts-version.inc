
OS_TYPE = "rdke"

PLATFORM_PATH:xione-uk="stream"
PLATFORM_PATH ?= "unknown"

# Try to find the yocto version
def get_yocto_code(d):
    overrides = d.getVar('OVERRIDES').split(':')
    if 'dunfell' in overrides:
        return "DUNFELL"
    elif 'kirkstone' in overrides:
        return "KIRKSTONE"
    else:
        return "UNKNOWN"

# Helper function to extract branch name after '/' or use full branch name
def extract_branch_name(d):
    branch = d.getVar('PROJECT_BRANCH')
    if '/' in branch:
        return branch.split('/')[1]
    return branch

# Try to deduce AAMP artifacts version from available info
def deduce_aamp_artifacts_version_code(d):
    """
    For non-release builds -
      X.Y (X = branch name, Y = datetime )
      develop.Y for develop ( Y = datetime )
      2.16.0.Y for support/2.16.0 (Y = datetime )
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
        X = extract_branch_name(d)
        return '{}.{}'.format(X, '${DATETIME}')
    return try_sky_release() or try_sprint_or_stable()

# Try to deduce the VIPA widget version prefix
def deduce_vipa_widget_version_prefix(d):
    """
    For non-release builds -
      X_OS_Y_Z (X = branch name, OS = V or E, Y = platform, Z = JS version )
      develop_E_Y_Z for develop RDKE ( Y = platform, Z = JS version )
      2.16.0_E_Y_Z for support/2.16.0 RDKE ( Y = platform, Z = JS version )
    For release builds -
      X_OS_Y_Z (X = RELEASE.SPIN, OS = V or E, Y = platform, Z = JS version )
      038.012.00.8.2.0.13.1_E_Y_Z for 8.2 RDKE ( Y = platform, Z = JS version )
    JS version will be added when widget is created
    """
    def generate_for_release():
        X = d.getVar("RELEASE_VERSION_STRING", True)
        if not X:
            return None
        Y = d.getVar('RELEASE_SPIN') or '0'
        PLATFORM = d.getVar('PLATFORM_PATH')
        return '{}.{}_E_{}'.format(X, Y, PLATFORM)

    def generate_for_sprint_or_stable():
        X = extract_branch_name(d)
        PLATFORM = d.getVar('PLATFORM_PATH')
        return '{}_E_{}'.format(X, PLATFORM)

    return generate_for_release() or generate_for_sprint_or_stable()

# DATETIME is defined elsewhere
WIDGET_VERSION_PREFIX = "${@deduce_vipa_widget_version_prefix(d)}"
AAMP_ARTIFACTS_VERSION ?= "${OS_TYPE}-${PLATFORM_PATH}-${@deduce_aamp_artifacts_version_code(d)}"
AAMP_ARTIFACTS_VERSION[vardepsexclude] += "DATETIME"
