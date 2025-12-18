Run binaries
===========

Contents
- server.jar (ChatServer entry point)
- user.jar (LoginFrame entry point)
- admin.jar (AdminMainFrame entry point)
- release/config.properties (database, Supabase, SMTP)
- lib/ (runtime dependencies), icons/ (UI assets)

Update config
- Edit `release/config.properties` before launching; jars read it relative to the run folder.

Start commands (PowerShell)
- Server: `powershell -ExecutionPolicy Bypass -File .\run-server.ps1`
- User client: `powershell -ExecutionPolicy Bypass -File .\run-user.ps1`
- Admin console: `powershell -ExecutionPolicy Bypass -File .\run-admin.ps1`

Notes
- Keep the working directory at `run/` so relative paths to config/icons stay valid.
- Jars use UTF-8 via `-Dfile.encoding=UTF-8` in the scripts.
