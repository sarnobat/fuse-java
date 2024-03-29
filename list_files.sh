echo "SRIDHAR $@" >&2
test "$1" = "/" && cat /Volumes/git/2023/repos_personal.git/sarnobat.git/2023/genealogy/rohidekar.ged | grep NAME
test "$1" = "/" || echo "Not_top_level"
