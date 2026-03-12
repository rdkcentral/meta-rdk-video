# Compiler and tools
CXX       ?= g++
AR        ?= ar
ARFLAGS   ?= rcs   #Need to learn about the flags RCS.

# Directories
INCLUDE   = include
SRC       = src
BUILD     = build
LIB       = lib

#To create symlik files and versioned files 
LIBNAME     = calc
SONAME      = lib$(LIBNAME).so.1
REALNAME    = lib$(LIBNAME).so.1.0.0
DEVLINK     = lib$(LIBNAME).so



# Output
STATIC_LIB = $(LIB)/libcalc.a
SHARED_LIB = $(LIB)/libcalc.so
#MAIN_OBJ = $(BUILD)/

# Flags ###---> Need to learn about the flags
CXXFLAGS  += -std=c++17 -O2 -Wall -Wextra -I$(INCLUDE)

# Sources and objects
SRCS := $(wildcard $(SRC)/*.cpp)
OBJS := $(patsubst $(SRC)/%.cpp,$(BUILD)/%.o,$(SRCS))

# Default target
.PHONY: all
all: main
#-----------------------------------------------------------------------#
##
##Object creation for all the function definations which are in src folder to build folder
#$(BUILD)/%.o: $(SRC)/%.cpp | $(BUILD)
#	$(CXX) $(CXXFLAGS) -c $< -o $@

#Creating a static lib archive from all the object files in build folder
#$(STATIC_LIB): $(OBJS) | $(LIB)
#	$(AR) $(ARFLAGS) $@ $^

#Linking the static library to create executable
#main: $(STATIC_LIB) main.cpp
#	$(CXX) $(CXXFLAGS) -static main.cpp -L $(LIB) -lcalc -o $@

#-----------------------------------------------------------------------------#
#Compiling source fiiles in to PIC object files.
$(BUILD)/%.o: $(SRC)/%.cpp | $(BUILD)
	$(CXX) $(CXXFLAGS) -fPIC -c $< -o $@

#Creating a shared lib archive (.so) from the pic object files
#$(SHARED_LIB): $(OBJS) | $(LIB)
#	$(CXX) $(CXXFLAGS) $(LDFLAGS) -shared -o $@ $^

#to create verisoned .so files

$(SHARED_LIB): $(OBJS) | $(LIB)
	$(CXX) $(CXXFLAGS) $(LDFLAGS) -shared -Wl,-soname,$(SONAME) -o $(LIB)/$(REALNAME) $^
	ln -sf $(REALNAME) $(LIB)/$(SONAME)
	ln -sf $(SONAME) $(LIB)/$(DEVLINK)




#Linking Shared Library to the main object file 
main: $(SHARED_LIB) main.cpp
	#$(CXX) $(CXXFLAGS) main.cpp -L$(LIB) -lcalc $(LDFLAGS) -o $@
	$(CXX) $(CXXFLAGS) main.cpp -L$(LIB) -lcalc $(LDFLAGS) -Wl,-rpath-link,$(LIB) -o $@

# Create directories if not existed
$(BUILD):
	mkdir -p $(BUILD)

$(LIB):
	mkdir -p $(LIB)

#To clean 
.PHONY: clean
clean:
	rm -rf $(BUILD) $(LIB) main
