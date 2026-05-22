# Remove unbuildable media packages (avbuffer, videodecoder, audiodecoder)
# Removed the packages to avoid error in newly added packages.

RDEPENDS:packagegroup-vdevice-vendor:remove = "avbuffer videodecoder audiodecoder"
