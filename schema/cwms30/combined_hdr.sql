--------------------------------------------------------------------------------
-- Header for the combined sql file that defines an OPENDCS TSDB under Oracle --
--
-- Cove Software, LLC
--------------------------------------------------------------------------------
set echo on
spool combined.log

whenever sqlerror continue
set define on
@@defines.sql

