@echo off
set "SPINTALE_JDK17_HOME=C:\Users\v1589\.jdks\ms-17.0.19"

if exist "%SPINTALE_JDK17_HOME%\bin\java.exe" (
    set "JAVA_HOME=%SPINTALE_JDK17_HOME%"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
) else (
    echo [WARN] JDK 17 not found: %SPINTALE_JDK17_HOME%
)
