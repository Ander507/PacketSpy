@echo off
setlocal
cd /d "%~dp0"
rem Checks every target against every Minecraft version it claims to support (see versions.json):
rem   1. builds each target's jar into release\
rem   2. compiles the code against each version, and checks that the built jar only uses
rem      Minecraft classes/methods/fields that exist in that version.
rem Run this after changing code or adding a version: a failure means that version needs its own target.

set FAILED=
for /f "usebackq delims=" %%T in (`powershell -NoProfile -Command "(Get-Content -Raw versions.json | ConvertFrom-Json).targets | ForEach-Object { $_.name }"`) do (
    echo === Building target %%T ===
    call "%~dp0gradlew.bat" releaseJar -Ptarget=%%T -q
    if errorlevel 1 (
        echo FAILED: target %%T does not build
        set FAILED=1
    )
)

for /f "usebackq tokens=1,2" %%A in (`powershell -NoProfile -Command "(Get-Content -Raw versions.json | ConvertFrom-Json).targets | ForEach-Object { $t = $_.name; $_.versions | ForEach-Object { $t + ' ' + $_ } }"`) do (
    echo === Target %%A on Minecraft %%B ===
    call "%~dp0gradlew.bat" compileClientJava verifyJar -Ptarget=%%A -Pmc=%%B -q
    if errorlevel 1 (
        echo FAILED: target %%A does not work on Minecraft %%B
        set FAILED=1
    )
)

echo.
if defined FAILED (
    echo Some versions failed, see above.
    pause
    exit /b 1
)
echo All versions OK.
pause
exit /b 0
