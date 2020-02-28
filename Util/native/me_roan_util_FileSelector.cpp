#include <windows.h>
#include <shobjidl.h>
#include <jni.h>
#include <stdio.h>
#include "me_roan_util_FileSelector.h"
#include <sstream>

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

//Shows a dialog according to the passed flags (see definitions)
LPWSTR showDialog(int flags, long types, long typec){
	printf("%s\n", "A1");
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

		printf("%s\n", "A2");
		if(SUCCEEDED(hr)){
			if((flags & FOLDERS) > 0){
				DWORD options;
				if(SUCCEEDED(dialog->GetOptions(&options))){
					dialog->SetOptions(options | FOS_PICKFOLDERS);
				}
			}
			
			if(typec != 0){
				filters = (COMDLG_FILTERSPEC*)malloc(typec * sizeof(COMDLG_FILTERSPEC));
				if(filters != NULL){
					int idx = 0;
					for(long i = 0; idx < typec; i++){
						printf("loop %d - %d - %d/%d\n", i, idx, types, typec);
						if((types & (1 << i)) != 0){
							filters[idx++] = extensions[i].ext;
						}
					}
					dialog->SetFileTypes(typec, filters);
				}
			}
			
			printf("This is a test string\n");
			fflush(stdout);
			hr = dialog->Show(NULL);
			if(SUCCEEDED(hr)){
				IShellItem *item;
				hr = dialog->GetResult(&item);
				if(SUCCEEDED(hr)){
					printf("%s\n", "A4");
					item->GetDisplayName(SIGDN_FILESYSPATH, &path);
					item->Release();

					UINT index;
					hr = dialog->GetFileTypeIndex(&index);
					printf("%u\n", index);
					fflush(stdout);

					//TODO
					//path = (std::wstring(path) + L"." + std::wstring(extensions[index - 1].def)).c_str;
				}
			}
			dialog->Release();
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
	return toString(env, showDialog(FILES | OPEN, types, typec));
}

//Native subroutine for me.roan.util.FileSelector#showNativeFolderOpen
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFolderOpen(JNIEnv *env, jclass obj){
	return toString(env, showDialog(FOLDERS | OPEN, 0, 0));
}

//Native subroutine for me.roan.util.FileSelector#showNativeFileSave
JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFileSave(JNIEnv *env, jclass obj, jint types, jint typec){
	return toString(env, showDialog(FILES | SAVE, types, typec));
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