@echo off
setlocal
cd /d "%~dp0"
rem Builds one PacketSpy jar per target listed in versions.json and copies them into release\

for /f "usebackq delims=" %%T in (`powershell -NoProfile -Command "(Get-Content -Raw versions.json | ConvertFrom-Json).targets | ForEach-Object { $_.name }"`) do (
    echo.
    echo === Building PacketSpy for Minecraft %%T ===
    call "%~dp0gradlew.bat" releaseJar -Ptarget=%%T
    if errorlevel 1 goto :fail
)

echo.
echo Build successful! Jars are in the 'release' folder.
pause
exit /b 0

:fail
echo.
echo Build failed!
pause
exit /b 1
