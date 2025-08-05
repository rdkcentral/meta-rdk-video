inherit meson pkgconfig

PACKAGECONFIG[soup2] = "-Dsoup2=enabled,-Dsoup2=disabled,libsoup-2.4"

EXTRA_OEMESON="-Dplatforms="
