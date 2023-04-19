{application,couch_log,
             [{description,"CouchDB Log API"},
              {vsn,"3.2.2"},
              {registered,[couch_log_sup]},
              {applications,[kernel,stdlib,config]},
              {mod,{couch_log_app,[]}},
              {modules,[couch_log,couch_log_app,couch_log_config,
                        couch_log_config_dyn,couch_log_error_logger_h,
                        couch_log_formatter,couch_log_monitor,
                        couch_log_server,couch_log_sup,couch_log_trunc_io,
                        couch_log_trunc_io_fmt,couch_log_util,
                        couch_log_writer,couch_log_writer_file,
                        couch_log_writer_journald,couch_log_writer_stderr,
                        couch_log_writer_syslog]}]}.