cd native\Util

rmdir /S /Q Release
rmdir /S /Q x64

"C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\Common7\IDE\devenv.exe" Util.sln /build "Release|x64"
"C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\Common7\IDE\devenv.exe" Util.sln /build "Release|x86"

cd ..\..

copy /y native\Util\x64\Release\Util.dll src\me\roan\util\lib\amd64\Util.dll
copy /y native\Util\Release\Util.dll src\me\roan\util\lib\x86\Util.dll