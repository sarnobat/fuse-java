if [ "$1" = "/" ]
then

seq 2009 2023
elif [ "$1" = "" ]
then

echo "b_file.txt"
#echo "$1"

else
	echo "a_file_.txt"
	cat /Volumes/git/repos_personal.git/sarnobat.git/mint.com/sanitized.csv | perl -pe 's{^\d*/\d*/(\d*),.*,(.*)}{$1,\L$2.txt}g' | grep `basename $1`
fi
