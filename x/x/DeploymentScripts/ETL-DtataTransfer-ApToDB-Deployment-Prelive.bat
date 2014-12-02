@echo off 

::Deployment of ETL-DtataTransfer - ApToDB-Prelive

::ETL service names
set serviceName1=MR_PRELIVE-ApDataExtractToDB

::App name - app will be deployed with this name
set warName=mx

set appName=ApToDB

:: tomcat server locations
set tomcatLocation1=D:\ETL\mr_prelive\ApToDB


::Get date as yyyymmdd_hhmm 
::set date=%date:~-4%%date:~7,2%%date:~4,2%_%time:~0,2%%time:~3,2%

echo ********************ApDataExtractToDB-Prelive********************
echo Please read/confirm below settings/configurations before proceeding
echo Application to be deployed: %appName%
echo Tomcat1 location : %tomcatLocation1%
echo Service1 : %serviceName1%

set /P continue=Confirm to continue(y/n):

if "%continue%"=="y" (GOTO Confirm) else  (GOTO manualDeployment)

:Confirm
::Step 1 - Stop the service

echo ETL-DtataTransfer - ApToDB-Prelive
pause
set date=%date:~-4%%date:~7,2%%date:~4,2%_%time:~0,5%
set date=%date::=_%
set date=%date: =0%

set logFile=%appName%-Deployment.log
echo Application to be deployed: %appName% >> %logFile%
echo Tomcat1 location : %tomcatLocation1% >> %logFile%
echo Service1 : %serviceName1% >> %logFile%

echo Step 1 - Stop the services 

sc stop %serviceName1%
echo Step 1 - Stop the services
echo Step 1 - Stop the services >> %logFile%

ping -n 5 127.0.0.1 > NUL

::Check if the service is stopped 
sc query %serviceName1% | find "STOPPED"
if %ErrorLevel% EQU 0 (
    echo Step 1.1 - %serviceName1% Service is Stoppped
	echo Step 1.1 - %serviceName1% Service is Stoppped >> %logFile%
) else (
	echo Step 1.1 - Could not stop the service - %serviceName1% 
	echo Step 1.1 - Could not stop the service - %serviceName1% >> %logFile%
    goto manualDeployment 
)

::Step 2 - back up the older version - both .war and app folder in pwd

md .\Rollback\%date%

ping -n 10 127.0.0.1 > NUL
echo %warName% and %warName%.war will be backed up here .\Rollback\%date%\
echo %warName% and %warName%.war will be backed up here .\Rollback\%date%\ >> %logFile%

pause 

XCOPY %tomcatLocation1%\webapps\%warName%.war .\Rollback\%date%\ /R /I 
if %ErrorLevel% EQU 0 (
    echo Copied the .war to rollback 
) else (
	echo Error copying the .war to rollback .\Rollback\%date%\ , Please take the backup manually and continue.
	echo Error copying the .war to rollback .\Rollback\%date%\ , Please take the backup manually and continue. Error: %ErrorLevel%  >> %logFile%
    goto deploymentContinueWar1
)

:deploymentContinueWar1
pause

XCOPY %tomcatLocation1%\webapps\%warName% .\Rollback\%date%\%warName% /R /I /E
if %ErrorLevel% EQU 0 (
    echo Copied the %warName% to \Rollback\%date%
	echo Copied the %warName% to \Rollback\%date% >> %logFile%
) else (
	echo Error copying the %warName% to \Rollback\%date% , Please take the backup manually and continue.
	echo Error copying the %warName% to \Rollback\%date% Error: %ErrorLevel% >> %logFile%
    goto deploymentContinueWar2 
)

:deploymentContinueWar2
pause

ping -n 10 127.0.0.1 > NUL

echo Step 2 - Back up the older version here - .\Rollback\%date% , Now deleting the older version 
pause 

rd /q/s %tomcatLocation1%\webapps\%warName% 
if %ErrorLevel% EQU 0 (
    echo Removed the older app
	echo Removed the older app >> %logFile%
) else (
	echo Failed to remove the older app. Please remove the older version app manually and continue.
	echo Failed to remove the older app , Please remove the older version app manually and continue. Error: %ErrorLevel% >> %logFile%
    goto deploymentContinueWar3 
)
:deploymentContinueWar3
pause


del %tomcatLocation1%\webapps\%warName%.war 
if %ErrorLevel% EQU 0 (
    echo Removed the older .war
	echo Removed the older .war >> %logFile%
) else (
	echo Failed to remove the older .war, Please remove the older version .war manually and continue.
	echo Failed to remove the older .war , Please remove the older version .war manually and continue. Error: %ErrorLevel% >> %logFile%
    goto deploymentContinueWar4 
)
:deploymentContinueWar4

pause

ping -n 10 127.0.0.1 > NUL

echo Step 2 - Back up the older version here - .\Rollback\%date%

rename %warName%-%appName%.war %warName%.war
if %ErrorLevel% EQU 0 (
    echo Renamed the new version to %warName%.war
	echo Renamed the new version to %warName%.war >> %logFile%
) else (
	echo Renaming from %warName%-%appName%.war to %warName%.war failed.
	echo Renaming from %warName%-%appName%.war to %warName%.war failed. Error: %ErrorLevel% >> %logFile%
    goto deploymentContinueWar5 
)

:deploymentContinueWar5
pause

ping -n 10 127.0.0.1 > NUL

copy %warName%.war %tomcatLocation1%\webapps\
if %ErrorLevel% EQU 0 (
    echo Copying the new version to webapps done.
	echo Copying the new version to webapps done. >> %logFile%
) else (
	echo Copying the new version to webapps failed, Please copy it manually and continue.
	echo Copying the new version to webapps failed, Please copy it manually and continue. >>  %logFile%
    goto deploymentContinueWar6 
)

:deploymentContinueWar6
pause

echo Step 3 - New .war version of %warName% is copied to %tomcatLocation1%\webapps\

ping -n 10 127.0.0.1 > NUL

::Step 4 - Backup the logs

md %tomcatLocation1%\logs\%date%

ping -n 10 127.0.0.1 > NUL
move %tomcatLocation1%\logs\*.* %tomcatLocation1%\logs\%date%\
if %ErrorLevel% EQU 0 (
    echo Backedup logs at %tomcatLocation1%\logs\%date% 
	echo Backedup logs at %tomcatLocation1%\logs\%date% >> %logFile%
) else (
	echo Error backingup logs at %tomcatLocation1%\logs\%date% 
	echo Error backingup logs at %tomcatLocation1%\logs\%date%   >> %logFile%
	goto deploymentContinueWar7
)

:deploymentContinueWar7
pause

ping -n 10 127.0.0.1 > NUL

echo Step 4 - Logs backup at %tomcatLocation1%\logs\%date% 

::Step 4 - Start the services 

echo Step 5 - Starting the service 

sc start %serviceName1%
ping -n 10 127.0.0.1 > NUL


::Check if the service is started 
sc query %serviceName1% | find "RUNNING"
if %ErrorLevel% EQU 0 (
    echo Step 5.1 Start the service %serviceName1% - Service is RUNNING
	echo Step 5.1 Start the service %serviceName1% - Service is RUNNING. >> %logFile%
) else (
	echo Step 5.1 -  Could not start the service - %serviceName1%, Please start the service manually.
	echo Step 5.1 -  Could not start the service - %serviceName1%, Please start the service manually. >> %logFile%
    goto manualDeployment 
)

goto success

:manualDeployment 
echo Error! Please try again or go ahead with the manual deployment.
echo Error! Please try again or go ahead with the manual deployment. >> %logFile%

:success
echo Deployment Done

pause