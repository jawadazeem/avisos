Just some of the next things I'd like to take care of:
1) Understand what type of email server to use to actually be able to send emails and implement
2) Create a robust CI/CD pipeline, enterprise grade, just under our constraints, which are:
   - The entire application itself is based on ONE application server. There isn't even a web server. But that is ok, I guess. I just need a solid flow in .github/workflows where I can effectively push to origin main, and that the server is able to pickup on it. The current way, which is not actually currently working, I think there is a problem on the server side, is that the github daemon runs on the server, scans for any pushes to origin main, pulls when there is one, builds on itself (which is ok), then 
3) Want to implement Scikit Learn on here SOMEWHERE need to know where. maybe even tensorflow. I know none of these libs or anything, want to learn but also apply in a useful fashion
4) 