@echo off
set /p JAVA_HOME=<java_home.txt
echo using %JAVA_HOME%
%JAVA_HOME%\bin\java.exe -jar ../out.jar
del *.log
pause