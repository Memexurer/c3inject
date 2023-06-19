@echo off
set /p JAVA_HOME=<java_home.txt
echo using %JAVA_HOME%
%JAVA_HOME%\bin\java.exe -Dfile.encoding=windows-1250 -Duser.country=PL -Duser.language=pl -Duser.variant -cp C:\Users\azeroy\IdeaProjects\c3inject\helfy-jvm\build\classes\java\main;C:\Users\azeroy\IdeaProjects\c3inject\helfy-jvm\build\resources\main helfy.PopulatePlatformVariables