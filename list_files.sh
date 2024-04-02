cat <<'EOF' | tee /tmp/gedcom_individuals.awk >/dev/null

$0  ~ /I[0-9]{1,}$/    {
	PATH=$1
	gsub(/\//,"",PATH)
	printf("grep '%s\\b' /Volumes/git/2023/repos_personal.git/sarnobat.git/2023/genealogy/auto/gedcom_individuals.auto.csv\n", PATH)
}

EOF

cat <<'EOF' | tee /tmp/gedcom_individual_code_to_name.sh >/dev/null
basename "$1" | awk -f /tmp/gedcom_individuals.awk | tee -a /tmp/gedcom_grep.out | xargs -d'\n' -I% sh -c "%" | perl -pe 's{^[^s]+\s+}{}' 
EOF

echo "SRIDHAR $@" >&2
test "$1" = "/" && echo "by_name"
test "$1" = "/by_name" && cat /Volumes/git/2023/repos_personal.git/sarnobat.git/2023/genealogy/rohidekar.ged | grep INDI | perl -pe 's{0 @(I\d+)@ INDI}{$1}'
# test "$1" = "/" || grep --no-filename `basename "$1"` /Volumes/git/2023/repos_personal.git/sarnobat.git/2023/genealogy/auto/gedcom_individuals.auto.csv | perl -pe 's{^[^s]+\s+}{}'
# [[ "$1" =~ /by_name/I ]] && echo "yes.txt"
# [[ "$1" =~ '/by_name/I[0-9]*$' ]] && echo "$1" | awk -f /tmp/gedcom_individuals.awk | xargs -d'\n' -I% sh -c "%" | perl -pe 's{^[^s]+\s+}{}' | perl -pe 's{\s*$}{.txt}'
# [[ "$1" =~ '/by_name/I' ]] && echo "$1---"

(echo "$1" | grep -q -P "/by_name/I[0-9]{1,4}$") && (sh /tmp/gedcom_individual_code_to_name.sh "$1" | perl -pe 's{\s*$}{.txt}')
