#include <windows.h>
#include <shobjidl.h>
#include <jni.h>
#include <stdio.h>
#include "me_roan_util_FileSelector.h"
#include <sstream>
#include <iostream>

//Save dialog
#define SAVE 2
//Open dialog
#define OPEN 4
//File selection
#define FILES 8
//Folder selection
#define FOLDERS 16

typedef struct{
	COMDLG_FILTERSPEC ext;
	LPWSTR def;
} FILE_TYPE;

FILE_TYPE *extensions;
int ext_num = 0;

/**
 * Shows a dialog according to the passed flags (see definitions).
 * @param flags Determines whether to show a SAVE or OPEN dialog 
 *        and FILES or FOLDER selection.
 * @param types Bitwise combination of file extension filters to enable.
 * @param typec Number of bits set in types.
 * @param fname Default save file name, NULL for an OPEN dialog.
 */
LPWSTR showDialog(int flags, long types, long typec, LPWSTR fname){
	LPWSTR path = NULL;
	HRESULT hr = CoInitializeEx(NULL, COINIT_APARTMENTTHREADED | COINIT_DISABLE_OLE1DDE);
	if(SUCCEEDED(hr)){
		IFileDialog *dialog;
		COMDLG_FILTERSPEC *filters = NULL;
		if((flags & SAVE) > 0){
			hr = CoCreateInstance(CLSID_FileSaveDialog, NULL, CLSCTX_ALL, IID_IFileSaveDialog, reinterpret_cast<void**>(&dialog));
		}else{
			hr = CoCreateInstance(CLSID_FileOpenDialog, NULL, CLSCTX_ALL, IID_IFileOpenDialog, reinterpret_cast<void**>(&dialog));
		}

		if(SUCCEEDED(hr)){
			DWORD options;
			if(SUCCEEDED(dialog->GetOptions(&options))){
				if((flags & FOLDERS) > 0){
					dialog->SetOptions(options | FOS_PICKFOLDERS);
				}

				if(typec != 0){
					filters = (COMDLG_FILTERSPEC*)malloc(typec * sizeof(COMDLG_FILTERSPEC));
					if(filters != NULL){
						int idx = 0;
						for(long i = 0; idx < typec; i++){
							if((types & (1 << i)) != 0){
								filters[idx++] = extensions[i].ext;
							}
						}
						dialog->SetFileTypes(typec, filters);
						dialog->SetOptions(options | FOS_STRICTFILETYPES);
					}
				}

				//TODO pass as argument
				//hr = dialog->SetFileName(L"test.png");

				//Force an extension, this being the default
				dialog->SetDefaultExtension(L"png");

				hr = dialog->Show(NULL);
				if(SUCCEEDED(hr)){
					IShellItem* item;
					hr = dialog->GetResult(&item);
					if(SUCCEEDED(hr)){
						item->GetDisplayName(SIGDN_FILESYSPATH, &path);
						item->Release();
					}
				}
				dialog->Release();
			}
		}
		CoUninitialize();
		if(filters != NULL){
			free(filters);
		}
	}
	return path;
}

//Converts a LPWSTR to a jstring using the given JNI environment
jstring toString(JNIEnv *env, LPWSTR data){
	if(data == NULL){
		return NULL;
	}else{
		int len = WideCharToMultiByte(CP_UTF8, 0, data, -1, NULL, 0, NULL, NULL);
		if(len == 0){
			return NULL;
		}

		char *utf8 = (char*)malloc(len);

		len = WideCharToMultiByte(CP_UTF8, 0, data, -1, utf8, len, NULL, NULL);
		if(len == 0){
			return NULL;
		}

		jstring str = env->NewStringUTF(utf8);
		free(utf8);
		CoTaskMemFree(data);
		return str;
	}
}

//Native subroutine for me.roan.util.FileSelector#showNativeFileOpen
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFileOpen(JNIEnv *env, jclass obj, jint types, jint typec){
	return toString(env, showDialog(FILES | OPEN, types, typec, NULL));
}

//Native subroutine for me.roan.util.FileSelector#showNativeFolderOpen
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFolderOpen(JNIEnv *env, jclass obj){
	return toString(env, showDialog(FOLDERS | OPEN, 0, 0, NULL));
}

//Native subroutine for me.roan.util.FileSelector#showNativeFileSave
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFileSave(JNIEnv *env, jclass obj, jint type){
	return toString(env, showDialog(FILES | SAVE, type, type == 0 ? 0 : 1, NULL));//TODO
}

//Native subroutine for me.roan.util.FileSelector#registerFileExtension
JNIEXPORT jint JNICALL Java_me_roan_util_FileSelector_registerNativeFileExtension(JNIEnv* env, jclass obj, jstring name, jstring ext, jstring def){
	if(ext_num == 0){
		extensions = (FILE_TYPE*)malloc(sizeof(FILE_TYPE));
		if(extensions == NULL){
			return -1;
		}else{
			ext_num = 1;
		}
	}else{
		ext_num++;
		FILE_TYPE *new_extensions = (FILE_TYPE*)realloc(extensions, ext_num * sizeof(FILE_TYPE));
		if(new_extensions == NULL){
			return -1;
		}else{
			extensions = new_extensions;
		}
	}
	
	extensions[ext_num - 1].ext = {
		(wchar_t*)env->GetStringChars(name, FALSE),
		(wchar_t*)env->GetStringChars(ext, FALSE)
	};
	extensions[ext_num - 1].def = (LPWSTR)env->GetStringChars(name, FALSE);

	return ext_num;
}