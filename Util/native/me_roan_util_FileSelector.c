#include <windows.h>
#include <jni.h>
#include <stdio.h>
#include "me_roan_util_FileSelector.h"

CHAR FileName[MAX_PATH];

int showOpenDialog(int options){
	OPENFILENAME ofn;
	memset(&ofn, 0, sizeof(ofn));
	ofn.lStructSize = sizeof(ofn);
	ofn.lpstrFile = FileName;
	ofn.nMaxFile = MAX_PATH;
	ofn.lpstrTitle = "Open";
	ofn.Flags = OFN_FILEMUSTEXIST | OFN_NOREADONLYRETURN | OFN_PATHMUSTEXIST | OFN_NOVALIDATE;
	if (GetOpenFileName(&ofn)){
		return 0;
	}else{
		return 1;
	}
}

JNIEXPORT jstring JNICALL Java_me_roan_util_FileSelector_showNativeFileOpen(JNIEnv *env, jclass obj){
	showOpenDialog(0);
	return NULL;
}