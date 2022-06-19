//
// Created by WStars on 2022/6/18.
//
#include "include/render.h"
#include "include/GL/glut.h"

//渲染数据
extern int render[(256+16)*240];

void render_windows(){

    //设置背影颜色
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    //清除一个或一组特定的缓冲区
    glClear(GL_COLOR_BUFFER_BIT);
    //开始渲染,点,线,片
    glBegin(GL_POINTS);


    for(int h=0; h<240; h++) {
        for(int w=0;w<256; w++) {
            int pixels = render[(w ) + ((h ) * 256)];
            glColor3f(1.0,0.0,0.0);
            //顶点二维坐标
            glVertex2f(w, h);
        }
    }
    //结束
    glEnd();
    glFlush();
}