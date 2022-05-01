//
// Created by Administrator on 2021/8/29.
//

#ifndef STM32_SDCARD_ILI9341_H
#define STM32_SDCARD_ILI9341_H

#endif //STM32_SDCARD_ILI9341_H

#include "stm32f4xx_ll_gpio.h"
#include "stm32f4xx_ll_utils.h"

#define GPIO_TYPE GPIOB

#define LCD_CS  LL_GPIO_PIN_3
#define LCD_DC  LL_GPIO_PIN_4
#define LCD_RST LL_GPIO_PIN_7
#define LCD_WR  LL_GPIO_PIN_6
#define LCD_RD  LL_GPIO_PIN_8

//GPIO
#define	LCD_CS_SET  	 LL_GPIO_SetOutputPin(GPIO_TYPE,LCD_CS);
#define	LCD_DC_SET  	 LL_GPIO_SetOutputPin(GPIO_TYPE,LCD_DC);
#define	LCD_RST_SET  	 LL_GPIO_SetOutputPin(GPIO_TYPE,LCD_RST);
#define	LCD_WR_SET  	 LL_GPIO_SetOutputPin(GPIO_TYPE,LCD_WR);
#define	LCD_RD_SET  	 LL_GPIO_SetOutputPin(GPIO_TYPE,LCD_RD);

#define	LCD_CS_CLR  	 LL_GPIO_ResetOutputPin(GPIO_TYPE,LCD_CS);
#define	LCD_DC_CLR   	 LL_GPIO_ResetOutputPin(GPIO_TYPE,LCD_DC);
#define	LCD_RST_CLR  	 LL_GPIO_ResetOutputPin(GPIO_TYPE,LCD_RST);
#define	LCD_WR_CLR  	 LL_GPIO_ResetOutputPin(GPIO_TYPE,LCD_WR);
#define	LCD_RD_CLR  	 LL_GPIO_ResetOutputPin(GPIO_TYPE,LCD_RD);

#define	LCD_WIDTH 320
#define	LCD_HEIGHT 240

#define	COLOR_BLACK 0x0000
#define	COLOR_WHITE 0xFFFF
#define	COLOR_RED 0xF800
#define	COLOR_GREEN 0x07e0
#define	COLOR_BLUE 0x001F
#define	COLOR_YELLOW 0xffe0
#define	COLOR_PURPLE 0x8010
#define	COLOR_GRAY 0x8410
#define	COLOR_BROWN 0xa145


void ILI9341_Init(void);

void LCD_WR_REG(uint16_t reg);

void LCD_WR_DATA(uint16_t data);

void LCD_WR_CLEAR(uint16_t color);

void LCD_SetRegion(uint16_t xStar, uint16_t yStar, uint16_t xLength, uint16_t yLength);

void LCD_WR_POINT(uint16_t color);

void Lcd_Send_Data(uint16_t data);
