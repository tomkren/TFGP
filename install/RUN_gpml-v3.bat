

REM PREREQUISITIES: git, python3 as python, java8 as java


call config_win.bat
call scripts\download_or_update_sources.bat




mkdir logs
mkdir logs\TFGP
mkdir logs\dag-evaluate



mkdir rundir
cd rundir

xcopy ..\sources\TFGP\release\v3 TFGP\ /sy
xcopy ..\sources\dag-evaluate dag-evaluate\ /sy

xcopy ..\datasets dag-evaluate\data\ /sy

copy ..\%CONFIG% TFGP\%CONFIG%
copy ..\%CONFIG% dag-evaluate\%CONFIG%






start ..\scripts\run_dag-evaluate.bat


cd TFGP
java -jar gpml.jar %CONFIG% ..\..\logs\TFGP > ..\..\logs\gp.log 2>&1
cd ..



cd dag-evaluate
rmdir cache /s /q
cd ..





cd ..

