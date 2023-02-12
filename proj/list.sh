# echo "ARG = $1"
if [ "$1" = "/" ]
then
#     seq 1 5
    # TODO: get individuals with no parents
    echo "I25"
elif [ "$1" = "" ]
then
#     seq 1 5
    # TODO: get individuals with no parents
    echo "I25"
else
# 	seq 11 15
	# TODO: get individuals who have parent equal to $1
	GED=/Volumes/git/repos_personal.git/sarnobat.git/2022/genealogy/auto/
	sort $GED/gedcom_member_of_family.auto.csv | grep "^I25\b" > 1.txt
	sort $GED/gedcom_family_to_child.auto.csv | perl -pe 's{\t}{ }g' | grep 'I25\b' | awk '{print $2,$1}' > 2.txt
	join 1.txt 2.txt
	echo "TODO: get the chidlren in this family"
fi

