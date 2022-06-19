#include "include/nes.h"
#include "include/GL/glut.h"
#include <pthread.h>
pthread_t pthread;

void display(void) {
    if(pthread == NULL){
        pthread_create(&pthread,NULL,(void*)nes_start,NULL);
    }
}

int main(int argc, char *argv[]) {
    glutInit(&argc, argv);
//    glutInitDisplayMode(GLUT_DOUBLE|GLUT_RGBA|GLUT_STENCIL);
    glutInitWindowSize(256, 240);
    glutInitWindowPosition(200, 100);
    glutCreateWindow("OpenGL Demo");

    glutDisplayFunc(display);

    glutMainLoop();
    return 0;
}
