#include <windows.h>
#include <shobjidl.h>
#include <jni.h>
#include <stdio.h>
#include "me_roan_util_FileSelector.h"

#define SAVE 2
#define OPEN 4
#define FILES 8
#define FOLDERS 16

LPWSTR showDialog(int flags){
	LPWSTR path = NULL;
	HRESULT hr = CoInitializeEx(NULL, COINIT_APARTMENTTHREADED | COINIT_DISABLE_OLE1DDE);
	if(SUCCEEDED(hr)){
		IFileDialog* dialog;
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
		CoUninitialize();
	}
	return path;
}

jstring toString(JNIEnv *env, LPWSTR data){
	if(data == NULL){
		return NULL;
	}else{

		wprintf(data);
		wprintf(L"“ú–{Œê");

		//jstring str = env->NewStringUTF(data);
		CoTaskMemFree(data);
		return NULL;
	}
}

JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFileOpen(JNIEnv *env, jclass obj){
	return toString(env, showDialog(FILES | OPEN));
}

JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFolderOpen(JNIEnv *env, jclass obj){
	return toString(env, showDialog(FOLDERS | OPEN));
}

JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFileSave(JNIEnv *env, jclass obj){
	return toString(env, showDialog(FILES | SAVE));
}