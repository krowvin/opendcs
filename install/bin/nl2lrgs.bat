@echo off
rem
rem nl2lrgs - Convert Network Lists to LRGS file format
rem
rem usage: nl2lrgs <options> <netlist1> <netlist2> ...
rem options:
rem	-e               Export from 'editable' database (default is installed)
rem
$INSTALL_PATH\bin\decj decodes.dbimport.LrgsNetlist %*%
