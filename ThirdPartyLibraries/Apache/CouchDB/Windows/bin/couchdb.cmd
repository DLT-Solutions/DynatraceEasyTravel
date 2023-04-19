@ECHO OFF

:: Licensed under the Apache License, Version 2.0 (the "License"); you may not
:: use this file except in compliance with the License. You may obtain a copy of
:: the License at
::
::   http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
:: WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
:: License for the specific language governing permissions and limitations under
:: the License.

SET COUCHDB_BIN_DIR=%~dp0
SET ROOTDIR=%COUCHDB_BIN_DIR%\..\
CD "%ROOTDIR%"

SET /P START_ERL= < releases\start_erl.data
FOR /F "tokens=1" %%G IN ("%START_ERL%") DO SET ERTS_VSN=%%G
FOR /F "tokens=2" %%G IN ("%START_ERL%") DO SET APP_VSN=%%G

set BINDIR=%ROOTDIR%/erts-%ERTS_VSN%/bin
set EMU=beam
set PROGNAME=%~n0
set PATH=%COUCHDB_BIN_DIR%;%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SYSTEMROOT%\System32\WindowsPowerShell\v1.0\

IF NOT DEFINED COUCHDB_QUERY_SERVER_JAVASCRIPT SET COUCHDB_QUERY_SERVER_JAVASCRIPT=./bin/couchjs ./share/server/main.js
IF NOT DEFINED COUCHDB_QUERY_SERVER_COFFEESCRIPT SET COUCHDB_QUERY_SERVER_COFFEESCRIPT=./bin/couchjs ./share/server/main-coffee.js
IF NOT DEFINED COUCHDB_FAUXTON_DOCROOT SET COUCHDB_FAUXTON_DOCROOT=./share/www

rem "%BINDIR%\erl" -boot "%ROOTDIR%\releases\%APP_VSN%\couchdb" ^
rem -args_file "%ROOTDIR%\etc\vm.args" ^
rem -epmd "%BINDIR%\epmd.exe" ^
rem -config "%ROOTDIR%\releases\%APP_VSN%\sys.config" ^
rem -run "couchDB_ET" start %*

rem "%BINDIR%\erl" -boot "%ROOTDIR%\releases\%APP_VSN%\couchdb" ^
rem -args_file "%ROOTDIR%\etc\vm.args" ^
rem -epmd "%BINDIR%\epmd.exe" ^
rem -config "%ROOTDIR%\releases\%APP_VSN%\sys.config" ^
rem -run "couchDB_ET start 5989 C:/Users/Rafal.Psciuk/.dynaTrace"

rem erts-11.2.2.12\bin\erl.exe -boot "C:\workspaces\etgit3\easytravel\Distribution\dist\weblauncher\..\couchdb\Windows\releases\3.2.2\couchdb" -args_file "C:\workspaces\etgit3\easytravel\Distribution\dist\weblauncher\..\couchdb\Windows\etc\vm.args" -epmd "C:\workspaces\etgit3\easytravel\Distribution\dist\weblauncher\..\couchdb\Windows\erts-11.2.2.12\bin\epmd.exe" -config "C:\workspaces\etgit3\easytravel\Distribution\dist\weblauncher\..\couchdb\Windows\releases\3.2.2\sys.config" -couch_ini "C:\Users\Rafal.Psciuk\.dynaTrace\easyTravel 2.0.0\easyTravel\config\local.ini" "C:\workspaces\etgit3\easytravel\Distribution\dist\weblauncher\..\couchdb\Windows\etc\default.ini" "C:\workspaces\etgit3\easytravel\Distribution\dist\weblauncher\..\couchdb\Windows\etc\local.d\10-admins.ini" -run couchDB_ET start 5989 C:/Users/Rafal.Psciuk/.dynaTrace

"%BINDIR%\erl" -boot "%ROOTDIR%\releases\%APP_VSN%\couchdb" -args_file "%ROOTDIR%\etc\vm.args" -epmd "C:\workspaces\etgit3\easytravel\Distribution\dist\weblauncher\..\couchdb\Windows\erts-11.2.2.12\bin\epmd.exe" -config "C:\workspaces\etgit3\easytravel\Distribution\dist\weblauncher\..\couchdb\Windows\releases\3.2.2\sys.config" -couch_ini "C:\Users\Rafal.Psciuk\.dynaTrace\easyTravel 2.0.0\easyTravel\config\local.ini" "C:\workspaces\etgit3\easytravel\Distribution\dist\weblauncher\..\couchdb\Windows\etc\default.ini" "C:\workspaces\etgit3\easytravel\Distribution\dist\weblauncher\..\couchdb\Windows\etc\local.d\10-admins.ini" -run couchDB_ET start 5989 C:/Users/Rafal.Psciuk/.dynaTrace

:: EXIT /B
