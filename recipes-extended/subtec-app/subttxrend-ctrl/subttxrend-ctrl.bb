##############################################################################
# Copyright © 2024 RDK Management.
# File adapted from subttxrend-app.bb by torben.thellefsen@sky.uk
# All rights reserved.
# Licensed by RDK Management, LLC under the terms of the RDK license.
# 
# ============================================================================
# Liberty Global B.V. CONFIDENTIAL AND PROPRIETARY
# ============================================================================
# This file (and its contents) are the intellectual property of Liberty Global B.V.
# It may not be used, copied, distributed or otherwise disclosed in whole or in
# part without the express written permission of Liberty Global B.V.
# The RDK License agreement constitutes express written consent by Liberty Global.
# ============================================================================
# This software is the confidential and proprietary information of Liberty Global B.V.
# ("Confidential Information"). You shall not disclose this source code or
# such Confidential Information and shall use it only in accordance with the
# terms of the license agreement you entered into.
#
# LIBERTY GLOBAL B.V. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
# SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT
# LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
# PARTICULAR PURPOSE, OR NON-INFRINGEMENT. LIBERTY GLOBAL B.V. SHALL NOT BE LIABLE FOR
# ANY DAMAGES SUFFERED BY LICENSEE NOR SHALL THEY BE RESPONSIBLE AS A RESULT
# OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
##############################################################################
LICENSE = "RDK & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=626bbc2ac7625da5b97fcb8a24bd88b3"
DEPENDS = ""
DEPENDS += " subttxrend-common subttxrend-socksrc subttxrend-dbus"
DEPENDS += " subttxrend-gfx subttxrend-dvbsub subttxrend-ttxt subttxrend-protocol"
DEPENDS += " subttxrend-ttml subttxrend-scte subttxrend-cc subttxrend-webvtt"

DEPENDS:append = " virtual/egl "

SRC_URI="${CMF_GITHUB_ROOT}/subtec-app;${CMF_GITHUB_SRC_URI_SUFFIX}"
S = "${WORKDIR}/git/subttxrend-ctrl"

#
# pkgconfig         - pkgconfig used in cmake (adds dependency)
# cmake             - cmake build system used
#

inherit pkgconfig cmake coverity
