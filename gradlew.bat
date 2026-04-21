@echo off
setlocal

where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)

set GRADLE_VERSION=8.14.3
set PROJECT_DIR=%~dp0
set DIST_DIR=%PROJECT_DIR%\.gradle\wrapper\dists
set GRADLE_HOME=%DIST_DIR%\gradle-%GRADLE_VERSION%
set GRADLE_BIN=%GRADLE_HOME%\bin\gradle.bat
set GRADLE_ZIP=%DIST_DIR%\gradle-%GRADLE_VERSION%-bin.zip
set GRADLE_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip

if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

if not exist "%GRADLE_BIN%" (
  if not exist "%GRADLE_ZIP%" (
    echo Downloading Gradle %GRADLE_VERSION%...
    powershell -Command "Invoke-WebRequest -Uri '%GRADLE_URL%' -OutFile '%GRADLE_ZIP%'"
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
  )
  echo Unpacking Gradle %GRADLE_VERSION%...
  powershell -Command "Expand-Archive -LiteralPath '%GRADLE_ZIP%' -DestinationPath '%DIST_DIR%' -Force"
  if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
)

call "%GRADLE_BIN%" %*
exit /b %ERRORLEVEL%
