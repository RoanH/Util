#include <windows.h>
#include <shobjidl.h>
#include <jni.h>
#include "me_roan_util_FileSelector.h"

/**
 * Show a save dialog, mutually exclusive with OPEN.
 */
#define SAVE 2
/**
 * Show an open dialog, mutually exclusive with SAVE.
 */
#define OPEN 4
/**
 * Selection of files, mutually exclusive with FOLDERS.
 */
#define FILES 8
/**
 * Selection of folders, mutually exclusive with FILES.
 */
#define FOLDERS 16

/**
 * Struct representing a registered file extension.
 */
typedef struct{
	/**
	 * File dialog filter.
	 */
	COMDLG_FILTERSPEC ext;
	/**
	 * Default extension to use in case the filter accepts multiple extensions.
	 */
	LPWSTR def;
} FILE_TYPE;

/**
 * Array of registered extensions.
 */
FILE_TYPE *extensions;
/**
 * Number of registered extensions.
 */
int ext_num = 0;

/**
 * Shows a dialog according to the passed flags (see definitions).
 * @param flags Determines whether to show a SAVE or OPEN dialog 
 *        and FILES or FOLDER selection.
 * @param types Bitwise combination of file extension filters to enable.
 * @param typec Number of bits set in types.
 * @param fname Default save file name, NULL for an OPEN dialog.
 * @return The path to the selected file or folder.
 */
LPWSTR showDialog(int flags, jlong types, jint typec, LPWSTR fname){
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
						for(jlong i = 0; idx < typec; i++){
							if((types & (jlong(1) << i)) != 0){
								filters[idx++] = extensions[i].ext;
								dialog->SetDefaultExtension(extensions[i].def);
							}
						}
						dialog->SetFileTypes(typec, filters);
						dialog->SetOptions(options | FOS_STRICTFILETYPES);
					}
				}

				if(fname != NULL){
					dialog->SetFileName(fname);
				}

				hr = dialog->Show(NULL);
				if(SUCCEEDED(hr)){
					IShellItem *item;
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

/**
 * Converts a LPWSTR to a jstring using the given JNI environment.
 * @param env JNI environment.
 * @param The LPWSTR to convert.
 * @return The converted LPWSTR as a jstring.
 */
jstring toString(JNIEnv *env, LPWSTR data){
	if(data == NULL){
		return NULL;
	}else{
		int len = WideCharToMultiByte(CP_UTF8, 0, data, -1, NULL, 0, NULL, NULL);
		if(len == 0){
			CoTaskMemFree(data);
			return NULL;
		}

		char *utf8 = (char*)malloc(len);

		len = WideCharToMultiByte(CP_UTF8, 0, data, -1, utf8, len, NULL, NULL);
		if(len == 0){
			CoTaskMemFree(data);
			free(utf8);
			return NULL;
		}

		jstring str = env->NewStringUTF(utf8);
		free(utf8);
		CoTaskMemFree(data);
		return str;
	}
}

/**
 * Native subroutine for me.roan.util.FileSelector#showNativeFileOpen
 * @param env JNI environment.
 * @param obj Calling class.
 * @param types Bitwise combination of file extension filter to enable.
 * @param typec Number of bits set in 'types', if 0 then no filters will be used.
 * @return The file path of the file to open.
 */
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFileOpen(JNIEnv *env, jclass obj, jlong types, jint typec){
	return toString(env, showDialog(FILES | OPEN, types, typec, NULL));
}

/**
 * Native subroutine for me.roan.util.FileSelector#showNativeFolderOpen
 * @param env JNI environment.
 * @param obj Calling class.
 * @return The folder file path to open.
 */
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFolderOpen(JNIEnv *env, jclass obj){
	return toString(env, showDialog(FOLDERS | OPEN, 0, 0, NULL));
}

/**
 * Native subroutine for me.roan.util.FileSelector#showNativeFileSave
 * @param env JNI environment.
 * @param obj Calling class.
 * @param type The ID of the extension to use, 0 for no restriction.
 * @param name The default name for the saved file.
 * @return The file save location.
 */
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFileSave(JNIEnv *env, jclass obj, jlong type, jstring name){
	return toString(env, showDialog(FILES | SAVE, type, type == 0 ? 0 : 1, (LPWSTR)env->GetStringChars(name, FALSE)));
}

/**
 * Native subroutine for me.roan.util.FileSelector#registerFileExtension
 * @param env JNI environment.
 * @param obj Calling class.
 * @param name Description of the extension to register.
 * @param ext File extension filter string.
 * @param def Default extension to use for files that match the filter.
 * @return The ID of the newly registered extension.
 */
JNIEXPORT jlong JNICALL Java_me_roan_util_FileSelector_registerNativeFileExtension(JNIEnv* env, jclass obj, jstring name, jstring ext, jstring def){
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

	return jlong(1) << (ext_num - 1);
}