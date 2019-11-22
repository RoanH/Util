#include <windows.h>
#include <shobjidl.h>
#include <jni.h>
#include <stdio.h>
#include "me_roan_util_FileSelector.h"

#define SAVE 2
#define OPEN 4
#define FILES 8
#define FOLDERS 16

LPWSTR showOpenFileDialog(){
	HRESULT hr = CoInitializeEx(NULL, COINIT_APARTMENTTHREADED | COINIT_DISABLE_OLE1DDE);
	if(SUCCEEDED(hr)){
		IFileSaveDialog* openDialog;
		hr = CoCreateInstance(CLSID_FileSaveDialog, NULL, CLSCTX_ALL, IID_IFileSaveDialog, reinterpret_cast<void**>(&openDialog));

		if(SUCCEEDED(hr)){
			DWORD options;
			if(SUCCEEDED(openDialog->GetOptions(&options))){
				openDialog->SetOptions(options);//
			}

			// Show the Open dialog box.
			hr = openDialog->Show(NULL);

			// Get the file name from the dialog box.
			if(SUCCEEDED(hr)){
				IShellItem* pItem;
				hr = openDialog->GetResult(&pItem);
				if(SUCCEEDED(hr)){
					LPWSTR pszFilePath;
					hr = pItem->GetDisplayName(SIGDN_FILESYSPATH, &pszFilePath);

					CoTaskMemFree(pszFilePath);
					wprintf(pszFilePath);
					pItem->Release();
				}
			}
			openDialog->Release();
		}
		CoUninitialize();
	}
	return 0;
}

JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFileOpen(JNIEnv *env, jclass obj){
	//showOpenDialog(0);
	showOpenFileDialog();



	return NULL;
}

JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFolderOpen(JNIEnv *env, jclass obj){



	return NULL;
}