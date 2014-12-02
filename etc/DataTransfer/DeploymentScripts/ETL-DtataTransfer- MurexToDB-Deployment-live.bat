@echo off 

::Deployment of ETL-DtataTransfer - MurexToDB-live

::ETL service names
set serviceName1=ANZETLApp_MxToRwh

::App name - app will be deployed with this name
set warName=mx

set appName=MurexFeedToDB


:: tomcat server locations
set tomcatLocation1=F:\ETL\ANZETLApp_QA_MxToRwh


::Get date as yyyymmdd_hhmm 
set date=%date:~-4%%date:~7,2%%date:~4,2%_%time:~0,2%%time:~3,2%

echo ********************MurexFeedToDB live Deployment********************
echo Please read/confirm below settings/configurations before proceeding
echo Application to be deployed: %appName%
echo Tomcat1 location : %tomcatLocation1%
echo Service1 : %serviceName1%

set /P continue=Confirm to continue(y/n):

if "%continue%"=="y" (GOTO Confirm) else  (GOTO manualDeployment)

:Confirm
::Step 1 - Stop the service

echo ETL-DtataTransfer - MurexFeedToDB-live
pause
echo Step 1 - Stop the services 

sc stop %serviceName1%

ping -n 300 127.0.0.1 > NUL

::Check if the service is stopped 
sc query %serviceName1% | find "STOPPED"
if %ErrorLevel% EQU 0 (
    echo Step 1.1 - %serviceName1% Service is Stoppped
) else (
	echo Step 1.1 - Could not stop the service - %serviceName1% 
    goto manualDeployment 
)

::Step 2 - back up the older version - both .war and app folder in pwd

md .\Rollback\%date%
ping -n 10 127.0.0.1 > NUL
echo %warName% and %warName%.war will be backed up here .\Rollback\%date%\

pause 

move %tomcatLocation1%\webapps\%warName%.war .\Rollback\%date%\
XCOPY %tomcatLocation1%\webapps\%warName% .\Rollback\%date%\%warName%\ /D /E /C /R /K /Y /I

ping -n 10 127.0.0.1 > NUL

echo Step 2 - Back up the older version here - .\Rollback\%date% , Now deleting the older version 
pause 

rd /q/s %tomcatLocation1%\webapps\%warName% 

ping -n 10 127.0.0.1 > NUL

echo Step 2 - Back up the older version here - .\Rollback\%date%

copy %warName%-%appName%.war %tomcatLocation1%\webapps\
rename %tomcatLocation1%\webapps\%warName%-%appName%.war %warName%.war

echo Step 3 - New .war version of %warName% is copied to %tomcatLocation1%\webapps\

ping -n 10 127.0.0.1 > NUL

::Step 4 - Backup the logs

md %tomcatLocation1%\logs\%date%
ping -n 10 127.0.0.1 > NUL
move %tomcatLocation1%\logs\*.* %tomcatLocation1%\logs\%date%\

ping -n 10 127.0.0.1 > NUL

echo Step 4 - Logs backup at %tomcatLocation1%\logs\%date% 

::Step 4 - Start the services 

echo Step 5 - Starting the service 

sc start %serviceName1%
ping -n 300 127.0.0.1 > NUL


::Check if the service is started 
sc query %serviceName1% | find "RUNNING"
if %ErrorLevel% EQU 0 (
    echo Step 5.1 Start the service %serviceName1% - Service is RUNNING
) else (
	echo Step 5.1 -  Could not start the service - %serviceName1%
    goto manualDeployment 
)

goto success

:manualDeployment 
echo Error! Go ahead with the manual deployment

:success
echo Deployment Done

pause
