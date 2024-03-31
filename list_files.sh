cat <<'EOF' | tee /tmp/gedcom.awk >/dev/null

$0  ~ /I[0-9]{1,}$/    {
	PATH=$1
	gsub(/\//,"",PATH)
	printf("grep %s /Volumes/git/2023/repos_personal.git/sarnobat.git/2023/genealogy/auto/gedcom_individuals.auto.csv\n", PATH)
}

EOF

echo "SRIDHAR $@" >&2
test "$1" = "/" && cat /Volumes/git/2023/repos_personal.git/sarnobat.git/2023/genealogy/rohidekar.ged | grep INDI | perl -pe 's{0 @(I\d+)@ INDI}{$1}'
# test "$1" = "/" || grep --no-filename `basename "$1"` /Volumes/git/2023/repos_personal.git/sarnobat.git/2023/genealogy/auto/gedcom_individuals.auto.csv | perl -pe 's{^[^s]+\s+}{}'
test "$1" = "/" || echo "$1" | awk -f /tmp/gedcom.awk | xargs -d'\n' -I% sh -c "%" | perl -pe 's{^[^s]+\s+}{}' | perl -pe 's{\s*$}{.txt}'