SRC = Gstreamer.c
TARGET = output7056

PKG_CONFIG ?= pkg-config

CFLAGS += $(shell $(PKG_CONFIG) --cflags gstreamer-1.0)
LIBS   += $(shell $(PKG_CONFIG) --libs gstreamer-1.0)

# Default target
.PHONY: all
all: $(TARGET)

$(TARGET): $(SRC)
	$(CC) $(CFLAGS) $(LDFLAGS) $< -o $@ $(LIBS)

clean:
	rm -f $(TARGET)
