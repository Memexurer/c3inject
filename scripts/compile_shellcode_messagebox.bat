@echo off
set STUB_PATH=C:\Users\azeroy\IdeaProjects\c3inject\runner\build\libs\runner-1.0-SNAPSHOT.jar
set OUTPUT_PATH=C:\Users\azeroy\IdeaProjects\c3inject\out.jar
set SHELLCODE_PATH=messagebox.bin

set TRANSFORMER_JAR_PATH=C:\Users\azeroy\IdeaProjects\c3inject\transformer\build\libs\transformer-1.0-SNAPSHOT.jar
set JAVA_16_PATH=C:\Users\azeroy\.jdks\corretto-16.0.2\bin\java.exe
rem 0 - shellcode path
rem 1 - stub path
rem 2 - output

%JAVA_16_PATH% -jar %TRANSFORMER_JAR_PATH% %SHELLCODE_PATH% %STUB_PATH% %OUTPUT_PATH%