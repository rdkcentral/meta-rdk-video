# Disable webkit JS bindings - building without wpe-webkit
DEPENDS:remove = "wpe-webkit"
EXTRA_OECMAKE += " -DCMAKE_WPEWEBKIT_WATERMARK_JSBINDINGS=0"
