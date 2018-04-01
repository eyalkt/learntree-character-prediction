#LearnTree: LearnTree.o
#	gcc -g -Wall -m32 -ansi -o LearnTree LearnTree.o
target: all

all: learntree predict

learntree: LearnTree.c
	gcc -g -Wall -m32 -ansi -o learntree LearnTree.c

predict: predict.c
	gcc -g -Wall -m32 -ansi -o predict predict.c

clean:
	rm -f *.o learntree predict
