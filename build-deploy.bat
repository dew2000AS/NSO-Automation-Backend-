@echo off
echo ========================================
echo Building HSB Backend...
echo ========================================

cd /d "%~dp0"

REM Clean and build the project
call mvn clean package -DskipTests

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build successful! Deploying to WildFly...
echo ========================================

REM Copy WAR to WildFly deployments
copy /Y "target\HSB.war" "C:\wildfly-33.0.2.Final\standalone\deployments\"

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo DEPLOYMENT COPY FAILED!
    echo ========================================
    pause
    exit /b 1
)

echo.
echo ========================================
echo Deployment complete!
echo HSB.war copied to WildFly deployments
echo ========================================
pause
