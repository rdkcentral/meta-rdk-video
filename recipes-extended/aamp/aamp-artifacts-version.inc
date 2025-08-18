
OS_TYPE = "rdkv"

PLATFORM_PATH_skyxione-de="xione"
PLATFORM_PATH_skyxione-uk="xione"
PLATFORM_PATH_xione-uk="xione"
PLATFORM_PATH_skyxione-it="xione"
PLATFORM_PATH_skyxione-alpaca-it="xione"
PLATFORM_PATH_skyxione-alpaca-de="xione"
PLATFORM_PATH_skyxione-flex2="xione-flex2"
PLATFORM_PATH_skyxione-sercomm-flex2="sercomm-xione-flex2"
PLATFORM_PATH_skyxione-sercomm-xoe="sercomm-xione-flex2"
PLATFORM_PATH_skyxione-alpaca-foxtel="xione-alpaca-foxtel"
PLATFORM_PATH_llama-foxtel="llama-foxtel"
PLATFORM_PATH_llama-uk="llama"
PLATFORM_PATH_llama-it="llama"
PLATFORM_PATH_llama-de="llama"
PLATFORM_PATH_sky-cello="cello"
PLATFORM_PATH_hisense-a6gp="hisense"
PLATFORM_PATH_hisense-50a6gx="hisense"
PLATFORM_PATH_element-teone="element-teone"
PLATFORM_PATH_pioneer-uhd="pioneer-uhd"
PLATFORM_PATH_es1-rtk="es1-rtk"
PLATFORM_PATH_es1-bcm="es1-bcm"
PLATFORM_PATH_xione-sercomm="xione-dhurv"
PLATFORM_PATH_skyxione-xoe-wnc="xione-xoe-wnc"

# Try to deduce AAMP artifacts version from available info
def deduce_artifacts_version_code(d):
    """
      2.2.X for DUNFELL SPRINT (X = datetime)
      2.3.X for KIRKSTONE SPRINT (X = datetime)
      3.2.X for DUNFELL STABLE (X = datetime)
      3.3.X for KIRKSTONE STABLE (X = datetime)
      5.Y.X for DUNFELL QS<Y> (X = datetime and Y = release branch name)
      6.Y.X for KIRKSTONE QS<Y> (X = datetime and Y = release branch name)
      5.Y.Z for DUNFELL RELEASE (Y = release and Z = release spin)
      6.Y.Z for KIRKSTONE RELEASE (Y = release and Z = release spin)
    """
    def get_yocto_code(isForRelease):
        overrides = d.getVar('OVERRIDES').split(':')
        if 'dunfell' in overrides:
           return 5 if isForRelease else 2
        elif 'kirkstone' in overrides:
           return 6 if isForRelease else 3
        bb.fatal('Unknown yocto version, cannot deduce AAMP artifacts version')
        return 0
    def try_sky_release():
        sky_release_version = d.getVar("RELEASE_VERSION_CHANGE", True)
        if not sky_release_version or sky_release_version == '000.000.00':
            return None
        Y = sky_release_version.split('.')[0]
        Z = d.getVar('RELEASE_SPIN') or '0'
        return '{}.{}.{}'.format(get_yocto_code(True), Y, Z)
    def try_sprint_or_stable():
        branch = d.getVar('PROJECT_BRANCH')
        if branch.endswith('sprint'):
           return '2.{}.{}'.format(get_yocto_code(False), '${DATETIME}')
        elif 'stable2' == branch or 'default' == branch:
           return '3.{}.{}'.format(get_yocto_code(False), '${DATETIME}')
        elif branch.startswith('QS'):
           return '{}.{}.{}'.format(get_yocto_code(True), branch[2:], '${DATETIME}')
        bb.note('Cannot deduce AAMP artifacts version from given branch %s' % branch)
        return '{}.{}'.format(branch, '${DATETIME}')
    return try_sky_release() or try_sprint_or_stable()

DATE_TIME = "${@time.strftime('%Y.%m.%d_%H:%M',time.gmtime())}"
AAMP_ARTIFACTS_VERSION ?= "${OS_TYPE}-${PLATFORM_PATH}_${@deduce_artifacts_version_code(d)}"
AAMP_ARTIFACTS_VERSION[vardepsexclude] += "DATETIME"

