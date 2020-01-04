Set oShell = CreateObject ("Wscript.Shell")
Dim strArgs
strArgs = "cmd /c Jouska.bat"
oShell.Run strArgs, 0, false
