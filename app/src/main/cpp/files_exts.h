#ifndef FILE_EXTS_H
#define FILE_EXTS_H

#include <stdbool.h>
#include <stdint.h>

#define FILE_TYPE_IMAGE 0
#define FILE_TYPE_VIDEO 1
#define FILE_TYPE_AUDIO 2
#define FILE_TYPE_DOCUMENT 3

// get an array of extensions for a file type.
// the returned value must be freed
char **getFileExtsForType(int32_t type, int *size);

// this function frees exts.
// exts must be allocated dynamically.
bool isFileExtinArray(const char *filename, char **exts, int exts_len);

#endif