//
// Created by WStars on 2021/8/29.
//

#include <string.h>
#include <nes.h>
#include <stm32f4xx_ll_dma.h>
#include "ILI9341.h"

//FLASH中存储字符的起始地址
#define FONT_START_ADDR 0x08010000

void LL_uDelay(uint32_t us) {
    __IO uint32_t Delay = us * 100 / 8;
    do {
        __NOP();
    } while (Delay--);
}

void Lcd_Send_Data(uint16_t data) {
    GPIOA->ODR = data;
    //高四位置0
    GPIOB->ODR = (GPIOB->ODR & 0x0fff) | (data & 0xf000);
    LCD_WR_CLR
    LCD_WR_SET
}

void LCD_SetRegion(uint16_t xStar, uint16_t yStar, uint16_t xLength, uint16_t yLength) {
    uint16_t xEnd = xStar + xLength - 1;
    uint16_t yEnd = yStar + yLength - 1;

    LCD_WR_REG(0x2a);
    LCD_WR_DATA(xStar >> 8);
    LCD_WR_DATA(0x00FF & xStar);
    LCD_WR_DATA(xEnd >> 8);
    LCD_WR_DATA(0x00FF & xEnd);

    LCD_WR_REG(0x2b);
    LCD_WR_DATA(yStar >> 8);
    LCD_WR_DATA(0x00FF & yStar);
    LCD_WR_DATA(yEnd >> 8);
    LCD_WR_DATA(0x00FF & yEnd);

    LCD_WR_REG(0x2C);
}

void ILI9341_Init(void) {
    LCD_RST_CLR;
    LCD_RST_CLR;
    LCD_RST_SET
    LCD_RD_SET
    LCD_CS_SET
    LL_mDelay(50);

    LCD_WR_REG(0xCF);   //功耗控制B
    LCD_WR_DATA(0x00);
    LCD_WR_DATA(0x81);
    LCD_WR_DATA(0X30);
    LCD_WR_REG(0xED);   //电源序列控制
    LCD_WR_DATA(0x64);
    LCD_WR_DATA(0x03);
    LCD_WR_DATA(0X12);
    LCD_WR_DATA(0X81);
    LCD_WR_REG(0xE8);   //驱动时序控制A
    LCD_WR_DATA(0x85);
    LCD_WR_DATA(0x10);
    LCD_WR_DATA(0x78);
    LCD_WR_REG(0xCB);   //功耗控制A
    LCD_WR_DATA(0x39);
    LCD_WR_DATA(0x2C);
    LCD_WR_DATA(0x00);
    LCD_WR_DATA(0x34);
    LCD_WR_DATA(0x02);
    LCD_WR_REG(0xF7);
    LCD_WR_DATA(0x20);
    LCD_WR_REG(0xEA);   //驱动时序控制B
    LCD_WR_DATA(0x00);
    LCD_WR_DATA(0x00);
    LCD_WR_REG(0xC0);    //Power control
    LCD_WR_DATA(0x1B);   //VRH[5:0]
    LCD_WR_REG(0xC1);    //Power control
    LCD_WR_DATA(0x12);   //SAP[2:0];BT[3:0] 0x01
    LCD_WR_REG(0xC5);    //VCM control
    LCD_WR_DATA(0x08);     //30
    LCD_WR_DATA(0x26);     //30
    LCD_WR_REG(0xC7);    //VCM control2
    LCD_WR_DATA(0XB7);
    LCD_WR_REG(0x36);    // 存储器访问控制
    LCD_WR_DATA(0x08);
    LCD_WR_REG(0x3A);   //像素格式设置
    LCD_WR_DATA(0x55);
    LCD_WR_REG(0xB1);   //帧速率控制
    LCD_WR_DATA(0x00);
    LCD_WR_DATA(0x1A);
    LCD_WR_REG(0xB6);    // Display Function Control 显示功能控制
    LCD_WR_DATA(0x0A);
    LCD_WR_DATA(0xA2);
    LCD_WR_REG(0xF2);    // 3Gamma Function Disable 3伽马设置
    LCD_WR_DATA(0x00);
    LCD_WR_REG(0x26);    //Gamma curve selected 伽马曲线选择
    LCD_WR_DATA(0x01);
    LCD_WR_REG(0xE0);    //Set Gamma 正极伽马校准
    LCD_WR_DATA(0x0F);
    LCD_WR_DATA(0x1D);
    LCD_WR_DATA(0x1A);
    LCD_WR_DATA(0x0A);
    LCD_WR_DATA(0x0D);
    LCD_WR_DATA(0x07);
    LCD_WR_DATA(0x49);
    LCD_WR_DATA(0X66);
    LCD_WR_DATA(0x3B);
    LCD_WR_DATA(0x07);
    LCD_WR_DATA(0x11);
    LCD_WR_DATA(0x01);
    LCD_WR_DATA(0x09);
    LCD_WR_DATA(0x05);
    LCD_WR_DATA(0x04);
    LCD_WR_REG(0XE1);    //Set Gamma 负极伽马校准
    LCD_WR_DATA(0x00);
    LCD_WR_DATA(0x18);
    LCD_WR_DATA(0x1D);
    LCD_WR_DATA(0x02);
    LCD_WR_DATA(0x0F);
    LCD_WR_DATA(0x04);
    LCD_WR_DATA(0x36);
    LCD_WR_DATA(0x13);
    LCD_WR_DATA(0x4C);
    LCD_WR_DATA(0x07);
    LCD_WR_DATA(0x13);
    LCD_WR_DATA(0x0F);
    LCD_WR_DATA(0x2E);
    LCD_WR_DATA(0x2F);
    LCD_WR_DATA(0x05);
    LCD_WR_REG(0x2B);   //行地址设置 0-319
    LCD_WR_DATA(0x00);
    LCD_WR_DATA(0x00);
    LCD_WR_DATA(0x01);
    LCD_WR_DATA(0x3f);
    LCD_WR_REG(0x2A);   //列地址设置 0-239
    LCD_WR_DATA(0x00);
    LCD_WR_DATA(0x00);
    LCD_WR_DATA(0x00);
    LCD_WR_DATA(0xef);
    LCD_WR_REG(0x11); //退出睡眠模式
    LL_mDelay(120);
    LCD_WR_REG(0x29); //开始显示
    LL_mDelay(150);


    LCD_WR_REG(0x36);
    LCD_WR_DATA(0xac);
    LCD_SetRegion(0, 0, LCD_WIDTH, LCD_HEIGHT);
    //清屏
    LCD_WR_CLEAR(COLOR_BLACK);
    LL_uDelay(1000);

}

void LCD_WR_REG(uint16_t reg) {
    LCD_CS_CLR
    LCD_DC_CLR
    Lcd_Send_Data(reg);
    LCD_CS_SET;
}

void LCD_WR_DATA(uint16_t data) {
    LCD_CS_CLR
    LCD_DC_SET
    Lcd_Send_Data(data);
    LCD_CS_SET;
}

void LCD_WR_POINT(uint16_t color) {
    Lcd_Send_Data(color);
}

void LCD_WR_CLEAR(uint16_t color) {
    LCD_CS_CLR;
    LCD_DC_SET;
    for (int i = 0; i < LCD_WIDTH * LCD_HEIGHT; i++) {
        Lcd_Send_Data(color);
    }
    LCD_CS_SET;
}