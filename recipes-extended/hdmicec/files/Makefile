#* ******************************************************************************
#*  If not stated otherwise in this file or this component's LICENSE
#*  file the following copyright and licenses apply:
#*
#*  Copyright 2025 RDK Management
#*
#*  Licensed under the Apache License, Version 2.0 (the License);
#*  you may not use this file except in compliance with the License.
#*  You may obtain a copy of the License at
#*
#*  http://www.apache.org/licenses/LICENSE-2.0
#*
#*  Unless required by applicable law or agreed to in writing, software
#*  distributed under the License is distributed on an AS IS BASIS,
#*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#*  See the License for the specific language governing permissions and
#*  limitations under the License.
#*
#* ******************************************************************************

.PHONY: all clean

CXX      ?= g++

AIDL_LIB_NAME := hal_aidl
AIDL_LIB      := lib$(AIDL_LIB_NAME).so

BUILD_DIR     ?= $(CURDIR)/../build
AIDL_GEN_DIR  ?= $(BUILD_DIR)/current
AIDL_SRC_DIR  := $(AIDL_GEN_DIR)/cpp/com/rdk/hal

# Source and object files
AIDL_SRC := $(shell find $(AIDL_SRC_DIR) -type f -name '*.cpp')
AIDL_OBJ := $(AIDL_SRC:%.cpp=%.o)

AIDL_LIB_PATH := $(BUILD_DIR)/usr/lib/$(AIDL_LIB)

# Flags
AIDL_LIB_INC_FLAGS := -I$(AIDL_GEN_DIR)/h \
                      -I$(BUILD_DIR)/usr/include

AIDL_LIB_LD_LIBS   := -lbinder -lutils

AIDL_LIB_LDFLAGS   := -L$(BUILD_DIR)/usr/lib \
                      -Wl,-rpath-link=$(BUILD_DIR)/usr/lib

AIDL_LIB_CFLAGS    := -Wall -Wextra -std=c++17 -ggdb -fPIC

# Default target
all: $(AIDL_LIB_PATH)

# Build shared library
$(AIDL_LIB_PATH): $(AIDL_OBJ)
	$(CXX) -shared $(AIDL_LIB_LDFLAGS) $^ -o $@ $(AIDL_LIB_LD_LIBS)

# Compile objects
%.o: %.cpp
	$(CXX) $(AIDL_LIB_CFLAGS) $(AIDL_LIB_INC_FLAGS) -c $< -o $@

# Clean target
clean:
	@echo "Cleaning object files and library..."
	rm -f $(AIDL_OBJ) $(AIDL_LIB_PATH)


