/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2016 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
#include <stdio.h>
#include <mfrMgr.h>
#include <mfrTypes.h>

mfrError_t mfrGetSerializedData(mfrSerializedType_t param, mfrSerializedData_t *data)
{
	return mfrERR_NONE;
}

mfrError_t mfrDeletePDRI()
{
	return mfrERR_NONE;
}

mfrError_t mfrScrubAllBanks()
{
	return mfrERR_NONE;
}

mfrError_t mfrWriteImage(const char *str,  const char *str1, 
	mfrImageType_t imageType,  mfrUpgradeStatusNotify_t upgradeStatus)
{
	return mfrERR_NONE;
}

mfrError_t mfr_init()
{
	return mfrERR_NONE;
}
