//
// Created by Administrator on 2021/8/29.
//

#ifndef STM32_SDCARD_ILI9341_H
#define STM32_SDCARD_ILI9341_H

#endif //STM32_SDCARD_ILI9341_H

#include "stm32f4xx_hal.h"

#define GPIO_TYPE   GPIOA

#define LCD_CS      GPIO_PIN_10
#define LCD_RST     GPIO_PIN_9
#define LCD_DC      GPIO_PIN_8


//GPIO
#define	LCD_CS_SET  	  HAL_GPIO_WritePin(GPIO_TYPE,LCD_CS,GPIO_PIN_SET);
#define	LCD_DC_SET  	  HAL_GPIO_WritePin(GPIO_TYPE,LCD_DC,GPIO_PIN_SET);
#define	LCD_RST_SET  	  HAL_GPIO_WritePin(GPIO_TYPE,LCD_RST,GPIO_PIN_SET);
#define	LCD_BG_SET  	  HAL_GPIO_WritePin(GPIO_TYPE,LCD_BG,GPIO_PIN_SET);

#define	LCD_CS_CLR  	  HAL_GPIO_WritePin(GPIO_TYPE,LCD_CS,GPIO_PIN_RESET);
#define	LCD_DC_CLR   	  HAL_GPIO_WritePin(GPIO_TYPE,LCD_DC,GPIO_PIN_RESET);
#define	LCD_RST_CLR  	  HAL_GPIO_WritePin(GPIO_TYPE,LCD_RST,GPIO_PIN_RESET);


#define	LCD_WIDTH 320
#define	LCD_HEIGHT 240

void ILI9341_Init(void);

void LCD_WR_REG(uint8_t reg);

void LCD_WR_DATA(uint8_t data);

void LCD_WR_DATA_16(uint16_t color);

void LCD_WR_CLEAR(uint16_t color);

void LCD_SetRegion(uint16_t xStar, uint16_t yStar, uint16_t xEnd, uint16_t yEnd);

void LCD_Show_Image(const unsigned char *p);

void LCD_Show_Char(uint8_t p,uint16_t start_x,uint16_t start_y,uint16_t color);

void LCD_WR_POINT(uint16_t color);

void LCD_W_POINT_CONTINUE(uint8_t render[256][3]);
