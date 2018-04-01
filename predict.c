#include <stdio.h>
#include <string.h>

int main(int argc, char **argv) { /*change LearnTree class name !*/
  char command[200];
  /*system("rm -f *.o LearnTree");*/
  strcpy(command, "java prediction/Predict ");/*-cp kfkfkf*/
  /*
  strcat(command, argv[0]);
  strcat(command, ".class ");
  */
  int i;
  for (i=1; i<argc; i++) {
    strcat(command, argv[i]);
    strcat(command, " ");
  }
  puts(command);
  system(command);
  return 0;
}
