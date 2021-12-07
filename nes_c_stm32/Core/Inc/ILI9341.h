//
// Created by Administrator on 2021/8/29.
//

#ifndef STM32_SDCARD_ILI9341_H
#define STM32_SDCARD_ILI9341_H

#endif //STM32_SDCARD_ILI9341_H

#include "stm32f4xx_ll_spi.h"
#include "stm32f4xx_ll_gpio.h"
#include "stm32f4xx_ll_utils.h"

#define GPIO_TYPE GPIOA

#define LCD_CS  LL_GPIO_PIN_10
#define LCD_RST LL_GPIO_PIN_9
#define LCD_DC  LL_GPIO_PIN_8

//GPIO
#define	LCD_CS_SET  	  LL_GPIO_SetOutputPin(GPIO_TYPE,LCD_CS);
#define	LCD_DC_SET  	  LL_GPIO_SetOutputPin(GPIO_TYPE,LCD_DC);
#define	LCD_RST_SET  	  LL_GPIO_SetOutputPin(GPIO_TYPE,LCD_RST);

#define	LCD_CS_CLR  	  LL_GPIO_ResetOutputPin(GPIO_TYPE,LCD_CS);
#define	LCD_DC_CLR   	  LL_GPIO_ResetOutputPin(GPIO_TYPE,LCD_DC);
#define	LCD_RST_CLR  	  LL_GPIO_ResetOutputPin(GPIO_TYPE,LCD_RST);

#define	LCD_WIDTH 320
#define	LCD_HEIGHT 240

void ILI9341_Init(void);

void LCD_WR_REG(uint8_t reg);

void LCD_WR_DATA(uint8_t data);

void LCD_WR_CLEAR(uint16_t color);

void LCD_SetRegion(uint16_t xStar, uint16_t yStar, uint16_t xEnd, uint16_t yEnd);

void LCD_WR_POINT(uint16_t color);

void LCD_W_POINT_CONTINUE(uint16_t *render);