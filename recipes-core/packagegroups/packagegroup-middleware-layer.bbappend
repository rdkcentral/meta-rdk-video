# Remove webkit-dependent components - building without wpe-webkit
DEPENDS:remove = "browserwidget browserlauncher"
RDEPENDS:${PN}:remove = "wpe-webkit libwpe webkitbrowser-plugin wpe-backend-rdk wpe-webkit-web-inspector-plugin aamp rdk-browserlauncher fog"
