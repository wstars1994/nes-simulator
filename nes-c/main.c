#include "include/nes.h"
#include "include/GL/glut.h"
#include <pthread.h>
#include <render.h>
pthread_t pthread;

int main(int argc, char *argv[]) {
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_RGBA|GLUT_STENCIL);
    glutInitWindowSize(256, 240);
    glutInitWindowPosition(1200, 300);
    glutCreateWindow("OpenGL Demo");
    glutDisplayFunc(&render_windows);
    pthread_create(&pthread,NULL,(void*)nes_start,NULL);
    glutMainLoop();
    return 0;
}
