/**
* If not stated otherwise in this file or this component's LICENSE
* file the following copyright and licenses apply:
*
* Copyright 2024 RDK Management
*
* Licensed under the Apache License, Version 2.0 (the "License");
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
**/

#pragma once

#include <fstream>
#include <iostream>
#include <regex>

#include "UtilsString.h"
#define READ_BUFFER_SIZE 1024
namespace Utils {

/**
 * @brief Read the property value from the given file based on the provided property name.
 * @param[in] filename - The name of the file from which to read the properties.
 * @param[in] property -  The name of the property to search for in the file.
 * @param[out] propertyValue - The value of the property will be stored in this string.
 * @return          bool  True if the property is found and successfully read, false otherwise.
 */
inline bool readPropertyFromFile(const char* filename, const string& property, string& propertyValue)
{
    string line = "";
    bool found = false;
    std::ifstream file(filename);
    propertyValue = "";
    if (file.is_open())
    {
        while (std::getline(file, line))
        {
            // Skip lines that start with '#' (single-line comments)
            if (!line.empty() && line[0] == '#')
            {
                continue;
            }
            if (line.find(property + " ") == 0 || line.find(property + "=") == 0)
            {
                string propertyContent = line.substr(line.find("=") + 1);

                // If the property value starts with '$',recursively expand it
                if (!propertyContent.empty() && propertyContent[0] == '$')
                {
                    string expandedProperty = propertyContent.substr(1);
                    if (readPropertyFromFile(filename, expandedProperty, propertyValue))
                    {
                        found = true;
                        break;
                    }
                    else
                    {
                        LOGERR("Failed to find expanded property: %s", expandedProperty.c_str());
                    }
                }
                else
                {
                    // If it does not start with '$', set propertyValue directly
                    propertyValue = propertyContent;
                    if (!propertyValue.empty())
                    {
                        // Remove new line character from end of the string if it exists
                        if ((propertyValue.back() == '\r') || (propertyValue.back() == '\n'))
                        {
                            propertyValue.pop_back();
                        }
                    }
                    found = true;
                    break;
                }
            }
        }
    }
    else
    {
        LOGERR("File is not open");
    }

    // If the property was not found, set the propertyValue to an empty string
    if (!found)
    {
       LOGERR("Variable value is empty");
    }

    return found;
}

/**
 * @brief Read the content of a file and store it in the provided string.
 * @param[in] filename - The name of the file to read.
 * @param[out] content - The content of the file will be stored in this string.
 * @return  bool  True if the file is successfully read and its content is stored in 'content', false otherwise.
 */
inline bool readFileContent(const char* filename, string& content)
{
    char buffer[READ_BUFFER_SIZE];
    FILE* file = fopen(filename, "r");
    bool found = false;

    if (file)
    {
        while (fgets(buffer, sizeof(buffer), file) != NULL)
        {
            content += buffer;
            found = true;
        }
        fclose(file);

    }
    else
    {
        LOGERR("Failed to open the file");
    }

    return found;
}


/**
 * @brief Check if a given path corresponds to a regular file.
 * @param[in] path - The path to check.
 * @return bool - True if the path corresponds to a regular file, false otherwise.
 */
inline bool isRegularFile(const string& path)
{
    struct stat st;
    if (stat(path.c_str(), &st) == 0)
    {
        return S_ISREG(st.st_mode);
    }
    return false;
}


inline bool searchFilesRec(std::vector<std::string> &pathList, unsigned int currentDepth, const std::list<string>& exclusions, string& result)
{
    int count = 0;

    if (pathList.size() == 0)
    {
        LOGERR("Empty path");
        return false;
    }

    std::string inputPath = "/";
    for (unsigned int n = 0; n < currentDepth; n++)
    {
        inputPath += pathList[n];
        inputPath += "/";
    }
    
    std::string currentPath = pathList[currentDepth];

    if (currentPath.find('*') != std::string::npos || currentPath.find('?') != std::string::npos)
    {
        // Process files and directories in the current directory
        DIR *dir = opendir(inputPath.c_str());
        if (!dir)
        {
            LOGERR("Failed to open the directory '%s'", inputPath.c_str());
            return false;
        }

        std::string pattern_s = currentPath;
        size_t pos = 0;
        while ((pos = pattern_s.find('*', pos)) != std::string::npos)
        {
            pattern_s.replace(pos, 1, "[^\\n]*");
            pos += 6;
        }

        pos = 0;
        while ((pos = pattern_s.find('?', pos)) != std::string::npos)
        {
            pattern_s.replace(pos, 1, "[^\\n]{1}");
            pos += 8;
        }

        std::regex pattern(pattern_s.c_str());
        std::smatch matches;

        struct dirent *entry;
        while ((entry = readdir(dir)))
        {
            string fileName = entry->d_name;

            if (fileName == "." || fileName == "..")
            {
                continue;
            }

            if (std::regex_search(fileName, matches, pattern))
            {
                if (std::find(exclusions.begin(), exclusions.end(), fileName) == exclusions.end())
                {
                    if (currentDepth >= pathList.size() - 1)
                    {
                        inputPath += fileName;
                        if (access(inputPath.c_str(), F_OK) == 0)
                        {
                            result += inputPath + "\n";

                            count++;
                            if (count >= 10)
                                break; // Stop when count reaches 10
                        }
                    }
                    else
                    {
                        pathList[currentDepth] = fileName;
                        searchFilesRec(pathList, currentDepth + 1, exclusions, result);
                    }
                }
            }
        }
        closedir(dir);
    }
    else
    {
        if (currentDepth >= pathList.size() - 1 )
        {
            inputPath += currentPath;
            if (access(inputPath.c_str(), F_OK) == 0)
            {
                result += inputPath + "\n";
                return true;
            }
        }
        else
        {
            searchFilesRec(pathList, currentDepth + 1, exclusions, result);
        }
    }

    return true;
}

/**
 * @brief Recursively search for files and directories in a given directory path, with depth limits and exclusions.
 * @param[in] inputPath - The directory path to start the search from.
 * @param[in] maxDepth - The maximum depth of subdirectories to search (0 for no limit).
 * @param[in] minDepth - The minimum depth of subdirectories to start searching from (0 to start from inputPath).
 * @param[in] exclusions - A list of paths to exclude from the search.
 * @param[out] result - The search results will be stored in this string.Results are capped at 10.
 * @return bool - True if the search operation is successful, false otherwise.
 */
inline bool searchFiles(string& inputPath, int maxDepth, int minDepth, const std::list<string>& exclusions, string& result)
{
    std::vector<std::string> pathList;
    Utils::String::split(pathList, inputPath, "/");
    pathList.erase(pathList.begin(), pathList.begin() + 1);
    
    return searchFilesRec(pathList, 0, exclusions,result);
}

/**
 * @brief Process a string containing variables and replace them with corresponding values.
 *
 * @param[in] input - The input string containing variables; delimited by ' ' or '/'
 * @param[in] filePath - The path to the file containing property values.
 * @param[out] expandedString - The string with variables replaced by values.
 * @return bool - True if the processing and replacement are successful, false otherwise.
 */
inline bool ExpandPropertiesInString(const char* input, const char* filePath, string & expandedString)
{
    const char* variablePos = strchr(input, '$');
    while (variablePos)
    {
        expandedString.assign(input, variablePos - input);
        const char* endPos = strpbrk(variablePos, " /");
        if (endPos)
        {
            size_t variableLength = endPos - variablePos - 1;
            char variable[variableLength + 1];
            strncpy(variable, variablePos + 1, variableLength);
            variable[variableLength] = '\0';

            string tempPropertyValue;
            if (readPropertyFromFile(filePath, variable, tempPropertyValue))
            {
                const char* propertyValue = tempPropertyValue.c_str();
                expandedString += tempPropertyValue;
                variablePos += strlen(propertyValue);
            }
            else
            {
                LOGERR("Variable '%s' not found or error reading value.\n", variable);
                return false;
            }
         }

        else
        {
            endPos = variablePos + 1;
        }
        variablePos = strchr(endPos, '$');
        if (variablePos)
        {
            expandedString.append(endPos, variablePos - endPos);
        }
        else
        {
            expandedString += endPos;
        }
    }

    return true;
}

}
