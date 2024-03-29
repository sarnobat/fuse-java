echo "SRIDHAR $@" >&2
test "$1" = "/" && echo "top_level"
test "$1" = "/" || echo "Not_top_level"
