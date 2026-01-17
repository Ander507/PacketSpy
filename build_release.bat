@echo off
echo Building PacketSpy for Release...
call gradlew build
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)

if not exist "release" mkdir release
copy "build\libs\PacketSpy-*-sources.jar" "release\"
copy "build\libs\PacketSpy-*.jar" "release\"

echo.
echo Build successful! Files copied to 'release' folder.
pause

