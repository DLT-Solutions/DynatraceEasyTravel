-module(couchDB_ET).
-export([start/1,test_now/3,stop_now/3,crash_now/3,last_breath_message/1,service/3]).
-define(LOG_ERROR(Format, Args), couch_log:error(Format, Args)).
-define(LOG_INFO(Format, Args), couch_log:info(Format, Args)).
-compile(export_all).
% !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
% !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
%
% NOTE:
% AFTER MODIFYING THIS CODE, YOU NEED TO BUILD IT MANUALLY: 
% - install erlang version compatible with CouchDB (currently 23)
% - run erl.exe in this directory
% - execute 'c(couchDB_ET).'
%
% !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
% !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

%
% Module added to facilitate controlling CouchDB:
% Start up an HTTP server, and dynamically generate a couple of simple pages/services:
%
%    test_now - a test page that can be displayed to check that the server is operating;
%    stop_now - stops the Erlang program and exits Erlang;
%    crash_now - crashed the Erlang program.
%
% Then start CouchDB.
%
% The port number to be used with the HTTP server is passed as a parameter,
% with the -run option, and thus is expected to be a list of strings (of length one).
%

start(MyArgList) ->

% Extract command line arguments from the argument list.
% Note: later on I treat each argument as if it was a string and it appers to work OK.
erlang:display("MyArgList"),
erlang:display(MyArgList),
[Port|Tail] = MyArgList,
erlang:display("Have received port as:"),
erlang:display(Port),
[Folder|_] = Tail, % we don't care about the rest of the args, as there should not be any, so dump it to a wildcard
erlang:display("Have received folder path as:"),
erlang:display(Folder),

{PortInt, []} = string:to_integer(Port),

% It may be useful to show it on the console
erlang:display("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ CouchDB @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"),

% Erlang usage note:
% If testing above parameter passing, it is easiest to temporarily comment out the rest of this method.
% However, as Erlang does not support block comments, just use "-ifdef(comment)."  and "-endif."
% though first need to terminate current code with a period!

LogDirStr = string:concat(Folder,"/easyTravel 2.0.0/easyTravel/log"),
TmpDirStr = Folder,

erlang:display("final paths:"),
erlang:display(LogDirStr),
erlang:display(TmpDirStr),

% Erlang usage note:
% We do not need to ensure the directories that are required by the server,
% for server_root and document_root, exist - because we are using existing folders.
% However, if we needed to do that, we would call e.g.:
%     filelib:ensure_dir("somefolder/") - note the terminating '/'

% start the HTTP server
inets:start(),
inets:start(httpd, [
  {modules, [
   mod_alias,
   mod_auth,
   mod_esi,
   mod_actions,
   mod_cgi,
   mod_dir,
   mod_get,
   mod_head,
   mod_log,
   mod_disk_log
  ]},
  
  {port, PortInt},
  {server_name, "CouchDB Controller"},
  {document_root, TmpDirStr},
  {server_root, LogDirStr},
  {erl_script_alias, {"/erl", [couchDB_ET]}}, 
  % Important to choose names that are not going to clash
  % with existing file names.
  {error_log, "erl_error.log"},
  {security_log, "erl_security.log"},
  {transfer_log, "erl_transfer.log"},
      
  {mime_types,[ {"html","text/html"}, {"css","text/css"}, {"js","application/x-javascript"} ]},
  {bind_address,{0,0,0,0}}
]),

  erlang:display("Attempting to read the shutdown port to ensure the monitoring server is there OK..."),

  % First assemble the URL to the correct host, with the given port.
  % The host is the local host, because it is here that we start CouchDB.
  % The port has been given in the arguments.
  ControlUrlStr = "http://localhost:" ++ Port ++ "/erl/couchDB_ET:test_now",
  erlang:display("Control URL is:"),
  erlang:display(ControlUrlStr),

  % Note: it would be nice to enclose the following read in a try/catch.  However, Erlang seems to have a bug/limitation to do with
  % exporting variables from try/catch, and thus treats assignments done inside try/catch as uncertain (they do not always have to happen),
  % and the variables are considered 'unsafe', which is another way of saying 'might be unbound'.  Thus, one is unable to use such
  % variables afterwards.  It would make sense for this limitation to apply to the code following the entire try/catch, but in fact
  % it seems to apply to the try/catch body itself, which is silly.
  % There is probably a way to get around this, but I do no know how to do it right now.  Thus, if we want to examine the string we read,
  % the read cannot be in try/catch.
  % For more information see http://erlang.org/pipermail/erlang-bugs/2008-November/001083.html

  % The timeout here is important, else we could hang on the read forever.
  
  {ok, {{Version, 200, ReasonPhrase}, Headers, Body}} = httpc:request(get, {ControlUrlStr, []}, [{timeout, timer:seconds(10)}], []),

  % Now any of the follwing could have happened:
  %
  % 1. The match (assignment) can fail because of an incorrect number of elements e.g. if we fail on timeout,
  %    we get just two elements: {badmatch,{error,timeout}}, which is too few and so the assignment fails, as it
  %    does not match the expected parameters of {...}, Header, Body (three elements). This will result in a crash,
  %    as we do not catch this error.
  %    However, a crash is acceptable, as the only thing we want to do is to prevent CouchDB from starting.
  %
  % 2. The match (assignment) can be successful i.e. we have read something from the URL, but we still need to check
  %    that it was the correct string.

  % erlang:display("Have been able to read the shutdown port OK. Have read page body as:"),
  % erlang:display(Body),

  % Test if Body contains the correct string.
  case test(Body, "CouchDB controlling process is running") of
   true ->
	  erlang:display("Controlling process found on port OK...");
   false ->
      erlang:display("No control message found on port - aborting..."),
      init:stop(),
      halt(1)
  end,
  % Run timer task to log health check message
  timer:apply_after(600000, couchDB_ET, health_message, ["Performing Health Check"]).

service(SessionID, _Env, _Input) -> mod_esi:deliver(SessionID, [ 
   "Content-Type: text/html\r\n\r\n", "<html><body>Hello, World!</body></html>" ]).

test_now(SessionID, _Env, _Input) ->
mod_esi:deliver(SessionID, [
  "Content-Type: text/html\r\n\r\n",
  "<html><body>CouchDB controlling process is running...</body></html>"
]).

stop_now(SessionID, _Env, _Input) ->
mod_esi:deliver(SessionID, [
  "Content-Type: text/html\r\n\r\n",
  "<html><body>Stopping CouchDB now...</body></html>"
]),
init:stop(),
timer:sleep(5000),
halt(1).

crash_now(SessionID, _Env, _Input) ->
ErrorMsg = "exit value: {system_limit,[{erlang,spawn_link,[erlang,apply,[#Fun<shell.1.103280319>,[]]],[]},{erlang,spawn_link,1,[{file,\"erlang.erl\"},{line,2208}]},{shell,get_command,5,[{file,\"shell.erl\"},{line,298}]},{shell,server_loop,7,[{file,\"shell.erl\"},{line,229}]}]}",
mod_esi:deliver(SessionID, [
  "Content-Type: text/html\r\n\r\n",
  "<html><body>Crash now...</body></html>"
]),
% Trace error message just before process crash
last_breath_message(ErrorMsg),
timer:sleep(5000),
halt(abort).

% Look for substring in a string.
test(Source, Find) ->
    Pos = string:str(Source, Find),
    if
        Pos > 1 ->
            true;
        true ->
            false
    end.

% Trace stored directly to couch.log to be able to correlate process crash and related log entries
last_breath_message(Error) ->
  ?LOG_ERROR("OS Process Error ~s", [Error]).
  
simple_message(Msg) ->
  ?LOG_INFO("~s", [Msg]).
  
health_message(Msg) ->
  ?LOG_INFO("~s", [Msg]),
  simple_message("Health: OK"),
  % Shchedule next call to this - I was not able implement it diffrently
  timer:apply_after(600000, couchDB_ET, health_message, [Msg]).