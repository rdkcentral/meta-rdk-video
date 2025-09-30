#!/bin/sh

mkdir -p /tmp/gst
for LIB in libgstapp.so libgstaudioresample.so libgstcoreelements.so libgstplayback.so libgstaudioconvert.so libgstautodetect.so libgstrialtosinks.so libgsttypefindfunctions.so; do
  ln -sf /usr/lib/gstreamer-1.0/${LIB} /tmp/gst/${LIB}
done

ls -1 /usr/lib/libocdm.so* | while read LIB; do
  ln -sf /usr/lib/libocdmRialto.so.1 /tmp/gst/$(basename ${LIB})
done
