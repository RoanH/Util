#include <windows.h>
#include <shobjidl.h>
#include <jni.h>
#include <stdio.h>
#include "me_roan_util_FileSelector.h"

//Save dialog
#define SAVE 2
//Open dialog
#define OPEN 4
//File selection
#define FILES 8
//Folder selection
#define FOLDERS 16

COMDLG_FILTERSPEC *extensions;
int ext_num = 0;

//Shows a dialog according to the passed flags (see definitions)
LPWSTR showDialog(int flags){
	LPWSTR path = NULL;
	HRESULT hr = CoInitializeEx(NULL, COINIT_APARTMENTTHREADED | COINIT_DISABLE_OLE1DDE);
	if(SUCCEEDED(hr)){
		IFileDialog *dialog;
		if((flags & SAVE) > 0){
			hr = CoCreateInstance(CLSID_FileSaveDialog, NULL, CLSCTX_ALL, IID_IFileSaveDialog, reinterpret_cast<void**>(&dialog));
		}else{
			hr = CoCreateInstance(CLSID_FileOpenDialog, NULL, CLSCTX_ALL, IID_IFileOpenDialog, reinterpret_cast<void**>(&dialog));
		}

		if(SUCCEEDED(hr)){
			if((flags & FOLDERS) > 0){
				DWORD options;
				if(SUCCEEDED(dialog->GetOptions(&options))){
					dialog->SetOptions(options | FOS_PICKFOLDERS);
				}
			}

			COMDLG_FILTERSPEC spec[] = {
				{L"Image files", L"*.jpg"}
			};

			dialog->SetFileTypes(1, spec);
			
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
		CoUninitialize();
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
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFileOpen(JNIEnv *env, jclass obj){
	return toString(env, showDialog(FILES | OPEN));
}

//Native subroutine for me.roan.util.FileSelector#showNativeFolderOpen
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFolderOpen(JNIEnv *env, jclass obj){
	return toString(env, showDialog(FOLDERS | OPEN));
}

//Native subroutine for me.roan.util.FileSelector#showNativeFileSave
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFileSave(JNIEnv *env, jclass obj){
	return toString(env, showDialog(FILES | SAVE));
}

//Native subroutine for me.roan.util.FileSelector#registerFileExtension
JNIEXPORT jint JNICALL Java_me_roan_util_FileSelector_registerFileExtension(JNIEnv* env, jclass obj, jstring name, jstring ext){
	if(ext_num == 0){
		extensions = (COMDLG_FILTERSPEC*)malloc(sizeof(COMDLG_FILTERSPEC));
		if(extensions == NULL){
			return -1;
		}else{
			ext_num = 1;
		}
	}else{
		ext_num++;
		COMDLG_FILTERSPEC *new_extensions = (COMDLG_FILTERSPEC*)realloc(extensions, ext_num * sizeof(COMDLG_FILTERSPEC));
		if(new_extensions == NULL){
			return -1;
		}else{
			extensions = new_extensions;
		}
	}
	
	extensions[ext_num - 1] = {
		(wchar_t*)env->GetStringChars(name, FALSE),
		(wchar_t*)env->GetStringChars(ext, FALSE)
	};

	return -1;
}