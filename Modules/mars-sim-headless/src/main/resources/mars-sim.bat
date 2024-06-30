:: This batch file will run the command "java -jar ./[$VERSION_OR_BUILD]_headless_java17.jar new" 
:: Ensure you replace the [$VERSION_OR_BUILD] with the correct version (such as 3.6.3) or build number (such as 8718) in the jarfile
@echo off
call java -jar ./mars-sim_3.6.3_headless_java17.jar new
echo Exit Code = %ERRORLEVEL%
if "%ERRORLEVEL%" == "1" exit /B 1